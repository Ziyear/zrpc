package com.ziyear.zrpc.core.netty;

import com.ziyear.zrpc.core.netty.handler.RpcServiceChannel;
import com.ziyear.zrpc.core.netty.handler.SerializationHandle;
import com.ziyear.zrpc.core.netty.handler.UnSerializationHandle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 功能描述 : 服务端
 *
 * @author Ziyear 2021-06-05 16:19
 */
public class NettyRpcService {
    private String ip;
    private int port;
    private int pageLen = 1024 * 1024 * 1024;

    public NettyRpcService(String ip, int port) {
        this.ip = ip;
        this.port = port;
        serviceStar();
    }

    /**
     * 开启服务,将开启一个 端口去等待数据
     */
    private void serviceStar() {
        EventLoopGroup work = new NioEventLoopGroup();
        EventLoopGroup child = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(work, child)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //发送数据的时候,加上包的长度.
                        pipeline.addLast(new LengthFieldPrepender(4));
                        //接收数据的时候,把包的长度拆了
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(pageLen, 0, 4, 0, 4));
                        //把发送出去的数据 序列化
                        pipeline.addLast(new SerializationHandle());
                        //把接收的数据序列化
                        pipeline.addLast(new UnSerializationHandle());
                        //服务端自己的业务逻辑
                        pipeline.addLast(new RpcServiceChannel());
                    }
                });
        try {
            serverBootstrap.bind(port).sync();
            System.out.println(ip + ":" + port + " : 服务开启");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
