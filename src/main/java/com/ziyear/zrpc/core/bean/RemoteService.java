package com.ziyear.zrpc.core.bean;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 远程服务
 *
 * @author Ziyear 2021-06-05 15:32
 */
public class RemoteService {

    /**
     * 远程服务的IP地址
     */
    private List<String> address;

    /**
     * 服务的名称
     */
    private String name;

    /**
     * 服务的class信息
     */
    private Class<?> serviceClass;

    /**
     * 负载均衡到了哪一个位置
     */
    private int balanceIndex;

    public RemoteService(List<String> address, String name) {
        this.address = address;
        this.name = name;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalanceIndex() {
        return balanceIndex;
    }

    public void setBalanceIndex(int balanceIndex) {
        this.balanceIndex = balanceIndex;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public InetSocketAddress getAddressByBalance() {
        if (address == null || address.size() == 0) {
            return null;
        }
        String addressStr = null;
        synchronized (this) {
            balanceIndex = balanceIndex + 1;
            if (balanceIndex > address.size() - 1) {
                balanceIndex = 0;
            }
            addressStr = address.get(balanceIndex);
        }

        if (addressStr == null) {
            return null;
        }

        String[] split = addressStr.split("-");

        return InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1]));
    }


    public synchronized void addRemoteAddress(String newaddress) {
        if (address.contains(newaddress)) {
            return;
        }
        address.add(newaddress);
    }

    public synchronized void removeRemoteAddress(String newaddress) {
        address.remove(newaddress);
    }
}