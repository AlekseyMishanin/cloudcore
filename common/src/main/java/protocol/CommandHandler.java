package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import model.PackageBody;
import model.ProtocolCommand;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение команды.
 *
 * @author Mishanin Aleksey
 * */
public class CommandHandler extends AbstractHandler {

    private PackageBody packageBody;

    public CommandHandler(PackageBody packageBody) {
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
            switch (ProtocolCommand.getCommand(firstByte)){
                case AUTHRESPONSE:
                case REGRESPONSE:
                    packageBody.setStatus(PackageBody.Status.READBOOLRESPONSE);
                    break;
                case STRUCTUREREQUEST:
                    packageBody.setStatus(PackageBody.Status.BUILDSTRUCTURECATALOG);
                    break;
                case STRUCTURERESPONSE:
                    packageBody.setStatus(PackageBody.Status.READLENGTHSTRUCTURE);
                    break;
                case NEWCATALOG:
                    packageBody.setStatus(PackageBody.Status.READLENGTHPATHCATALOG);
                    break;
                case UPDATESTRUCTURE:
                    packageBody.setStatus(PackageBody.Status.CHANGEDSTRUCTURE);
                    break;
                case DELETECATALOG:
                    packageBody.setStatus(PackageBody.Status.READLENGTHDELETECATALOG);
                    break;
                case FILE:
                case FILERESPONSE:
                case FILEREQUEST:
                    System.out.println(1);
                    packageBody.setStatus(PackageBody.Status.READLENGTHCATALOGFORFILE);
                    break;
                case COPYCATALOG:
                case CUTCATALOG:
                case RENAMECATALOG:
                    packageBody.setStatus(PackageBody.Status.READLENGTHOLDPATH);
                    break;
                default:
                    packageBody.setStatus(PackageBody.Status.READLENGTHUSER);
            }
            //System.out.println(1);
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
