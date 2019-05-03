package protocol;

import db.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

public class StructureCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public StructureCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(packageBody.getCommand() == ProtocolCommand.STRUCTUREREQUEST &&
                packageBody.getStatus() == PackageBody.Status.BUILDSTRUCTURECATALOG) {
            String str = AuthService.getInstance().buildStructureCatalog(packageBody.getIdClient());
            System.out.println(str);
            Packages.responseDirectoryStructure(ctx.channel(),str);
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
