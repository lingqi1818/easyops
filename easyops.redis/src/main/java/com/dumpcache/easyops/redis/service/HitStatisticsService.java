package com.dumpcache.easyops.redis.service;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;

public class HitStatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HitStatisticsService.class);

    private JedisCluster        jedisCluster;
    private int                 clusterId;
    private ThreadPoolExecutor  pool   = new ThreadPoolExecutor(10, 20, 100, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(100000), new DiscardAndLogPolicy());

    public HitStatisticsService(JedisCluster jedisCluster, int clusterId) {
        this.jedisCluster = jedisCluster;
        this.clusterId = clusterId;
    }

    public void stat(final String key, final boolean isHit) {
        pool.submit(new Runnable() {
            public void run() {
                _stat(key, isHit);
            }
        });
    }

    private void _stat(String key, boolean isHit) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        Set<String> keys = jedisCluster
                .smembers("easy_ops_redis_hit_stat_monitor_keys_set_" + clusterId);
        String monitorKey = null;
        if (keys != null) {
            for (String mk : keys) {
                if (key.startsWith(mk)) {
                    monitorKey = mk;
                    break;
                }
            }
        }

        if (!StringUtils.isEmpty(monitorKey)) {
            if (isHit) {
                Long result = jedisCluster.incr(
                        "easy_ops_redis_hit_stat_monitor_key_hit_" + clusterId + "_" + monitorKey);
                System.out.println(result);
            }
            jedisCluster.incr(
                    "easy_ops_redis_hit_stat_monitor_key_total_" + clusterId + "_" + monitorKey);
        }
    }

    static class DiscardAndLogPolicy extends ThreadPoolExecutor.DiscardPolicy {
        public DiscardAndLogPolicy() {
            super();
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            LOGGER.error("HitStatisticsService ThreadPoolExecutor rejectedExecution:", e);
            super.rejectedExecution(r, e);
        }
    }

}
