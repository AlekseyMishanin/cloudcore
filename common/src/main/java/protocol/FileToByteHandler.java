package protocol;

import db.AuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.util.ReferenceCountUtil;
import model.PackageTransport;
import model.PackageBody;
import model.ProtocolCommand;
import utility.Packages;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    //System.out.println(7);
                    String catalogClient = packageBody.getVariable();
                    String pathToFile = packageBody.getNameFile();
                    Channel channel = ctx.channel();
                    Path path = Paths.get("ServerCloud/" + packageBody.getNameUser());
                    ReadableByteChannel inChannel = null;
                    FileChannel outChannel = null;
                    System.out.println(6);
                    try {
                        if(!Files.exists(path)){ Files.createDirectory(path);}
                        long size = AuthService.getInstance().selectSizeFile(pathToFile, PackageBody.systemSeparator,packageBody.getIdClient());
                        System.out.println(size);
                        if(size == 0) return;
                        InputStream in = AuthService.getInstance().selectFile(pathToFile, PackageBody.systemSeparator,packageBody.getIdClient());
                        System.out.println(in == null);
                        if(in == null) return;
                        inChannel = Channels.newChannel(in);

                        outChannel = new FileOutputStream(
                                path.toAbsolutePath().toString() +
                                        pathToFile.substring(pathToFile.lastIndexOf(PackageBody.systemSeparator))
                        ).getChannel();
                        outChannel.transferFrom(inChannel,0,size);
                        Packages.sendFromServerToClient(
                                channel,
                                catalogClient+pathToFile.substring(pathToFile.lastIndexOf(PackageBody.systemSeparator)),
                                        Paths.get(path.toAbsolutePath().toString() +
                                                pathToFile.substring(pathToFile.lastIndexOf(PackageBody.systemSeparator))));


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            inChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        packageBody.clear();
                    }
                }
            }.fork();
        } else {
            //пересылаем сообдение следующему ChannelHandler
            ctx.fireChannelRead(msg);
        }
    }
}
