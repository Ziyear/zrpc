package com.ziyear.zrpc.core.netty.handler;

import com.ziyear.zrpc.core.ZRpcServiceInvoke;
import com.ziyear.zrpc.core.bean.RpcInvokeBean;
import com.ziyear.zrpc.core.bean.RpcInvokeResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 功能描述 : 服务端处理
 *
 * @author Ziyear 2021-06-05 16:01
 */
public class RpcServiceChannel extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcInvokeBean rpcInvokeBean = (RpcInvokeBean) msg;
        String serviceName = rpcInvokeBean.getServiceName();
        String methodName = rpcInvokeBean.getMethodName();
        Object[] methodAges = rpcInvokeBean.getMethodAges();
        Class<?>[] paramType = rpcInvokeBean.getParamType();
        //执行远程方法
        RpcInvokeResult execResult = ZRpcServiceInvoke.getInstance().invoke(serviceName, methodName, methodAges, paramType);
        //把结果写回去
        ctx.channel().writeAndFlush(execResult);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
