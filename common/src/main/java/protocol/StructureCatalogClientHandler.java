package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

public class StructureCatalogClientHandler extends AbstractHandler {

    private PackageBody packageBody;

    public StructureCatalogClientHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if(packageBody.getCommand() == ProtocolCommand.STRUCTURERESPONSE &&
                packageBody.getStatus() == PackageBody.Status.READSTRINGSTRUCTURE) {
            if(packageBody.getLengthStructure()!=0) {
                //если кол-во байт доступных для чтения меньше длины имени файла
                if (buf.readableBytes() < packageBody.getLengthStructure()) {
                    //прекращаем обработку
                    return;
                }
                //создаем временный буфер под имя пользователя
                byte[] data = new byte[packageBody.getLengthStructure()];
                //читаем имя файла во временный буфер
                buf.readBytes(data);
                packageBody.setStructureCatalog(new String(data));
            }
            packageBody.setStatus(PackageBody.Status.BUILDSTRUCTURE);
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
