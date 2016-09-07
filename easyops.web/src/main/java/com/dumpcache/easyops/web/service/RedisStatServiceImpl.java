package com.dumpcache.easyops.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;
import com.dumpcache.easyops.redis.service.ClusterRedisServiceImpl;
import com.dumpcache.easyops.redis.service.HitStatisticsService;
import com.dumpcache.easyops.redis.service.RedisClusterManager;

@Service
public class RedisStatServiceImpl implements RedisStatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisStatService.class);
    @Autowired
    private RedisClusterManager redisClusterManager;

    @Autowired
    private DataSource          dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setRedisClusterManager(RedisClusterManager redisClusterManager) {
        this.redisClusterManager = redisClusterManager;
    }

    @Override
    public List<Stat> getAllStats() {
        List<Stat> stats = new ArrayList<Stat>();
        List<RedisCluster> clusters = redisClusterManager.listClusters();
        if (clusters != null && clusters.size() > 0) {
            for (RedisCluster cluster : clusters) {
                ClusterRedisServiceImpl redisService = null;
                try {
                    redisService = new ClusterRedisServiceImpl();
                    redisService.setClusterId(cluster.getId());
                    redisService.setDataSource(dataSource);
                    redisService.init();
                    HitStatisticsService hitStatisticsService = new HitStatisticsService(
                            redisService.getJedisCluster(), cluster.getId());
                    Set<String> keys = hitStatisticsService.getMonitorKeys();
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            Stat s = new Stat();
                            s.setClusterId(cluster.getId());
                            s.setKeyStart(key);
                            Long hitNo = hitStatisticsService.getMonitorKeyHitNumber(key);
                            Long totalNo = hitStatisticsService.getMonitorKeyTotalNumber(key);
                            if (hitNo == null || hitNo == 0 || totalNo == null || totalNo == 0) {
                                s.setHitRate(String.format("%.2f", 0.0));
                            } else {
                                s.setHitRate(String.format("%.2f", hitNo * 100.0 / totalNo));
                            }
                            stats.add(s);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                } finally {
                    if (redisService != null) {
                        redisService.close();
                    }
                }
            }
        }
        return stats;
    }

    @Override
    public void addMonitorKey(int clusterId, String key) {
        ClusterRedisServiceImpl redisService = null;
        try {
            redisService = new ClusterRedisServiceImpl();
            redisService.setClusterId(clusterId);
            redisService.setDataSource(dataSource);
            redisService.init();
            HitStatisticsService hitStatisticsService = new HitStatisticsService(
                    redisService.getJedisCluster(), clusterId);
            hitStatisticsService.addMonitorKey(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            redisService.close();
        }

    }

    @Override
    public void deleteMonitorKey(int clusterId, String key) {
        ClusterRedisServiceImpl redisService = null;
        try {
            redisService = new ClusterRedisServiceImpl();
            redisService.setClusterId(clusterId);
            redisService.setDataSource(dataSource);
            redisService.init();
            HitStatisticsService hitStatisticsService = new HitStatisticsService(
                    redisService.getJedisCluster(), clusterId);
            hitStatisticsService.deleteMonitorKey(key);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            redisService.close();
        }

    }

}
