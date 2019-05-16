package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение имени файла.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToNameFileHandler extends AbstractHandler {

    private PackageBody packageBody;    //ссылка на объект протокола

    public ByteToNameFileHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() != null) &&
                packageBody.getStatus() == PackageBody.Status.READNAMEFILE) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины имени файла
            if (buf.readableBytes() < packageBody.getLenghFileName()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер под имя файла
            byte[] data = new byte[packageBody.getLenghFileName()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //в пакете присваиваем новое имя файла
            packageBody.setNameFile(new String(data));
            //если команда в пакете: загрузка файла на сервер или загрузка файла на машину клиента
            if(packageBody.getCommand() == ProtocolCommand.FILE ||
                    packageBody.getCommand() == ProtocolCommand.FILERESPONSE){
                //присваиваем статус: чтение длины файла
                packageBody.setStatus(PackageBody.Status.READLENGTHFILE);
            }
            //если команда в пакете: запрос файла на сервере для скачивания
            if(packageBody.getCommand() == ProtocolCommand.FILEREQUEST){
                //присваиваем статус: скачать файл
                packageBody.setStatus(PackageBody.Status.WRITEFILE);
            }
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
