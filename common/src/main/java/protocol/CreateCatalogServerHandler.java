package protocol;

import db.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

public class CreateCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public CreateCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if((packageBody.getCommand() == ProtocolCommand.NEWCATALOG) &&
                packageBody.getStatus() == PackageBody.Status.CREATENEWCATALOG) {

            String newCatalog = packageBody.getVariable();
            String parentCatalog = newCatalog.indexOf(packageBody.getSystemSeparator()) != -1 ?
                    newCatalog.substring(0, newCatalog.lastIndexOf(packageBody.getSystemSeparator())) : null;
            if(AuthService.getInstance().insertNewCatalog(newCatalog, parentCatalog, packageBody.getIdClient())){
                Packages.updateStructure(ctx.channel());
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
