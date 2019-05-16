package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;

import java.util.HashMap;

/**
 * Класс инкапсулирует часть протокола, отвечающую за чтение имени пользователя.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToNameUserHandler extends AbstractHandler {

    private PackageBody packageBody;    //ссылка на объект протокола

    public ByteToNameUserHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() != ProtocolCommand.FILEERROR ||
                packageBody.getCommand() != null) &&
                packageBody.getStatus() == PackageBody.Status.READNAMEUSER) {
            //преобразуем Object к ByteBuf
            ByteBuf buf = ((ByteBuf) msg);
            //если кол-во байт доступных для чтения меньше длины имени файла
            if (buf.readableBytes() < packageBody.getLenghUserName()) {
                //прекращаем обработку
                return;
            }
            //создаем временный буфер под имя пользователя
            byte[] data = new byte[packageBody.getLenghUserName()];
            //читаем имя файла во временный буфер
            buf.readBytes(data);
            //получаем из канала ссылку на объект атрибутов.
            HashMap<Client,String> idValue = ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get();
            //добавляем в объект атрибутов логин пользователя
            idValue.put(Client.LOGIN,new String(data));
            //если пришла команда на авторизацию или регистрацию
            if(packageBody.getCommand() == ProtocolCommand.AUTHORIZATION ||
                    packageBody.getCommand() == ProtocolCommand.REGISTRATION)
            {
                //меняем статус протокола на чтение пароля
                packageBody.setStatus(PackageBody.Status.READPASSWORD);
            }
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
