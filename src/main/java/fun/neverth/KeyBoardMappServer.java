package fun.neverth;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author neverth.li
 * @date 2020/10/13 15:48
 */
public class KeyBoardMappServer {

    ChannelFuture webSocket;
    ChannelFuture socket;

    public static void main(String[] args) throws Exception {
        //服务器启动
        new KeyBoardMappServer().start(20000);
    }

    public void start(int port) throws Exception {
        //用于监听连接的线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //用于发送接收消息的线程组
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            //启动类引导程序
            ServerBootstrap b = new ServerBootstrap();
            //绑定两个线程组
            b.group(bossGroup, workGroup);
            //设置非阻塞,用它来建立新accept的连接,用于构造serverSocketChannel的工厂类
            b.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    ChannelPipeline ch = channel.pipeline();
                    if (channel.localAddress().getPort() == 8898) {
                        // HttpServerCodec：将请求和应答消息解码为HTTP消息
                        ch.addLast(new HttpServerCodec());
                        // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
                        ch.addLast(new HttpObjectAggregator(65536));
                        // ChunkedWriteHandler：向客户端发送HTML5文件
                        ch.addLast(new ChunkedWriteHandler());
                        //在管道中添加自己实现的Handler处理类
                        ch.addLast(new ServerHandler());
                    }else {
                        ch.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                        ch.addLast("decoder", new StringDecoder());
                        ch.addLast("encoder", new StringEncoder());
                        ch.addLast("handler", new ServerHandler());
                    }
                }
            }).handler(new LoggingHandler(LogLevel.INFO));

            webSocket = b.bind(8898).sync();
            System.out.println("webSocket服务器启动端口:" + 8898);
            socket = b.bind(8899).sync();
            System.out.println("socket服务器启动端口:" + 8899);

            webSocket.channel().closeFuture().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    webSocket.channel().close();
                }
            });
            socket.channel().closeFuture().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    socket.channel().close();
                }
            }).sync();
//            Channel channel = b.bind(port).sync().channel();
//            channel.closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
