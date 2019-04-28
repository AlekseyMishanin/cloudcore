package protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import model.PackageTransport;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

import java.nio.file.Paths;

/**
 * Класс инкапсулирует часть протокола, отвечающую запись файла в канал со стороны сервера.
 *
 * @author Mishanin Aleksey
 * */
public class FileToByteHandler extends AbstractHandler{

    private PackageBody packageBody;

    public FileToByteHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.FILEREQUEST) &&
                packageBody.getStatus() == PackageBody.Status.WRITEFILE) {
            //System.out.println(9);
            //вызываем статический метод утилиты для оптавки файла клиенту
            Packages.sendFromServerToClient(ctx.channel(),new PackageTransport(packageBody.getNameUser(), Paths.get("serverA/", packageBody.getNameUser() + "/", packageBody.getNameFile())));
            //освобождаем сообщение
            ReferenceCountUtil.release(msg);
            //очищаем пакет
            packageBody.clear();
        } else {
            //пересылаем сообдение следующему ChannelHandler
            ctx.fireChannelRead(msg);
        }
    }
}
