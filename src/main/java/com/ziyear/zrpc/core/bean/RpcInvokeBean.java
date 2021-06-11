package com.ziyear.zrpc.core.bean;


import java.util.Arrays;

/**
 * 执行
 *
 * @author Ziyear 2021-06-05 15:31
 */
public class RpcInvokeBean {

    /**
     * 要调用的类的名称
     */
    private String serviceName;

    /**
     * 要调用的方法的名称
     */
    private String methodName;

    /**
     * 方法的参数
     */
    private Object[] methodAges;

    /**
     * 参数类型
     */
    private Class<?>[] paramType;


    public Class<?>[] getParamType() {
        return paramType;
    }

    public void setParamType(Class<?>[] paramType) {
        this.paramType = paramType;
    }


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getMethodAges() {
        return methodAges;
    }

    public void setMethodAges(Object[] methodAges) {
        this.methodAges = methodAges;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RpcInvokeBean{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", methodAges=").append(Arrays.toString(methodAges));
        sb.append(", paramType=").append(Arrays.toString(paramType));
        sb.append('}');
        return sb.toString();
    }
}