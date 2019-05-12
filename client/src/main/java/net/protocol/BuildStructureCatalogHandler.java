package net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.AbstractHandler;
import utility.ListController;

public class BuildStructureCatalogHandler extends AbstractHandler {

    private PackageBody packageBody;

    public BuildStructureCatalogHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if(packageBody.getCommand() == ProtocolCommand.STRUCTURERESPONSE &&
                packageBody.getStatus() == PackageBody.Status.BUILDSTRUCTURE) {
            //если кол-во байт доступных для чтения меньше длины имени файла
            //нужно протестировать
            ListController.getInstance().getOperatingPanelController().updateTreeViewCloud(packageBody.getStructureCatalog());

            ReferenceCountUtil.release(msg);
            //очищаем пакет
            packageBody.clear();
        } else {
            //отправляем сообщение к следующему ChannelHandler
            ctx.fireChannelRead(msg);
        }
    }
}
