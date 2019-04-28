package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение длины имени файла или пользователя.
 *
 * @author Mishanin Aleksey
 * */
public class ToIntegerDecoder extends AbstractHandler {

    private PackageBody packageBody;

    public ToIntegerDecoder(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() !=null) &&
                packageBody.getStatus() == PackageBody.Status.READLENGTHUSER) {
            //если для чтения доступно менее 4-х байт
            if (buf.readableBytes() < 4) {
                //прекращаем обарботку
                return;
            }
            //записываем int в поле длины имени пользователя
            packageBody.setLenghUserName(buf.readInt());
            //присваиваем пакету статус: чтение имени пользователя
            packageBody.setStatus(PackageBody.Status.READNAMEUSER);
            //System.out.println(2);

            //если пакет содержит нужную команду и статус
        } else if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() != null) &&
                packageBody.getStatus() == PackageBody.Status.READLENGTHNAMEFILE) {
            //если для чтения доступно менее 4-х байт
            if (buf.readableBytes() < 4) {
                //прекращаем обарботку
                return;
            }
            //записываем int в поле длины имени файла
            packageBody.setLenghFileName(buf.readInt());
            //присваиваем пакету статус: чтение имени файла
            packageBody.setStatus(PackageBody.Status.READNAMEFILE);
            //System.out.println(4);
        }
        //пересылаем сообщение следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
