package server;

import db.SqlService;
import db.arhive.AuthService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import model.PackageBody;
import protocol.*;
import protocol.attribute.Client;
import protocol.attribute.PoolConstantName;
import utility.Packages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerNetty implements PoolConstantName {

    //счетчик подключений
    private static AtomicInteger countChannel = new AtomicInteger();
    //список активных пользовательских соединений
    private final ConcurrentHashMap<Integer, UserChannel> listUserChannel = new ConcurrentHashMap<>();
    //таймер для запуска задания наблюдения за пользователями
    private final Timer timer = new Timer(true);
    //задание наблюдения
    private FileTimerTask fileTimerTask;

    public ServerNetty() {
        fileTimerTask  = new FileTimerTask();
        //задание срабатывает каждую секунду
        timer.schedule(fileTimerTask, 1000, 1000);
    }

    public void run() throws Exception{
        SqlService.getInstance().start();
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            final SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            //создаем мапу для сохранения атрибутов канала
            final AttributeKey<Map<Client,String>> id = AttributeKey.newInstance(CLIENTCONFIG);
            ServerBootstrap startSetting = new ServerBootstrap();
            startSetting.group(mainGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            PackageBody packageBody = new PackageBody();
                            if (sslCtx != null) {
                                socketChannel.pipeline().addLast(sslCtx.newHandler(socketChannel.alloc()));
                            }
                            socketChannel.pipeline()
                                .addLast("command",new CommandServerHandler(packageBody))
                                .addLast("getListCatalog",new StructureCatalogServerHandler(packageBody))
                                .addLast("lengthFirst",new ToIntegerDecoder(packageBody))
                                .addLast("userName",new ByteToNameUserHandler(packageBody))
                                .addLast("getNameNewCatalog", new ByteToNameVariableHandler(packageBody))
                                .addLast("deleteCatalog", new DeleteCatalogServerHandler(packageBody))
                                .addLast("verifyRassword",new ByteToPasswordServerHandler(packageBody))
                                .addLast("lengthSecond",new ToIntegerDecoder(packageBody))
                                .addLast("getNewPathToCutOrCopy", new ByteToPathOperationCatalogHandler(packageBody))
                                .addLast("cutOrCopyPathToStructure", new OperationNameCatalogServerHandler(packageBody))
                                .addLast("operationWithCatalog", new CreateCatalogServerHandler(packageBody))
                                .addLast("fileName",new ByteToNameFileHandler(packageBody))
                                .addLast("lengthFile",new ToLongDecoder(packageBody))
                                .addLast("chunkedWriter", new ChunkedWriteHandler())
                                .addLast("loadfile",new ByteToFileServerHandler(packageBody))
                                .addLast("sendfile", new FileToByteHandler(packageBody));

                            //добавляем канал пользователя в список для наблюдения
                            listUserChannel.put(countChannel.incrementAndGet(),new UserChannel(packageBody,socketChannel));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //разрешаем на принятых каналах применять атрибуты
            startSetting.childAttr(id, new HashMap<Client,String>());
            ChannelFuture future = startSetting.bind(8189).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        ServerNetty server = new ServerNetty();
        server.run();
    }

    /**
     * Класс инкапсулирует объект за состоянием которого наблюдает сервер.
     * */
    private class UserChannel{

        private PackageBody packageBody;    //объект протокола
        private Channel channel;            //ссылка на канал
        private long lengthFile;            //длина файла

        public UserChannel(PackageBody packageBody, Channel channel) {
            this.packageBody = packageBody;
            this.channel = channel;
        }

        public void setLengthFile(long lengthFile) {
            this.lengthFile = lengthFile;
        }

        public boolean isLengthEquals(){
            return lengthFile == packageBody.getLenghFile();
        }
    }

    /**
     * Класс задания проверяет изменилась ли длина файла в пакете за определенное время. Если не изменилась полагаем, что
     * произошел сбой при передаче данных.
     * */
    private class FileTimerTask extends TimerTask {

        @Override
        public void run() {
            listUserChannel.forEach((id, user) -> {
                //если канал не активен
                if (!user.channel.isActive()) {
                    //удаляем объект из списка наблюдаемых объектов
                    listUserChannel.remove(id);
                } else {
                    //если состояние "чтение файла" и если длина файла из протокола равна длине файла которая была в предыдущем временном периоде
                    if (user.packageBody.getStatus() == PackageBody.Status.READFILE && user.isLengthEquals()) {
                        //очищаем объект протокола
                        user.packageBody.clear();
                        //закрываем в хандлере байтовый поток для записи файла
                        user.channel.pipeline().get(ByteToFileServerHandler.class).closeileOutputStream();
                        try {
                            //отрпалвяем пользователю сообщение о сбое
                            Packages.fileError(user.channel);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (user.packageBody.getStatus() == PackageBody.Status.READFILE) {
                        //запоминаем длину файла из объекта протокола
                        user.setLengthFile(user.packageBody.getLenghFile());
                    } else {
                        //сбрасываем значение временной переменной под длину файла
                        user.setLengthFile(-1);
                    }
                }
            });
        }
    }
}
