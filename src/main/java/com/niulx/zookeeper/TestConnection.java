package com.niulx.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

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
        System.out.println("连接成功:" + z.getState());


        z.create("/temp", "zk".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        Stat stat = new Stat();
        System.out.println(new String(z.getData("/temp", null, stat)));

        z.setData("/temp", "test".getBytes(), stat.getVersion());

        System.out.println(new String(z.getData("/temp", null, stat)));

        z.delete("/temp", stat.getVersion());


    }
}
