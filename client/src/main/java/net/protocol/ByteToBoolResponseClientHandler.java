package net.protocol;

import dialog.StaticAlert;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import javafx.application.Platform;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.AbstractHandler;
import utility.ListController;

/**
 * Класс инкапсулирует часть протокола, отвечающую за обработку ответа сервера на операцию авторизации/регистрации
 *
 * @author Mishanin Aleksey
 * */
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
                if(ListController.getInstance().getAuthorisationController() != null) {
                    //через список контроллеров, передаем результат авторизации в метод контроллера
                    ListController.getInstance().getAuthorisationController().resultAuthorisation(bool);
                } else if (bool && ListController.getInstance().getOperatingPanelController() != null) {
                    //через список контроллеров, передаем результат авторизации в метод контроллера
                    ListController.getInstance().getOperatingPanelController().alertAboutCreateConnection();
                }
            }
            if(packageBody.getCommand()==ProtocolCommand.REGRESPONSE){
                if(ListController.getInstance().getAuthorisationController() != null) {
                    //через список контроллеров, передаем результат регистрации в метод контроллера
                    ListController.getInstance().getAuthorisationController().resultRegistration(bool);
                }
            }
            ReferenceCountUtil.release(msg);
            //очищаем пакет
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
