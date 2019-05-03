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
                (packageBody.getStatus() == PackageBody.Status.READLENGTHUSER ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHNAMEFILE ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHSTRUCTURE ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHPATHCATALOG)) {
            //если для чтения доступно менее 4-х байт
            if (buf.readableBytes() < 4) {
                //прекращаем обарботку
                return;
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHUSER) {
                //записываем int в поле длины имени пользователя
                packageBody.setLenghUserName(buf.readInt());
                //присваиваем пакету статус: чтение имени пользователя
                packageBody.setStatus(PackageBody.Status.READNAMEUSER);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHNAMEFILE){
                packageBody.setLenghFileName(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READNAMEFILE);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHSTRUCTURE){
                packageBody.setLengthStructure(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READSTRINGSTRUCTURE);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHPATHCATALOG){
                packageBody.setLengthVariable(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READPATHNEWCATALOG);
            }
            
        }
        //пересылаем сообщение следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
