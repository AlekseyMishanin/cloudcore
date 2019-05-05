package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

public class ByteToPathOperationCatalogHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToPathOperationCatalogHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.COPYCATALOG ||
                packageBody.getCommand() != ProtocolCommand.CUTCATALOG ||
                packageBody.getCommand() != ProtocolCommand.RENAMECATALOG ) &&
                packageBody.getStatus() == PackageBody.Status.READNAMENEWPATH) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины имени файла
            if (buf.readableBytes() < packageBody.getLengthPasteCatalog()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер под имя пользователя
            byte[] data = new byte[packageBody.getLengthPasteCatalog()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //в пакете присваиваем новое имя пользователя
            packageBody.setPasteCatalog(new String(data));
            packageBody.setStatus(PackageBody.Status.OPERATIONNAMEPATH);
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
