#Netty统计连接数
##思路
netty如何统计当前的连接数？   
当有连接接入netty server的时候，ChannelInboundHandlerAdapter中就会调用regiser和active方法。我们只需要在这里对计数器递增即可。   
同时当有连接断开（客户端程序手动断开的时候，客户端异常断开不会完成四次挥手，服务端没法立刻判断客户端是否离开), ChannelInboundHandlerAdapter中会调用unregisert和inactive方法，我们需要在这里对计数器进行递减即可。
##注意点
1 例子中的NettyConnectServerHandler不是sharedable的，每次都需要new来创建。
  所以统计的变量应该是传入的参数。
2 保证多线程的时候统计变量不会出现问题，使用AtomicInteger来保证。
###核心代码

```
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
```

### git地址
[https://github.com/destinym/nettyconnect.git](https://github.com/destinym/nettyconnect.git)
count模块