package com.ziyear.zrpc.core.proxy;

import com.ziyear.zrpc.core.bean.RemoteService;
import com.ziyear.zrpc.core.bean.RpcInvokeBean;
import com.ziyear.zrpc.core.exception.ZRpcException;
import com.ziyear.zrpc.core.netty.NettyRpcClient;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

/**
 * 抽象的代理
 */
public abstract class AbstractProxy {

    /**
     * 获取代理
     *
     * @return
     */
    public abstract Object getPoxy();

    /**
     * PRC客户端真正的调用执行
     *
     * @param remoteService
     * @param method
     * @param args
     */
    public Object doRpcHandle(RemoteService remoteService, Method method, Object[] args) throws Exception {
        //使用 netty访问
        NettyRpcClient nettyPpcClient = NettyRpcClient.getInstance();
        InetSocketAddress addressByBalance = remoteService.getAddressByBalance();
        if (addressByBalance == null) {
            throw new ZRpcException("没有合适远程服务地址");
        }
        RpcInvokeBean rpcInvokeBean = new RpcInvokeBean();
        rpcInvokeBean.setParamType(method.getParameterTypes());
        rpcInvokeBean.setServiceName(remoteService.getName());
        rpcInvokeBean.setMethodName(method.getName());
        rpcInvokeBean.setMethodAges(args);
        return nettyPpcClient.connectService(addressByBalance, rpcInvokeBean);

    }
}