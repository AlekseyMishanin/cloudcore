package protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import protocol.attribute.PoolConstantName;

/**
 * Абстрактный класс наследника ChannelHandler. Переопределяет метод exceptionCaught
 * */
public abstract class AbstractHandler extends ChannelInboundHandlerAdapter implements PoolConstantName {

    private final Logger logger = Logger.getLogger(AbstractHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error(cause.getMessage());
        ctx.close();
    }
}
