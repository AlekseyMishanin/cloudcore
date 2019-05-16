package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение длины имени файла или пользователя, или каталога.
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
                        packageBody.getStatus() == PackageBody.Status.READLENGTHPATHCATALOG ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHDELETECATALOG ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHCATALOGFORFILE ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHOLDPATH ||
                        packageBody.getStatus() == PackageBody.Status.READLENGTHNEWPATH)) {
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
                System.out.println(5);
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
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHDELETECATALOG){
                packageBody.setLengthVariable(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READNAMEDELETECATALOG);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHCATALOGFORFILE){
                System.out.println(2);
                packageBody.setLengthVariable(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READNAMECATALOGFORFILE);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHOLDPATH){
                packageBody.setLengthVariable(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READNAMEOLDPATH);
            }
            if(packageBody.getStatus() == PackageBody.Status.READLENGTHNEWPATH){
                packageBody.setLengthPasteCatalog(buf.readInt());
                packageBody.setStatus(PackageBody.Status.READNAMENEWPATH);
            }

        }
        //пересылаем сообщение следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
