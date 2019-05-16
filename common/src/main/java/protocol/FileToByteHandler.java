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
 * Класс инкапсулирует часть протокола, отвечающую за оптарвку файла от сервера клиенту.
 *
 * @author Mishanin Aleksey
 * */
public class FileToByteHandler extends AbstractHandler{

    private PackageBody packageBody;        //ссылка на объект протокола

    public FileToByteHandler(PackageBody packageBody) {
        this.packageBody = packageBody;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если пакет содержит нужную команду и статус
        if((packageBody.getCommand() == ProtocolCommand.FILEREQUEST) &&
                packageBody.getStatus() == PackageBody.Status.WRITEFILE) {
            //освобождаем сообдение
            ReferenceCountUtil.release(msg);
            //запускаем задачу в демоне, чтобы избедать взаимной блокировки
            new RecursiveAction(){
                @Override
                protected void compute() {
                    //определяем путь до каталога на стороне клиента, где будет сохранен файл
                    String catalogClient = packageBody.getVariable();
                    //определяем имя файла
                    String pathToFile = packageBody.getNameFile();
                    //получаем ссылку на канал
                    Channel channel = ctx.channel();
                    //строим путь ко временной папке клиента на сервере
                    Path path = Paths.get("ServerCloud/" + ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.LOGIN));
                    ReadableByteChannel inChannel = null;
                    FileChannel outChannel = null;
                    try {
                        //если временная папка не существует, создаем ее
                        if(!Files.exists(path)){ Files.createDirectory(path);}
                        //вытягиваем из БД длину файла при помощи sql
                        long size = SqlService.getInstance().selectSizeFile(pathToFile,
                                PackageBody.systemSeparator,
                                Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)));
                        //если длина файла = 0 прерываем обработку
                        if(size == 0) return;
                        //открываем байтовый поток к файлу сохраненному в БД
                        InputStream in = SqlService.getInstance().selectFile(pathToFile,
                                PackageBody.systemSeparator,
                                Integer.parseInt(ctx.channel().attr(AttributeKey.<HashMap<Client,String>>valueOf(CLIENTCONFIG)).get().get(Client.ID)));
                        //если поток не удалось открыть, прерываем обработку
                        if(in == null) return;
                        //открываем канал для байтового потока с файлом
                        inChannel = Channels.newChannel(in);
                        //откываем канал к файлу во временной папке клиента на сервере
                        outChannel = new FileOutputStream(
                                path.toAbsolutePath().toString() +
                                        pathToFile.substring(pathToFile.lastIndexOf(PackageBody.systemSeparator))
                        ).getChannel();
                        //перенаправляем байты из канала открытого для БД в канал открытый для временного файла
                        outChannel.transferFrom(inChannel,0,size);
                        //отправляем файл клиенту. Файл считывается из временной папки клиента на сервере
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
