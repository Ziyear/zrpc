package com.ziyear.zrpc.core;

import com.ziyear.zrpc.core.bean.RpcInvokeResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC服务正真的执行者
 */
public class ZRpcServiceInvoke {
    /**
     * 本地服务的缓存,消费者将来这里执行方法
     */
    public Map<String, Object> serviceCache = new ConcurrentHashMap<>();


    public void addService(String name, Object serviceInstance) {
        Object cache = serviceCache.get(name);
        if (cache != null) {
            return;
        }
        serviceCache.put(name, serviceInstance);
    }

    public static ZRpcServiceInvoke getInstance() {
        return Instance.zRecServiceInvoke;
    }

    public RpcInvokeResult invoke(String serviceName, String methodName, Object[] methodAges, Class<?>[] paramType) {
        if (serviceName == null || methodName == null) {
            return RpcInvokeResult.fail("参数异常");
        }

        Object execInstance = serviceCache.get(serviceName);
        if (execInstance == null) {
            return RpcInvokeResult.fail("没有提供对应的服务");
        }
        Class<?> execInstanceClass = execInstance.getClass();

        try {
            Method method = execInstanceClass.getMethod(methodName, paramType);
            Object result = method.invoke(execInstance, methodAges);
            return RpcInvokeResult.success(result);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return RpcInvokeResult.fail("没有提供对应的方法");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return RpcInvokeResult.fail("远程服务器方法参数异常");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return RpcInvokeResult.fail("远程服务方法调用失败");
        }
    }

    private static class Instance {
        public static ZRpcServiceInvoke zRecServiceInvoke = new ZRpcServiceInvoke();
    }
}