package Client;

import Service.BlockService;
import Service.ServiceInterface;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {

    public static ServiceInterface service;
    public static Channel serverChannel;

    public Client() {

    }

    void run() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 9999));
            clientBootstrap.handler(new ClientHandler());
            ChannelFuture channelFuture = clientBootstrap.connect().sync();

            //any code related to client channel should be added here
            serverChannel = channelFuture.channel();

            Thread one = new Thread() {
                public void run() {
                    try {
                        while (true){
                            Scanner sc = new Scanner(System.in);
                            String line = sc.nextLine();
                            serverChannel.writeAndFlush(Unpooled.copiedBuffer(line, CharsetUtil.UTF_8));
                    }
                    } catch(Exception v) {
                        System.out.println(v);
                    }
                }
            };

            one.start();

            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            System.exit(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client c = new Client();
        new Thread(() -> {
            try {
                c.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service = new BlockService();
        service.run();
    }
}
