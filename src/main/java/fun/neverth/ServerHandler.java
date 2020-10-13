package fun.neverth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author neverth.li
 * @date 2020/10/13 15:49
 */
public class ServerHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 每当从服务端收到新的客户端连接时，客户端的 Channel 存入ChannelGroup列表中，
     * 并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + "加入");
        channels.add(ctx.channel());
    }

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     * 并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + "离开");
        channels.remove(ctx.channel());
    }

    /**
     * 覆盖了 channelRead0() 事件处理方法。每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。
     * 其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
     */
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        // 传统的http接入
        if (o instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) o);
        }
        // webSocket接入
        else if (o instanceof WebSocketFrame) {
            handleWebsocketFrame(ctx, (WebSocketFrame) o);
        }
    }

    /**
     * 覆盖了 channelActive() 事件处理方法。服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + "在线");
    }

    /**
     * 覆盖了 channelInactive() 事件处理方法。服务端监听到客户端不活动
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[SERVER] - " + incoming.remoteAddress() + "掉线");
    }

    /**
     * exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在
     * 处理事件时抛出的异常时。在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。然而
     * 这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 构造握手响应返回
        WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory("ws://localhost:20000/web", null, false);
        handshaker = webSocketServerHandshakerFactory.newHandshaker(req);
        handshaker.handshake(ctx.channel(), req);
    }

    private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是链路关闭消息
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 文本消息处理
        String request = ((TextWebSocketFrame) frame).text();
        System.out.println(ctx.channel().remoteAddress() + ": " + request);

        for (Channel channel : channels) {
            if (channel.equals(ctx.channel())){
                channel.writeAndFlush(new TextWebSocketFrame(request));
            }else{
                ChannelFuture channelFuture = channel.writeAndFlush(request + '\n');
                System.out.println(channelFuture.isSuccess());
            }
        }
    }

}
