package com.ziyear.zrpc.core.proxy;

import com.ziyear.zrpc.core.bean.RemoteService;
import com.ziyear.zrpc.core.exception.ZRpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述 :代理工厂
 *
 * @author Ziyear 2021-6-5 17:15
 */
@Slf4j
public class ProxyFactory {

    public static String TYPE_JDK = "jdk";
    public static String TYPE_CGLIB = "cglib";


    public static Object getProxy(RemoteService remoteService, String type) {
        AbstractProxy abstractProxy = null;
        if (TYPE_JDK.equals(type)) {
            abstractProxy = new JdkProxyHandle(remoteService);
        }

        if (TYPE_CGLIB.equals(type)) {
            abstractProxy = new CglibProxyHandle(remoteService);
        }
        if (abstractProxy == null) {
            log.error("获取代理对象失败,请正确选择类型!");
            throw new ZRpcException("错误的代理类型：" + type);
        }
        return abstractProxy.getPoxy();
    }

}
