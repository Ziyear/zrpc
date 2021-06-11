package com.ziyear.zrpc.core.netty;

import com.ziyear.zrpc.core.bean.RpcInvokeBean;
import com.ziyear.zrpc.core.netty.handler.RpcClientChannel;
import com.ziyear.zrpc.core.netty.handler.SerializationHandle;
import com.ziyear.zrpc.core.netty.handler.UnSerializationHandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.net.InetSocketAddress;

/**
 * 功能描述 : 客户端
 *
 * @author Ziyear 2021-06-05 15:39
 */
public class NettyRpcClient {

    private static final String CLIENT_HANDLE = "clientHandle";

    private int pageLen = 1024 * 1024 * 10;

    /**
     * 调度模块
     */
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private NettyRpcClient() {
    }

    /**
     * 单列获取对象
     *
     * @return
     */
    public static NettyRpcClient getInstance() {
        return Instance.nettyPpcClient;
    }

    /**
     * 初始化
     *
     * @param netSocketAddress
     * @param eventLoopGroup
     * @return
     */
    private ChannelFuture initNetty(InetSocketAddress netSocketAddress, EventLoopGroup eventLoopGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldPrepender(4));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(pageLen, 0, 4, 0, 4));
                        pipeline.addLast(new SerializationHandle());
                        pipeline.addLast(new UnSerializationHandle());
                        pipeline.addLast(CLIENT_HANDLE, new RpcClientChannel());
                    }
                });
        ChannelFuture connect = bootstrap.connect(netSocketAddress);
        try {
            connect.sync();
            return connect;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 功能描述 : 连接服务
     *
     * @param inetSocketAddress
     * @param rpcInvokeBean
     */
    public Object connectService(InetSocketAddress inetSocketAddress, RpcInvokeBean rpcInvokeBean) throws Exception {
        ChannelFuture connect = initNetty(inetSocketAddress, eventLoopGroup);
        try {
            assert connect != null;
            RpcClientChannel clientHandle = (RpcClientChannel) connect.channel().pipeline().get(CLIENT_HANDLE);
            clientHandle.startCount();
            //发送数据
            connect.channel().writeAndFlush(rpcInvokeBean).sync();
            //等待获取数据
            return clientHandle.getData();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connect != null) {
                connect.channel().close().sync();
            }
        }
    }

    private static class Instance {
        public static NettyRpcClient nettyPpcClient = new NettyRpcClient();
    }

}
