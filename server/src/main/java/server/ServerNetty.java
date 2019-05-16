package server;

import db.arhive.AuthService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import model.PackageBody;
import protocol.*;
import protocol.attribute.Client;
import protocol.attribute.PoolConstantName;

import java.util.HashMap;
import java.util.Map;

public class ServerNetty implements PoolConstantName {

    public void run() throws Exception{
        AuthService.getInstance().start();
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //создаем мапу для сохранения атрибутов канала
            final AttributeKey<Map<Client,String>> id = AttributeKey.newInstance(CLIENTCONFIG);
            ServerBootstrap startSetting = new ServerBootstrap();
            startSetting.group(mainGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            PackageBody packageBody = new PackageBody();
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
                                    .addLast("loadfile",new ByteToFileServerHandler(packageBody))
                                    .addLast("sendfile", new FileToByteHandler(packageBody));
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
}
