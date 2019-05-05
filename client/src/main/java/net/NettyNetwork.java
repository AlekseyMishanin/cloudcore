package net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.NonNull;
import model.PackageTransport;
import model.PackageBody;
import net.protocol.BuildStructureCatalogHandler;
import net.protocol.ByteToBoolResponseClientHandler;
import net.protocol.UpadateStructureHandler;
import protocol.*;
import utility.Packages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class NettyNetwork {
    private static NettyNetwork ourInstance = new NettyNetwork();

    public static NettyNetwork getInstance() {
        return ourInstance;
    }

    private NettyNetwork() {

        countDownLatch = new CountDownLatch(1);
        packageBody = new PackageBody();
    }

    private Channel currentChannel;
    private CountDownLatch countDownLatch;  //защелка, которая ожидает наступления события соединения с сервером.
    private PackageBody packageBody;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap clientBootstrap = new Bootstrap();
                    clientBootstrap.group(group);
                    clientBootstrap.channel(NioSocketChannel.class);
                    clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 8189));
                    clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast("command",new CommandHandler(packageBody))
                                    .addLast("updateStructure", new UpadateStructureHandler(packageBody))
                                    .addLast("verifyAuthRegResponse", new ByteToBoolResponseClientHandler(packageBody))
                                    .addLast("lengthUserNameOrOuther",new ToIntegerDecoder(packageBody))
                                    .addLast("getVriable",new ByteToNameVariableHandler(packageBody))
                                    .addLast("userName",new ByteToNameUserHandler(packageBody))
                                    .addLast("getListCatalog", new StructureCatalogClientHandler(packageBody))
                                    .addLast("buildStructureCloud", new BuildStructureCatalogHandler(packageBody))
                                    .addLast("lengthFileName",new ToIntegerDecoder(packageBody))
                                    .addLast("fileName",new ByteToNameFileHandler(packageBody))
                                    .addLast("lengthFile",new ToLongDecoder(packageBody))
                                    .addLast("loadfile",new ByteToFileClientHandler(packageBody));
                            currentChannel = socketChannel;
                        }
                    });
                    ChannelFuture channelFuture = clientBootstrap.connect().sync();
                    countDownLatch.countDown();     //снимаем защелку, т.к. произошло событие соединения с сервером
                    channelFuture.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        group.shutdownGracefully().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Метод загрузки файла с сервера на компьютер клиента
     *
     * @param   pathToDownload
     *          путь к файлу на стороне сервера
     * */
    public void loadData(Path pathToDownload, String pathToload){
        try {
            //проверяем наступило ли событие успешного соединения с сервером
            countDownLatch.await();
            //вызываем статический метод загрузки файла
            Packages.loadFromServerToClient(currentChannel, pathToDownload, pathToload);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод загрузки файла на сервер с компьютера клиента
     *
     * @param   path
     *          путь к файлу на стороне клиента
     * */
    public void sendData(String cloudCatalog, Path path) throws InterruptedException {
        try {
            //проверяем наступило ли событие успешного соединения с сервером
            countDownLatch.await();
            //вызываем статический метод для отправки файла
            Packages.sendFromClienToServer(currentChannel, cloudCatalog ,path);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryAuthorization(@NonNull String login, int password){
        Packages.authorization(currentChannel,login, password);
    }

    public void tryRegistration(@NonNull String login, int password){
        Packages.registration(currentChannel,login, password);
    }

    public void requestDirectoryStructure(){
        try {
            Packages.requestDirectoryStructure(currentChannel);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestCteareNewCatalog(String path){
        try {
            Packages.requestCteareNewCatalog(currentChannel, path);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestDeleteCatalog(String catalog){
        try {
            Packages.requestDeleteCatalog(currentChannel, catalog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestCopyCatalog(Path oldCatalog, String newCatalog){
        try {
            Packages.requestCopyCatalog(currentChannel, oldCatalog, newCatalog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestCutCatalog(Path oldCatalog, String newCatalog){
        try {
            Packages.requestCutCatalog(currentChannel, oldCatalog, newCatalog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void requestRenameCatalog(Path oldCatalog, String newCatalog){
        try {
            Packages.requestRenameCatalog(currentChannel, oldCatalog, newCatalog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
