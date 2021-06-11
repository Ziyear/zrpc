package com.ziyear.zrpc.core.proxy;

import com.ziyear.zrpc.core.bean.RemoteService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK代理
 */
public class JdkProxyHandle extends AbstractProxy implements InvocationHandler {

    private RemoteService remoteService;

    public JdkProxyHandle(RemoteService remoteService) {
        this.remoteService = remoteService;
    }

    @Override
    public Object getPoxy() {
        Class<?> interfaceClass = remoteService.getServiceClass();
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //如果调用object默认方法,就放行
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        //调用模版方法,来获取参数
        return doRpcHandle(remoteService, method, args);
    }
}