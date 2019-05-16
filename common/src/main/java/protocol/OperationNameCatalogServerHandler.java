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

import java.io.File;
import java.util.HashMap;

/**
 * Класс инкапсулирует часть протокола, отвечающую за выполение операции с каталогами.
 *
 * @author Mishanin Aleksey
 * */
public class OperationNameCatalogServerHandler extends AbstractHandler {

    private PackageBody packageBody;

    public OperationNameCatalogServerHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.COPYCATALOG ||
                packageBody.getCommand() == ProtocolCommand.CUTCATALOG ||
                packageBody.getCommand() == ProtocolCommand.RENAMECATALOG) &&
                packageBody.getStatus() == PackageBody.Status.OPERATIONNAMEPATH) {
            //старое имя каталога
            String oldPath = packageBody.getVariable();
            //новое имя каталога
            String newPath = packageBody.getPasteCatalog();
            //если команда протокола = копировать
            if (packageBody.getCommand() == ProtocolCommand.COPYCATALOG){
                //отправляем sql-запрос в БД на добавление новой строки
                if(AuthService.getInstance().insertCopyPath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    //если строка успешно добавлена, уведомляем клиента о том, что структура каталогов обновилась
                    Packages.updateStructure(ctx.channel());
                }
            }
            //если команда протокола = вырезать
            if (packageBody.getCommand() == ProtocolCommand.CUTCATALOG){
                //отправляем sql-запрос в БД на добавление новой строки и на удаление строки с устаревшим наименованием каталога
                if(SqlService.getInstance().insertCopyPath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))) &&
                   SqlService.getInstance().deleteOldPath(
                        oldPath,
                           Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    //если операции успешно выполнены, уведомляем клиента о том, что структура каталогов обновилась
                    Packages.updateStructure(ctx.channel());
                }
            }
            //если команда протокола = переименовать
            if (packageBody.getCommand() == ProtocolCommand.RENAMECATALOG){
                //отправляем sql-запрос в БД на обновление строки
                if(SqlService.getInstance().renamePath(
                        oldPath,
                        newPath,
                        File.separator,
                        Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID))
                )){
                    //если операция успешно выполнена, уведомляем клиента о том, что структура каталогов обновилась
                    Packages.updateStructure(ctx.channel());
                }
            }
            ReferenceCountUtil.release(msg);
            packageBody.clear();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
