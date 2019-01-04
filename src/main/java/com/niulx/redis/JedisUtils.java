package com.niulx.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisUtils {

    private static JedisPool pool;

    static {
        pool = new JedisPool("192.168.149.130", 6379);
    }

    public static Jedis getResource() {
        return pool.getResource();
    }

}
