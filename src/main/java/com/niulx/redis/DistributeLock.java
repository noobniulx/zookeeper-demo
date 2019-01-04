package com.niulx.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.UUID;

public class DistributeLock {

    // 获取锁
    public String acquireLock(String lockName, long acquireTimeout, long lockTimeout) {
        String identifier = UUID.randomUUID().toString();
        String key = "lock" + lockName;
        int lockExpire = (int) lockTimeout / 1000;
        Jedis jedis = JedisUtils.getResource();
        long end = System.currentTimeMillis() + acquireTimeout;
        try {
            while (System.currentTimeMillis() < end) {
                if (jedis.setnx(key, identifier) == 1) {
                    // 获取锁成功
                    jedis.expire(key, lockExpire);
                    return identifier;
                }
                if (jedis.ttl(key) == -1) {
                    jedis.expire(key, lockExpire);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            jedis.close();
        }
        return null;
    }

    // 释放锁
    public boolean releaseLock(String lockName, String identifier) {
        System.out.println(lockName + "-开始释放锁:" + identifier);
        String key = "lock" + lockName;
        Jedis jedis = JedisUtils.getResource();
        boolean releaseLock = false;
        try {
            while (true) {
                jedis.watch(key);
                if (identifier.equals(jedis.get(key))) {
                    Transaction transaction = jedis.multi();
                    transaction.del(key);
                    if (transaction.exec().isEmpty()) {
                        continue;
                    }
                    releaseLock = true;
                }
                jedis.unwatch();
                break;
            }
        } finally {
            jedis.close();
        }
        return releaseLock;
    }
}
