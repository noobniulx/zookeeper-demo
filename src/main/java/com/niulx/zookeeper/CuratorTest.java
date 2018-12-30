package com.niulx.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

public class CuratorTest {

    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString("192.168.149.129:2181,192.168.149.130:2181,192.168.149.131:2181").
                sessionTimeoutMs(4000).
                retryPolicy(new ExponentialBackoffRetry(1000, 3)).
                namespace("curator").build();
        curatorFramework.start();

        /** 创建节点*/
        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/temp", "1".getBytes());

        /** 删除节点*/
        curatorFramework.delete().deletingChildrenIfNeeded().forPath("/temp");

        Stat stat = new Stat();

        curatorFramework.getData().storingStatIn(stat).forPath("/temp");

        /** 修改节点*/
        curatorFramework.setData().withVersion(stat.getVersion()).forPath("/temp","2".getBytes());

        curatorFramework.close();
    }
}
