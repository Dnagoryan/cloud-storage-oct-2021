package com.geekbrains.network;


import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.navigation.FileMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Net {

    private static Net INSTANCE;
    private SocketChannel netChannel;
    private Callback callback;
    private ChannelFuture future;
    private int PORT = 8190;
    private String HOST = "localhost";

    private Net(Callback callback) {
        this.callback = callback;
        Thread thread = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .group(worker)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                netChannel = ch;
                                ch.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ClientMessageHandler(callback)
                                );
                            }
                        });
                future = bootstrap.connect(HOST, PORT).sync();
                log.debug("Network start listening");
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("e", e);
            } finally {
                worker.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static Net getInstance(Callback callback) {
        if (INSTANCE == null) {
            INSTANCE = new Net(callback);
        }
        return INSTANCE;
    }

    public void send(AbstractMessage msg) {
        log.debug("Sent: {}",msg);
        netChannel.writeAndFlush(msg);

    }
}
