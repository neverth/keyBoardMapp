package fun.neverth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.awt.*;
import java.util.HashMap;

/**
 * @author neverth.li
 * @date 2020/10/13 16:32
 */
public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    Robot robot = new Robot();

    HashMap<Integer, Boolean> map = new HashMap<>();

    public ClientHandler() throws AWTException {
    }

    public void action(String key){
        int val = Integer.parseInt(key.substring(1));

        if (key.startsWith("d")){
            map.putIfAbsent(val, false);
            if (!map.get(val)){
                map.put(val, true);
                robot.keyPress(val);
            }
        }else{
            if (map.get(val)){
                map.put(val, false);
                robot.keyRelease(val);
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf buf = (ByteBuf) msg;
//        // 创建目标大小的数组
//        byte[] barray = new byte[buf.readableBytes()];
//        // 把数据从bytebuf转移到byte[]
//        buf.getBytes(0, barray);
//        //将byte[]转成字符串用于打印
//        String str = new String(barray);
        System.out.println(msg);
        action((String)msg);
    }

    // 数据读取完毕的处理
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    // 出现异常的处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client 读取数据出现异常");
        cause.printStackTrace();
        ctx.close();
    }
}
