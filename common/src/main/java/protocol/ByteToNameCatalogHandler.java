package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

public class ByteToNameCatalogHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToNameCatalogHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if((packageBody.getCommand() == ProtocolCommand.NEWCATALOG) &&
                packageBody.getStatus() == PackageBody.Status.READPATHNEWCATALOG) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины имени файла
            if (buf.readableBytes() < packageBody.getLengthVariable()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер под имя файла
            byte[] data = new byte[packageBody.getLengthVariable()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //в пакете присваиваем новое имя файла
            packageBody.setVariable(new String(data));
            //packageBody.setStatus(PackageBody.Status.CREATENEWCATALOG);
            packageBody.setStatus(PackageBody.Status.CREATENEWCATALOG);
        }
        ctx.fireChannelRead(msg);
    }
}
