package protocol;

import db.SqlService;
import db.arhive.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс инкапсулирует часть протокола, чтение пароля.
 *
 * @author Mishanin Aleksey
 * */
public class ByteToPasswordServerHandler extends AbstractHandler {

    private PackageBody packageBody;        //ссылка на объект протокола

    public ByteToPasswordServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //преобразуем Object к ByteBuf
        ByteBuf buf = (ByteBuf)msg;
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.AUTHORIZATION ||
                packageBody.getCommand() == ProtocolCommand.REGISTRATION) &&
                packageBody.getStatus() == PackageBody.Status.READPASSWORD) {
            //если для чтения доступно менее 4-х байт
            if (buf.readableBytes() < 4) {
                //прекращаем обарботку
                return;
            }
            //считываем из потока в локальную переменную пароль
            final int pass = buf.readInt();
            //если статус протокола: запрос авторизации
            if(packageBody.getCommand() == ProtocolCommand.AUTHORIZATION){
                //отпралвяем в БД sql-запрос на получение ID пользователя
                int ID = SqlService.getInstance().verifyLoginAndPass(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN),pass);
                //присваиваем булевой переменной true если пользователь найден в БД, иначе false
                boolean bool = ID == -1 ? false : true;
                //если пользователь существует в БД
                if(bool){
                    //получаем ссылку на объект атрибутов канала
                    HashMap<Client,String> idValue = ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get();
                    //присваиваем объекту атрибутов id клиента
                    idValue.put(Client.ID,Integer.toString(ID));
                }
                //отправляем ответ клиенту об успешной авторизации
                Packages.sendAuthorizationResponse(ctx.channel(),bool);
            }
            //если статус протокола: запрос регистрации
            if(packageBody.getCommand() == ProtocolCommand.REGISTRATION){
                //отправляем sql-запрос на добавление записи в БД
                boolean bool = SqlService.getInstance().accountRegistration(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN),pass);
                //отправляем ответ клиенту об успешной (если bool=true) или не успешной (если bool=false) регистрации
                Packages.sendRegistrationResponse(ctx.channel(),bool);
            }
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            //очищаем объект протокола
            packageBody.clear();
        } else {
            //передаем сообщение следующему хандлеру
            ctx.fireChannelRead(msg);
        }
    }
}
