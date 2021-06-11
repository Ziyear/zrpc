package com.ziyear.zrpc.core;


import com.ziyear.zrpc.core.bean.RemoteService;
import com.ziyear.zrpc.core.netty.NettyRpcService;
import com.ziyear.zrpc.core.proxy.ProxyFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述 : rpc应用服务
 *
 * @author Ziyear 2021-6-5 17:06
 */
public class ZRpcApplication {

    private static final Pattern ZRPC_PATH_COMPILE = Pattern.compile("/Z-RpcService/(.*)/(.*-[0-9]{3,6})");

    @Setter
    private String root = "zrpc-root";

    @Setter
    private String secRoot = "/Z-RpcService";

    /**
     * zk的地址,集群里可以用 逗号分隔
     */
    private String zookeeperAddress = "127.0.0.1:2181";

    /**
     * PRC远程端口
     */
    @Getter
    @Setter
    private int port;

    /**
     * 提供者的ip地址,消费者将会通过这个ip和提供者消费服务
     */
    @Getter
    @Setter
    private String ip = "127.0.0.1";

    private CuratorFramework curatorFramework;
    /**
     * 远程服务的缓存
     */
    private Map<String, RemoteService> serviceCache = new ConcurrentHashMap<>();

    private TreeCache treeCache;

    /**
     * netty服务,如果这边有对象注册,则开启netty服务
     */
    private NettyRpcService nettyRpcService;

    public ZRpcApplication() {
        initZookeeper();
    }

    public ZRpcApplication(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
        initZookeeper();
    }


    /**
     * 初始化zookeeper连接
     */
    private void initZookeeper() {
        curatorFramework = CuratorFrameworkFactory.builder().namespace(root)
                .connectString(zookeeperAddress)
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(2000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        curatorFramework.start();
    }


    /**
     * 注册服务
     *
     * @param name
     * @param seviceInstance
     * @throws Exception
     */
    public void register(String name, Object seviceInstance) throws Exception {
        if (ip == null && port == 0) {
            throw new Exception("没有指定PRC服务端IP地址及端口");
        }

        String node = getNotePath(name) + "/" + ip + "-" + port;
        if (curatorFramework.checkExists().forPath(node) != null) {
            //如果 原来的节点还没删除,先删除
            curatorFramework.delete().forPath(node);
        }
        //添加新的节点
        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(node);

        //开启 netty服务器,等后调用者
        if (nettyRpcService == null) {
            nettyRpcService = new NettyRpcService(ip, port);
        }

        ZRpcServiceInvoke zRpcServiceInvoke = ZRpcServiceInvoke.getInstance();
        zRpcServiceInvoke.addService(name, seviceInstance);
    }


    public void register(Class<?> serviceInterface, Object serviceInstance) throws Exception {
        register(serviceInterface.getName(), serviceInstance);
    }


    /**
     * 发现服务
     *
     * @param <T>
     * @return
     */
    public <T> T getService(String name, Class<T> creatClass) throws Exception {
        watchServiceChange();
        RemoteService remoteService = getServiceByCacheOrZookeeper(name, creatClass);
        return (T) ProxyFactory.getProxy(remoteService, ProxyFactory.TYPE_JDK);
    }


    public <T> T getService(Class<T> creatClass) throws Exception {
        return getService(creatClass.getName(), creatClass);
    }


    /**
     * 从缓存中获取PRC资源,如果没有从,zookeeper中获取
     *
     * @param name
     * @return
     */
    private RemoteService getServiceByCacheOrZookeeper(String name, Class<?> creatClass) throws Exception {


        RemoteService remoteService = serviceCache.get(name);
        //拿到缓存数据,直接走人
        if (remoteService != null) {
            return remoteService;
        }

        String nodePath = getNotePath(name);
        //拿不到去zookeeper拿一下
        //先检查有没对应的节点,没有就不拿
        List<String> ipAndports = null;
        if (curatorFramework.checkExists().forPath(nodePath) == null) {
            ipAndports = new ArrayList<>();
        } else {
            ipAndports = curatorFramework.getChildren().forPath(nodePath);
        }

        remoteService = new RemoteService(ipAndports, name);
        remoteService.setServiceClass(creatClass);
        serviceCache.put(name, remoteService);
        return remoteService;
    }


    /**
     * 监控节点的变动,如果有变动,刷新缓存中 远程服务器的数据
     */
    private void watchServiceChange() throws Exception {
        if (treeCache != null) {
            return;
        }

        treeCache = new TreeCache(curatorFramework, secRoot);
        treeCache.start();
        treeCache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
            ChildData childData = treeCacheEvent.getData();
            if (childData == null) {
                return;
            }
            //获取更变的路径
            String path = childData.getPath();
            //提供者增加
            if (treeCacheEvent.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                //用正则,解析路径信息,最后必须带ip地址才能解析通过,index1 类路径 index2 ip信息
                String[] pathAndIp = pathPattern(path);
                if (pathAndIp == null) {
                    return;
                }
                //获取缓存中的远程服务对象
                RemoteService remoteService = serviceCache.get(pathAndIp[0]);
                if (remoteService == null) {
                    return;
                }
                //添加进新的ip地址
                remoteService.addRemoteAddress(pathAndIp[1]);
            }
            //提供者减少
            else if (treeCacheEvent.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                String[] pathAndIp = pathPattern(path);
                if (pathAndIp == null) {
                    return;
                }
                RemoteService remoteService = serviceCache.get(pathAndIp[0]);
                if (remoteService == null) {
                    return;
                }
                //删除对应的Ip地址
                remoteService.removeRemoteAddress(pathAndIp[1]);
            }
        });
    }


    private String getNotePath(String serviceName) {
        return secRoot + "/" + serviceName.replaceAll("\\.", "/");
    }

    /**
     * 正则匹配node的地址
     * 没有匹配上返回null
     * 匹配上返回一个数组
     * 1 位是: path
     * 2 位是: 后面的ip地址
     */
    private String[] pathPattern(String data) {
        Matcher matcher = ZRPC_PATH_COMPILE.matcher(data);
        if (!matcher.find()) {
            return null;
        }
        String serviceName = matcher.group(1);
        serviceName = serviceName.replaceAll("/", "\\.");

        return new String[]{serviceName, matcher.group(2)};
    }
}