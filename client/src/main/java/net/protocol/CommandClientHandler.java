package net.protocol;

import dialog.StaticAlert;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.AbstractHandler;

/**
 * Класс инкапсулирует часть протокола, отвечающую за работу с командным байтом
 *
 * @author Mishanin Aleksey
 * */
public class CommandClientHandler extends AbstractHandler {
    private PackageBody packageBody;

    public CommandClientHandler(PackageBody packageBody) {
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
                case AUTHRESPONSE:
                case REGRESPONSE:
                    packageBody.setStatus(PackageBody.Status.READBOOLRESPONSE);
                    break;
                case STRUCTURERESPONSE:
                    packageBody.setStatus(PackageBody.Status.READLENGTHSTRUCTURE);
                    break;
                case UPDATESTRUCTURE:
                    packageBody.setStatus(PackageBody.Status.CHANGEDSTRUCTURE);
                    break;
                case FILERESPONSE:
                    packageBody.setStatus(PackageBody.Status.READLENGTHCATALOGFORFILE);
                    break;
                case DENIED:
                    //если в выполенении операции было отказано, выводим клиенту алерт с сообщением
                    StaticAlert.deniedOperation();
                    ReferenceCountUtil.release(msg);
                    packageBody.clear();
                    break;
                default:
                    break;
            }
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
