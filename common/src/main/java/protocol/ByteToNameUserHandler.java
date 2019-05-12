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

    private PackageBody packageBody;

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
            //в пакете присваиваем новое имя пользователя
            //packageBody.setNameUser(new String(data));
            HashMap<Client,String> idValue = ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get();
            idValue.put(Client.LOGIN,new String(data));
            if(packageBody.getCommand() == ProtocolCommand.AUTHORIZATION ||
                    packageBody.getCommand() == ProtocolCommand.REGISTRATION)
            {
                packageBody.setStatus(PackageBody.Status.READPASSWORD);
            } else {
                //присваиваем статус: чтение длины имени файла
                //packageBody.setStatus(PackageBody.Status.READLENGTHNAMEFILE);
                //System.out.println(3);
            }
        }
        //отправляем сообщение к следующему ChannelHandler
        ctx.fireChannelRead(msg);
    }
}
