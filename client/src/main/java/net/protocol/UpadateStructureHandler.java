package net.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.AbstractHandler;
import utility.Packages;

public class UpadateStructureHandler extends AbstractHandler {

    private PackageBody packageBody;

    public UpadateStructureHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(packageBody.getCommand() == ProtocolCommand.UPDATESTRUCTURE &&
                packageBody.getStatus() == PackageBody.Status.CHANGEDSTRUCTURE) {

            System.out.println("structure is changed");
            Packages.requestDirectoryStructure(ctx.channel());
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}