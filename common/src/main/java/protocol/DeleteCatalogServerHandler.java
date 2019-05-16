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
 * Класс инкапсулирует часть протокола, отвечающую за удаление каталога на стороне сервера
 *
 * @author Mishanin Aleksey
 * */
public class DeleteCatalogServerHandler extends AbstractHandler{

    private PackageBody packageBody;        //ссылка на объект протокола

    public DeleteCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если протокол содержит нужную команду и статус
        if(packageBody.getCommand() == ProtocolCommand.DELETECATALOG &&
                packageBody.getStatus() == PackageBody.Status.DELETECATALOG) {
            //отправляем sql-запрос к БД на удаление строки из таблицы
            if(SqlService.getInstance().deleteCatalog(packageBody.getVariable(),
                    Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)))){
                //если строка успешно удалена, отправляем клиенту сообщение о том, что структура каталогов обновилась
                Packages.updateStructure(ctx.channel());
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
