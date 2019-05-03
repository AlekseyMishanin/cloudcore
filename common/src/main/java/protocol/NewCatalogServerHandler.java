package protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;

public class NewCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public NewCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if((packageBody.getCommand() == ProtocolCommand.NEWCATALOG) &&
                packageBody.getStatus() == PackageBody.Status.CREATENEWCATALOG) {


            System.out.println(packageBody.getVariable());


            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
