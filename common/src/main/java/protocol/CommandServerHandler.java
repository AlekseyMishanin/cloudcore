package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.util.HashMap;

public class CommandServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public CommandServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет не содержит команды или содержит ошибку
        if((packageBody.getCommand() == ProtocolCommand.FILEERROR ||
                packageBody.getCommand() == null) &&
                packageBody.getStatus() == PackageBody.Status.NONE){
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //преобразуем ByteBuf в byte
            byte firstByte = buf.readByte();
            //на основании значения байта определяем ProtocolCommand
            packageBody.setCommand(ProtocolCommand.getCommand(firstByte));
            //на основании значения ProtocolCommand определяем статус протокола
            switch (ProtocolCommand.getCommand(firstByte)){
                case AUTHORIZATION:
                case REGISTRATION:
                    packageBody.setStatus(PackageBody.Status.READLENGTHUSER);
                    break;
                default:
                    if(checkIdUser(ctx)){
                        switch (ProtocolCommand.getCommand(firstByte)){
                            case STRUCTUREREQUEST:
                                packageBody.setStatus(PackageBody.Status.BUILDSTRUCTURECATALOG);
                                break;
                            case NEWCATALOG:
                                packageBody.setStatus(PackageBody.Status.READLENGTHPATHCATALOG);
                                break;
                            case DELETECATALOG:
                                packageBody.setStatus(PackageBody.Status.READLENGTHDELETECATALOG);
                                break;
                            case FILE:
                            case FILEREQUEST:
                                packageBody.setStatus(PackageBody.Status.READLENGTHCATALOGFORFILE);
                                break;
                            case COPYCATALOG:
                            case CUTCATALOG:
                            case RENAMECATALOG:
                                packageBody.setStatus(PackageBody.Status.READLENGTHOLDPATH);
                                break;
                        }
                    } else {
                        Packages.deniedInAction(ctx.channel());
                        ReferenceCountUtil.release(msg);
                        packageBody.clear();
                    }
                    break;
            }
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }

    private boolean checkIdUser(ChannelHandlerContext ctx){
        return ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID) != null;
    }
}
