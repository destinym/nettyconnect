package com.destinym.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyConnectClient {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private String host;
    private int port;

    public NettyConnectClient(String host, int port, EventLoopGroup workerGroup) {
        this.host = host;
        this.port = port;
        this.workerGroup = workerGroup;

        bootstrap = new Bootstrap();
    }

    public void connect() {
        try {
            bootstrap.group(workerGroup); // (2)
            bootstrap.channel(NioSocketChannel.class); // (3)
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettyConnectClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = bootstrap.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            //f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8081;
        int connectNum = 0;
        if (args.length > 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            connectNum = Integer.parseInt(args[2]);
        } else if (args.length == 1) {
            host = args[0];
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        for (int i = 0; i < connectNum; i++) {
            NettyConnectClient nettyConnectClient = new NettyConnectClient(host, port, workerGroup);
            nettyConnectClient.connect();
            System.out.println("aaa"+ i);
        }

    }
}
