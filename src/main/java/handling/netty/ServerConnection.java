package handling.netty;

import constants.ServerConstants;
import handling.ServerType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.console.Start;

public class ServerConnection {
    private static final Logger log = LogManager.getLogger(Start.class.getName());

    private final int port;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1); //The initial connection thread where all the new connections go to
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(); //Once the connection thread has finished it will be moved over to this group where the thread will be managed
    private final ServerType type;
    private int world = -1;
    private int channels = -1;
    private ServerBootstrap boot;
    private Channel channel;

    public ServerConnection(int port, int world, int channels, ServerType type) {
        this.port = port;
        this.world = world;
        this.channels = channels;
        this.type = type;
    }

    public void run() {
        try {
            boot = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, ServerConstants.MAXIMUM_CONNECTIONS)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ServerInitializer(world, channels, type));
            channel = boot.bind(port).sync().channel().closeFuture().channel();
            log.info("正在启动 - {} 端口: {}", type.name(), port);
        } catch (Exception e) {
            throw new RuntimeException("启动失败 - " + type.name() + ":" + channel.remoteAddress());
        }
    }

    public void close() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
