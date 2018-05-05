package Client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext){
//        channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("Block", CharsetUtil.UTF_8));
        System.out.println("channel active");
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object in) {
        ByteBuf b = (ByteBuf) in;
        String received = b.toString(CharsetUtil.UTF_8);
        String[] a = received.split(" ");
        System.out.println("Client received: " + received);

        if(a[0].equals("freeze"))
            Client.service.freeze();
        if(a[0].equals("thread"))
            Client.service.setThread(Integer.parseInt(a[2]));
        if(a[0].equals("db"))
            Client.service.setDB(Integer.parseInt(a[2]));
        if(a[0].equals("delete"))
            Client.service.deleteCommand(a[2]);
        if(a[0].equals("add"))
            Client.service.addCommand(a[2],a[3]);
        if(a[0].equals("update"))
            Client.service.updateCommand(a[2],a[3],a[4]);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel read Complete");
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause){
        cause.printStackTrace();
        channelHandlerContext.close();
    }

}
