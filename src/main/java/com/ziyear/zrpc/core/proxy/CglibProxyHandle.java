package com.ziyear.zrpc.core.proxy;

import com.ziyear.zrpc.core.bean.RemoteService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGLIB代理
 */
public class CglibProxyHandle extends AbstractProxy implements MethodInterceptor {
    private RemoteService remoteService;

    public CglibProxyHandle(RemoteService remoteService) {
        this.remoteService = remoteService;
    }

    @Override
    public Object getPoxy() {
        Class<?> interfaceClass = remoteService.getServiceClass();
        // 通过CGLIB动态代理获取代理对象的过程
        Enhancer enhancer = new Enhancer();
        // 设置enhancer对象的父类
        enhancer.setSuperclass(interfaceClass);
        // 设置enhancer的回调对象
        enhancer.setCallback(this);
        // 创建代理对象
        return enhancer.create();

    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        //如果调用object默认方法,就放行
        if (Object.class.equals(method.getDeclaringClass())) {
            return methodProxy.invokeSuper(obj, args);
        }
        //调用模版方法,来获取参数
        return doRpcHandle(remoteService, method, args);
    }
}
