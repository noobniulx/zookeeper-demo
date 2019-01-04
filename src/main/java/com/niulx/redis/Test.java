package com.niulx.redis;

public class Test extends Thread {

    private static int count;

    public static void main(String[] args) {
        Test t = new Test();

        for (int i = 0; i < 10; i++) {
            new Thread(t, "thread" + i).start();
        }
    }


    @Override
    public void run() {
        while (true) {
            DistributeLock distributeLock = new DistributeLock();
            String lock = distributeLock.acquireLock("updateOrder", 1000, 1000);
            if (lock != null) {
                System.out.println(Thread.currentThread().getName() + "-成功获取锁:" + lock);
                try {
                    System.out.println(count++);
                    Thread.sleep(10);
                    distributeLock.releaseLock("updateOrder", lock);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
