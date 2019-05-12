package protocol;

import db.SqlService;
import db.arhive.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.io.File;
import java.util.HashMap;

public class OperationNameCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public OperationNameCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.COPYCATALOG ||
                packageBody.getCommand() == ProtocolCommand.CUTCATALOG ||
                packageBody.getCommand() == ProtocolCommand.RENAMECATALOG) &&
                packageBody.getStatus() == PackageBody.Status.OPERATIONNAMEPATH) {

            String oldPath = packageBody.getVariable();
            String newPath = packageBody.getPasteCatalog();
            if (packageBody.getCommand() == ProtocolCommand.COPYCATALOG){
                if(AuthService.getInstance().insertCopyPath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    Packages.updateStructure(ctx.channel());
                }
            }

            if (packageBody.getCommand() == ProtocolCommand.CUTCATALOG){
                if(SqlService.getInstance().insertCopyPath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))) &&
                   SqlService.getInstance().deleteOldPath(
                        oldPath,
                           Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    Packages.updateStructure(ctx.channel());
                }
            }
            if (packageBody.getCommand() == ProtocolCommand.RENAMECATALOG){
                if(SqlService.getInstance().renamePath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    Packages.updateStructure(ctx.channel());
                }
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
