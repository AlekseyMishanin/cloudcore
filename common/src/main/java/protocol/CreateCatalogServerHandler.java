package protocol;

import db.SqlService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.util.HashMap;

/**
 * Класс инкапсулирует часть протокола, отвечающую за создание каталога на стороне сервера
 *
 * @author Mishanin Aleksey
 * */
public class CreateCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;        //ссылка на объект протокола

    public CreateCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если протокол содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.NEWCATALOG) &&
                packageBody.getStatus() == PackageBody.Status.CREATENEWCATALOG) {
            //вычитываем из протокола имя нового каталога
            String newCatalog = packageBody.getVariable();
            //определяем родительский каталог (в который вложен новый каталог)
            String parentCatalog = newCatalog.indexOf(packageBody.getSystemSeparator()) != -1 ?
                    newCatalog.substring(0, newCatalog.lastIndexOf(packageBody.getSystemSeparator())) : null;
            //отправляем sql-запрос к БД на добавление новой записи
            if(SqlService.getInstance().insertNewCatalog(newCatalog, parentCatalog,
                    Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)))){
                //если запись успешно добавлена, отправляем клиенту сообщение о том, что структура каталогов обновилась
                Packages.updateStructure(ctx.channel());
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
