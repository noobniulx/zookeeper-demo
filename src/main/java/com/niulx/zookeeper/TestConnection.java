package com.niulx.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class TestConnection {

    public static void main(String[] args) throws Exception {
        final CountDownLatch c = new CountDownLatch(1);
        ZooKeeper z = new ZooKeeper("192.168.149.129:2181,192.168.149.130:2181,192.168.149.131:2181", 4000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    c.countDown();
                }
            }
        });
        c.await();
        System.out.println("连接成功:"+z.getState());
    }
}
