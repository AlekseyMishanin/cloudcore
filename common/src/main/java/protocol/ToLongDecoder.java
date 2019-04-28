package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение длины файла.
 *
 * @author Mishanin Aleksey
 * */
public class ToLongDecoder extends AbstractHandler {

    private PackageBody packageBody;

    public ToLongDecoder(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.FILE ||
                packageBody.getCommand() == ProtocolCommand.FILERESPONSE) &&
                packageBody.getStatus() == PackageBody.Status.READLENGTHFILE) {
            //если для чтения доступно менее 8-и байт
            if (buf.readableBytes() < 8) {
                //прекращаем обарботку
                return;
            }
            //записываем long в поле длины файла
            packageBody.setLenghFile(buf.readLong());
            //присваиваем пакету статус: чтение файла
            packageBody.setStatus(PackageBody.Status.READFILE);
            //System.out.println(6);
        }
        //пересылаем сообщение следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
