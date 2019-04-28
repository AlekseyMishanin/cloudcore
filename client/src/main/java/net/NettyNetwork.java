package net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import model.PackageTransport;
import model.PackageBody;
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
    }

    private Channel currentChannel;
    private CountDownLatch countDownLatch;  //защелка, которая ожидает наступления события соединения с сервером.

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
                            PackageBody packageBody = new PackageBody();
                            socketChannel.pipeline()
                                    .addLast("command",new CommandHandler(packageBody))
                                    .addLast("lengthUserName",new ToIntegerDecoder(packageBody))
                                    .addLast("userName",new ByteToNameUserHandler(packageBody))
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

    public void loadData(String name, Path path){
        try {
            countDownLatch.await();  //проверяем наступило ли событие успешного соединения с сервером
            Packages.loadFromServerToClient(currentChannel, new PackageTransport("test",path));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String name, Path path) throws InterruptedException {
        try {
            countDownLatch.await();  //проверяем наступило ли событие успешного соединения с сервером
            Packages.sendFromClienToServer(currentChannel, new PackageTransport("test",path));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
