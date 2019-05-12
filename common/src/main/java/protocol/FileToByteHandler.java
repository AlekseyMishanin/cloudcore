package protocol;

import db.SqlService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import model.PackageBody;
import model.ProtocolCommand;
import protocol.attribute.Client;
import utility.Packages;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
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
            ReferenceCountUtil.release(msg);
            new RecursiveAction(){
                @Override
                protected void compute() {
                    String catalogClient = packageBody.getVariable();
                    String pathToFile = packageBody.getNameFile();
                    Channel channel = ctx.channel();
                    Path path = Paths.get("ServerCloud/" + ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN));
                    ReadableByteChannel inChannel = null;
                    FileChannel outChannel = null;
                    try {
                        if(!Files.exists(path)){ Files.createDirectory(path);}
                        long size = SqlService.getInstance().selectSizeFile(pathToFile,
                                PackageBody.systemSeparator,
                                Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)));
                        System.out.println(size);
                        if(size == 0) return;
                        InputStream in = SqlService.getInstance().selectFile(pathToFile,
                                PackageBody.systemSeparator,
                                Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)));
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
