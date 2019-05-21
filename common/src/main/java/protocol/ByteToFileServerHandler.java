package protocol;

import db.SqlService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RecursiveAction;

/**
 * Класс инкапсулирует часть протокола, отвечающую за загрузку файла на сервер.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToFileServerHandler extends AbstractHandler{

    private PackageBody packageBody;    //объект протокола
    private byte[] dataArr;             //временный массив. Чтобы не создавать множество отдельных массивов, т.е. избежать мусора
    private FileOutputStream out;       //байтовый поток
    private long lengthFileLocal;       //переменная содержит размер файла

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
            String fileStr = "ServerCloud/" +
                    ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN) +
                    "/" + packageBody.getNameFile();
            //если байтовый поток не существует
            if(out == null) {
                //строим путь ко временной папке пользователя. Временная папка = ServerCloud/ЛогинПользователя
                Path path = Paths.get("ServerCloud/" +
                        ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN));
                //если в каталоге ServerCloud не существует папки пользователя, то создаем папку
                if(!Files.exists(path)){Files.createDirectory(path);}
                //присваиваем переменной хандлера длину файла
                lengthFileLocal = packageBody.getLenghFile();
                //создаем байтовый поток к файлу во временной папке
                out = new FileOutputStream(fileStr);
            }
            //если в буфере есть данные для чтения
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
                //проверяем контрольную сумму файла
                if((packageBody.getChecksum() == Packages.getMd5(new File(fileStr)))) {
                    //запускаем задачу в демоне
                    new RecursiveAction() {
                        @Override
                        protected void compute() {
                            //отправляем sql-запрос к БД на добавление записи в таблицу. На основании возвращаемого результата определяем результат завершения операции
                            if (SqlService.getInstance().insertNewFile(
                                    packageBody.getVariable(),
                                    packageBody.getNameFile(),
                                    lengthFileLocal,
                                    Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client, String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)),
                                    "ServerCloud/" +
                                            ctx.channel().attr(AttributeKey.<HashMap<Client, String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN) +
                                            "/" + packageBody.getNameFile()
                            )) {
                                try {
                                    //если новая запись была успешно добавлена в БД, то отправляем клиенту сообщение о том, что структура каталогов обновилась
                                    Packages.updateStructure(ctx.channel());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.fork();
                } else {
                    System.out.println("bad");
                    Packages.fileError(ctx.channel());
                }
                //поспим немного, чтобы дочерний поток получил корректные аргументы на вход
                Thread.sleep(100);
                //очищаем пакет
                packageBody.clear();
                //закрываем поток
                out.close();
                out = null;
                lengthFileLocal = 0;
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    public void closeileOutputStream(){
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
