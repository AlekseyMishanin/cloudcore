package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.ReferenceCountUtil;
import model.PackageTransport;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.RecursiveAction;


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
            ReferenceCountUtil.release(msg);
            new RecursiveAction(){
                @Override
                protected void compute() {
                    try {
                        //вызываем статический метод утилиты для оптавки файла клиенту
                        Packages.sendFromServerToClient(ctx.channel(), new PackageTransport(packageBody.getNameUser(), Paths.get("serverA/", packageBody.getNameUser() + "/", packageBody.getNameFile())));
                        //очищаем пакет
                        packageBody.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.fork();
            //освобождаем сообщение

        } else {
            //пересылаем сообдение следующему ChannelHandler
            ctx.fireChannelRead(msg);
        }
    }
}
