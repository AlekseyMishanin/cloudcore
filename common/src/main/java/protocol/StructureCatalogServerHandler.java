package protocol;

import db.SqlService;
import db.arhive.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.util.HashMap;

/**
 * Класс инкапсулирует часть протокола, отвечающую построение структуры каталогов на стороне сервера для отправки клиенту.
 *
 * @author Mishanin Aleksey
 * */
public class StructureCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public StructureCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если протокол содержит нужную комануд и нужный статус
        if(packageBody.getCommand() == ProtocolCommand.STRUCTUREREQUEST &&
                packageBody.getStatus() == PackageBody.Status.BUILDSTRUCTURECATALOG) {
            //получаем ссылку на атрибуты канала
            HashMap<Client,String> idValue = ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get();
            //отправляем sql-запрос к БД на получение строки со структурой каталогов
            String str = SqlService.getInstance().buildStructureCatalog(Integer.parseInt(idValue.get(Client.ID)));
            //оправляем клиенту строку со структурой каталогов
            Packages.responseDirectoryStructure(ctx.channel(),str);
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
