package com.dumpcache.easyops.redis.test;

import com.dumpcache.easyops.redis.service.RedisClusterManager;
import com.dumpcache.easyops.redis.service.RedisClusterManagerImpl;

import junit.framework.TestCase;

public class RedisClusterManagerTest extends TestCase {
    private RedisClusterManager redisClusterManager;

    public void setUp() {
        redisClusterManager = new RedisClusterManagerImpl();
    }

    public void testCreateCluster() throws Exception {
        redisClusterManager.createRedisCluster(null, null);
    }
}
