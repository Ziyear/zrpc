package com.ziyear.zrpc.core.netty.handler;

import com.ziyear.zrpc.core.bean.RpcInvokeResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.CountDownLatch;

/**
 * 功能描述 : 客户端处理
 *
 * @author Ziyear 2021-06-05 15:58
 */
public class RpcClientChannel extends ChannelInboundHandlerAdapter {

    private RpcInvokeResult rpcInvokeResult;

    private CountDownLatch countDownLatch;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        rpcInvokeResult = (RpcInvokeResult) msg;
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    public Object getData() throws Exception {
        if (countDownLatch == null) {
            throw new Exception("获取数据异常");
        }
        //等待拿数据
        countDownLatch.await();
        if (!rpcInvokeResult.isSuccess()) {
            throw new Exception(rpcInvokeResult.getMsg());
        }
        return rpcInvokeResult.getData();
    }

    public void startCount() {
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
