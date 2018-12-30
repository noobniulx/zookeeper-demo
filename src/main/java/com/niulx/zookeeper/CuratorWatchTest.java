package com.niulx.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;


public class CuratorWatchTest {


    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString("192.168.149.129:2181,192.168.149.130:2181,192.168.149.131:2181").
                sessionTimeoutMs(4000).
                retryPolicy(new ExponentialBackoffRetry(1000, 3)).
                namespace("curator").build();
        curatorFramework.start();

        //addListenerWithPathNodeCache(curatorFramework, "/temp");

        //addListenerWithPathChildCache(curatorFramework, "/temp");

        addListenerWithPathTreeCache(curatorFramework, "/temp");

        System.in.read();
    }


    public static void addListenerWithPathNodeCache(CuratorFramework curatorFramework, String path) throws Exception {
        final NodeCache nodeCache = new NodeCache(curatorFramework, path, false);
        NodeCacheListener listener = new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println("Receive Event:" + nodeCache.getCurrentData().getPath());
            }
        };
        nodeCache.getListenable().addListener(listener);
        nodeCache.start();
    }


    public static void addListenerWithPathChildCache(CuratorFramework curatorFramework, String path) throws Exception {
        final PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, path, true);
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println("Receive Event:" + event.getType());
            }
        };
        pathChildrenCache.getListenable().addListener(listener);
        pathChildrenCache.start();
    }


    public static void addListenerWithPathTreeCache(CuratorFramework curatorFramework, String path) throws Exception {
        final TreeCache treeCache = new TreeCache(curatorFramework, path);
        TreeCacheListener listener = new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
                System.out.println(event.getType() + "->" + event.getData().getPath());
            }

        };
        treeCache.getListenable().addListener(listener);
        treeCache.start();
    }

}
