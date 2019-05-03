package net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.AbstractHandler;
import utility.ListController;

public class ByteToBoolResponseClientHandler extends AbstractHandler {

    private PackageBody packageBody;

    public ByteToBoolResponseClientHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() !=null) &&
                packageBody.getStatus() == PackageBody.Status.READBOOLRESPONSE) {
            //если для чтения доступно менее 1-х байт
            if (buf.readableBytes() < 1) {
                //прекращаем обарботку
                return;
            }
            boolean bool = buf.readBoolean();
            if(packageBody.getCommand()==ProtocolCommand.AUTHRESPONSE){
//                System.out.println("auth");
                ListController.getInstance().getAuthorisationController().resultAuthorisation(bool);
            }
            if(packageBody.getCommand()==ProtocolCommand.REGRESPONSE){
//                System.out.println("reg");
                ListController.getInstance().getAuthorisationController().resultRegistration(bool);
            }
            ReferenceCountUtil.release(msg);
            //очищаем пакет
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
