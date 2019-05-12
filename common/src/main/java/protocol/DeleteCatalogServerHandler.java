package protocol;

import db.SqlService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.util.HashMap;

public class DeleteCatalogServerHandler extends AbstractHandler{

    private PackageBody packageBody;

    public DeleteCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(packageBody.getCommand() == ProtocolCommand.DELETECATALOG &&
                packageBody.getStatus() == PackageBody.Status.DELETECATALOG) {

            if(SqlService.getInstance().deleteCatalog(packageBody.getVariable(),
                    Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)))){
                Packages.updateStructure(ctx.channel());
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
