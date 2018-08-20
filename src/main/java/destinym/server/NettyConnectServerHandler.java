package destinym.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyConnectServerHandler extends ChannelInboundHandlerAdapter {
    private AtomicInteger connectNum;

    public NettyConnectServerHandler(AtomicInteger connectNum) {
        this.connectNum = connectNum;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        if (connectNum.incrementAndGet() % 100 == 0) {
            System.out.println("current connected" + connectNum.get());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if (connectNum.decrementAndGet() % 100 == 0) {
            System.out.println("current connected" + connectNum.get());
        }
    }

}
