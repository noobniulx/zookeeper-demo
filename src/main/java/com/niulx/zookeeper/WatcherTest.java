package com.niulx.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatcherTest {

    public static void main(String[] args) throws Exception {

        final CountDownLatch c = new CountDownLatch(1);
        final ZooKeeper z = new ZooKeeper("192.168.149.129:2181,192.168.149.130:2181,192.168.149.131:2181", 4000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent.getType() + "->" + watchedEvent.getPath());
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    c.countDown();
                }
            }
        });
        c.await();
        System.out.println("连接成功:" + z.getState());


        z.create("/temp", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        Stat stat = z.exists("/temp", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent.getType() + "->" + watchedEvent.getPath());
                try {
                    z.exists(watchedEvent.getPath(), true);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        stat = z.setData("/temp", "2".getBytes(), stat.getVersion());

        Thread.sleep(10);

        z.delete("/temp", stat.getVersion());
    }
}
