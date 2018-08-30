package com.destinym.heartbeat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class HeartBeatClient {
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private String host;
    private int port;
    private CopyOnWriteArrayList<Channel> channelList;


    public HeartBeatClient(String host, int port, EventLoopGroup workerGroup, CopyOnWriteArrayList<Channel> channelList) {
        this.host = host;
        this.port = port;
        this.workerGroup = workerGroup;
        this.channelList = channelList;
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
                    ch.pipeline().addLast(new ClientHandler(channelList));
                }
            });

            // Start the client.
            ChannelFuture f = bootstrap.connect(host, port).sync(); // (5)
            channelList.add(f.channel());

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
        CopyOnWriteArrayList<Channel> channelList = new CopyOnWriteArrayList<>();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        for (int i = 0; i < connectNum; i++) {
            HeartBeatClient heartBeatClient = new HeartBeatClient(host, port, workerGroup, channelList);
            heartBeatClient.connect();
            System.out.println(i + "client start");
        }

        while (channelList.size() > 0) {
            System.out.println("current channel: " + channelList);
            sleep(10000);
        }
        workerGroup.shutdownGracefully();

    }
}
