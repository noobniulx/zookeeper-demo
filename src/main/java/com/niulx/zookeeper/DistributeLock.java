package com.niulx.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DistributeLock implements Lock, Watcher {

    private ZooKeeper zk;

    private String CURRENT_LOCK; // 当前锁

    private String WAIT_LOCK; // 等待的前一个锁

    private String ROOT_LOCK = "/locks"; // 根节点

    private CountDownLatch countDownLatch;


    public DistributeLock() {
        try {
            zk = new ZooKeeper("192.168.149.129:2181,192.168.149.130:2181,192.168.149.131:2181", 4000, this);
            Stat stat = zk.exists(ROOT_LOCK, false);
            if (stat == null) {
                zk.create(ROOT_LOCK, "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void lock() {
        // 获取锁成功
        if (tryLock()) {
            System.out.println(Thread.currentThread().getName() + "->" + "成功获取锁:" + CURRENT_LOCK);
            return;
        }
        // 没有获取到锁继续等待
        try {
            waitForLock(WAIT_LOCK);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForLock(String prev) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(prev, true); //监听上一个节点
        if (stat != null) {
            System.out.println(Thread.currentThread().getName() + "->" + "等待释放锁:" + prev);
            countDownLatch = new CountDownLatch(1);
            countDownLatch.await();
            // 继续判断当前节点是不是最小节点
            System.out.println(Thread.currentThread().getName() + "->获取锁成功");
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            // 创建临时节点
            CURRENT_LOCK = zk.create(ROOT_LOCK + "/", "0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + "->" + "尝试获取锁:" + CURRENT_LOCK);
            // 获取所有子节点
            List<String> childrens = zk.getChildren(ROOT_LOCK, false);
            // 对所有子节点排序
            SortedSet<String> sortedSet = new TreeSet<>();
            for (String children : childrens) {
                sortedSet.add(ROOT_LOCK + "/" + children);
            }
            // 获取最小的节点
            String first = sortedSet.first();
            System.out.println(Thread.currentThread().getName() + "->" + "最小的节点" + first);
            if (CURRENT_LOCK.equals(first)) {
                return true;
            }
            // 获取比当前节点更小的最后一个节点
            SortedSet<String> headSet = ((TreeSet<String>) sortedSet).headSet(CURRENT_LOCK);
            if (!headSet.isEmpty()) {
                WAIT_LOCK = headSet.last();
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        System.out.println(Thread.currentThread().getName() + "->" + "释放锁:" + CURRENT_LOCK);
        try {
            zk.delete(CURRENT_LOCK, -1);
            CURRENT_LOCK = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    @Override
    public void process(WatchedEvent event) {
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }
}
