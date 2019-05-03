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
                                    .addLast("verifyAuthRegResponse", new ByteToBoolResponseClientHandler(packageBody))
                                    .addLast("lengthUserNameOrOuther",new ToIntegerDecoder(packageBody))
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
     * @param   name
     *          логин клиента
     *
     * @param   path
     *          путь к файлу на стороне сервера
     * */
    public void loadData(String name, Path path){
        try {
            //проверяем наступило ли событие успешного соединения с сервером
            countDownLatch.await();
            //вызываем статический метод загрузки файла
            Packages.loadFromServerToClient(currentChannel, new PackageTransport("test",path));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод загрузки файла на сервер с компьютера клиента
     *
     * @param   name
     *          логин клиента
     *
     * @param   path
     *          путь к файлу на стороне клиента
     * */
    public void sendData(String name, Path path) throws InterruptedException {
        try {
            //проверяем наступило ли событие успешного соединения с сервером
            countDownLatch.await();
            //вызываем статический метод для отправки файла
            Packages.sendFromClienToServer(currentChannel, new PackageTransport("test",path));
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

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
