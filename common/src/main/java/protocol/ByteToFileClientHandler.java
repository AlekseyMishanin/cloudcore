package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Класс инкапсулирует часть протокола, отвечающую за загрузку файла на локальную машину клиента.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToFileClientHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToFileClientHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if(packageBody.getCommand() ==  ProtocolCommand.FILERESPONSE &&
                packageBody.getStatus() == PackageBody.Status.READFILE) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //System.out.println(7);
            //если в буфере есть данные для чтения
            while (buf.isReadable()){
                //определяем кол-во доступных для чтения байт
                int j = buf.readableBytes();
                //создаем временный буфер под байты
                byte[] data= new byte[j];
                //считываем данные во временный буфер
                buf.readBytes(data);
                //записываем байты в файл
                Files.write(Paths.get("clientA/", packageBody.getNameFile()), data, CREATE, APPEND);
                //уменьшаем длину файла в пакете
                packageBody.setLenghFile(packageBody.getLenghFile()-j);
                //освобождаем сообщение
                ReferenceCountUtil.release(msg);
            }
            //если не осталось байт для записи
            if (packageBody.getLenghFile() <= 0) {
                //System.out.println(8);
                //очищаем ракет
                packageBody.clear();
            }
        } else {
            //отправляем сообщение к следующему ChannelHandler
            ctx.fireChannelRead(msg);
        }
    }
}
