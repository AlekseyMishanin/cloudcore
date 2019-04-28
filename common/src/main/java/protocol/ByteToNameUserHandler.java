package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение имени пользователя.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToNameUserHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToNameUserHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() != null) &&
                packageBody.getStatus() == PackageBody.Status.READNAMEUSER) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины имени файла
            if (buf.readableBytes() < packageBody.getLenghUserName()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер под имя пользователя
            byte[] data = new byte[packageBody.getLenghUserName()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //в пакете присваиваем новое имя пользователя
            packageBody.setNameUser(new String(data));
            //присваиваем статус: чтение длины имени файла
            packageBody.setStatus(PackageBody.Status.READLENGTHNAMEFILE);
            //System.out.println(3);
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
