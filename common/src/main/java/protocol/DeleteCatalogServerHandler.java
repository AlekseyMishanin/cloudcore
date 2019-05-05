package protocol;

import db.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

public class DeleteCatalogServerHandler extends AbstractHandler{

    private PackageBody packageBody;

    public DeleteCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(packageBody.getCommand() == ProtocolCommand.DELETECATALOG &&
                packageBody.getStatus() == PackageBody.Status.DELETECATALOG) {

            if(AuthService.getInstance().deleteCatalog(packageBody.getVariable(), packageBody.getIdClient())){
                Packages.updateStructure(ctx.channel());
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
