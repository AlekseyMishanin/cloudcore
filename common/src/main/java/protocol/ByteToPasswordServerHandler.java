package protocol;

import db.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

public class ByteToPasswordServerHandler extends AbstractHandler {

    private PackageBody packageBody;

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
//                System.out.println("auth");
                //отпралвяем в БД sql-запрос на получение ID пользователя
                int ID = AuthService.getInstance().verifyLoginAndPass(packageBody.getNameUser(),pass);
                //присваиваем булевой переменной true если пользователь найден в БД, иначе false
                boolean bool = ID == -1 ? false : true;
                //если пользователь существует в БД
                if(bool){
                    //записываем ID пользователя
                    packageBody.setIdClient(ID);
                    //присваиваем признак того, что пользователь авторизован
                    packageBody.setCurrentUser(true);
                }
                //отправляем ответ клиенту
                Packages.sendAuthorizationResponse(ctx.channel(),bool);
            }
            //если статус протокола: запрос регистрации
            if(packageBody.getCommand() == ProtocolCommand.REGISTRATION){
//                System.out.println("reg");
                //отправляем sql-запрос на добавление записи в БД
                boolean bool = AuthService.getInstance().accountRegistration(packageBody.getNameUser(),pass);
                //отправляем ответ клиенту
                Packages.sendRegistrationResponse(ctx.channel(),bool);
            }
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
