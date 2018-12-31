package com.niulx.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class App {

    public static void main(String[] args) throws IOException {
        CountDownLatch c = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(()-> {
                try {
                    c.await();
                    DistributeLock lock = new DistributeLock();
                    lock.lock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread-" + i).start();
            c.countDown();
        }
        System.in.read();
    }
}
