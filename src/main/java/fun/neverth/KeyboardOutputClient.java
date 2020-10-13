package fun.neverth;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.UnsupportedEncodingException;

/**
 * @author neverth.li
 * @date 2020/10/13 16:28
 */
public class KeyboardOutputClient {
    // 要请求的服务器的ip地址
    private final String ip;
    // 服务器的端口
    private final int port;

    public KeyboardOutputClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    private void run() throws InterruptedException, UnsupportedEncodingException {
        // 处理线程组  workergroup中取出一个管道channel来建立连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline ch = socketChannel.pipeline();
                            ch.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                            ch.addLast("decoder", new StringDecoder());
                            ch.addLast("encoder", new StringEncoder());
                            ch.addLast("handler", new ClientHandler());
                        }
                    });

            // 客户端开启
            ChannelFuture cf = bs.connect(ip, port).sync();
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 退出，释放线程池资源
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
        new KeyboardOutputClient("106.14.44.108", 8899).run();
//        new Client("127.0.0.1", 8899).run();
    }
}
