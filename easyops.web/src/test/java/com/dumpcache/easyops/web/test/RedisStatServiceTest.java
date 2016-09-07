package com.dumpcache.easyops.web.test;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dumpcache.easyops.redis.service.ClusterRedisServiceImpl;
import com.dumpcache.easyops.redis.service.RedisClusterManager;
import com.dumpcache.easyops.web.service.RedisStatServiceImpl;

import junit.framework.TestCase;

public class RedisStatServiceTest extends TestCase {
    private RedisStatServiceImpl    redisStatService    = new RedisStatServiceImpl();
    private ClusterRedisServiceImpl clusterRedisService = new ClusterRedisServiceImpl();

    public void setUp() {
        @SuppressWarnings("resource")
        ApplicationContext ac = new ClassPathXmlApplicationContext(
                new String[] { "spring/spring-datasource.xml", "spring/spring-service.xml" });
        RedisClusterManager redisClusterManager = ac.getBean(RedisClusterManager.class);
        DataSource ds = (DataSource) ac.getBean("dataSource");
        redisStatService.setRedisClusterManager(redisClusterManager);
        redisStatService.setDataSource(ds);
        clusterRedisService.setNamespace("");
        clusterRedisService.setAppName("");
        clusterRedisService.setDataSource(ds);
        clusterRedisService.setClusterId(20);
        clusterRedisService.init();
    }

    public void testRedisStat() {
        redisStatService.addMonitorKey(20, "test");
        clusterRedisService.set("test", "123");
        System.out.println(clusterRedisService.get("test"));
        System.out.println(redisStatService.getAllStats().get(0).getHitRate());
    }

}
