package protocol;

import db.SqlService;
import db.arhive.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

/**
 * Класс инкапсулирует часть протокола, отвечающую за загрузку файла на сервер.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToFileServerHandler extends AbstractHandler{

    private PackageBody packageBody;
    private byte[] dataArr;
    private FileOutputStream out;
    private long lengthFileLocal;

    public ByteToFileServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        dataArr = new byte[1024];
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if(packageBody.getCommand() == ProtocolCommand.FILE &&
                packageBody.getStatus() == PackageBody.Status.READFILE) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //System.out.println(7);
            //если в буфере есть данные для чтения
            if(out == null) {
                Path path = Paths.get("ServerCloud/" +
                        ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN));
                if(!Files.exists(path)){Files.createDirectory(path);}
                lengthFileLocal = packageBody.getLenghFile();
                out = new FileOutputStream("ServerCloud/" +
                        ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN) +
                        "/" + packageBody.getNameFile());
            }
            while (buf.isReadable()){
                //определяем кол-во доступных для чтения байт
                int j = buf.readableBytes() > 1024 ? 1024 : buf.readableBytes();
                //записываем байты во временный буфер
                buf.readBytes(dataArr,0,j);
                //записываем байты в файл
                out.write(dataArr,0,j);
                //уменьшаем длину файла в пакете
                packageBody.setLenghFile(packageBody.getLenghFile()-j);
            }
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            //если не осталось байт для записи
            if (packageBody.getLenghFile() <= 0) {

                new RecursiveAction(){
                    @Override
                    protected void compute() {
                        if(SqlService.getInstance().insertNewFile(
                                packageBody.getVariable(),
                                packageBody.getNameFile(),
                                lengthFileLocal,
                                Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)),
                                "ServerCloud/" +
                                        ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN) +
                                        "/" + packageBody.getNameFile()
                        )){
                            try {
                                Packages.updateStructure(ctx.channel());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.fork();
                //поспим немного, чтобы дочерний поток получил корректные аргументы на вход
                Thread.sleep(100);
                //очищаем пакет
                packageBody.clear();
                out.close();
                out = null;
                lengthFileLocal = 0;
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
