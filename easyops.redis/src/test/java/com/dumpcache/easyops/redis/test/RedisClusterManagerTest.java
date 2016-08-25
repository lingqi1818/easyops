package com.dumpcache.easyops.redis.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dumpcache.easyops.redis.service.RedisClusterManager;
import com.dumpcache.easyops.redis.service.RedisClusterManager.RedisClusterInfo;

import junit.framework.TestCase;

public class RedisClusterManagerTest extends TestCase {
    private RedisClusterManager redisClusterManager;

    @SuppressWarnings("resource")
    public void setUp() {
        ApplicationContext ac = new ClassPathXmlApplicationContext(
                new String[] { "spring/spring-datasource.xml", "spring/spring-service.xml" });
        redisClusterManager = ac.getBean(RedisClusterManager.class);
    }

    public void testInfoCluster() throws Exception {
        RedisClusterInfo info = redisClusterManager.infoCluster(17);
        System.out.println(info.getClusterInfo());
        System.out.println(info.getNodesInfo().get(0).getStatus());
        System.out.println(info.getNodesInfo().get(0).getNode().getNodeInfo());
    }
}
