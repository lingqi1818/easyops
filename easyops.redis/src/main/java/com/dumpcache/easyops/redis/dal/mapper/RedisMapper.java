package com.dumpcache.easyops.redis.dal.mapper;

import java.util.List;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;

public interface RedisMapper {

    public void insertRedisCluster(RedisCluster rc);
    
    public void updateRedisCluster(RedisCluster rc);

    public List<RedisCluster> getAllRedisClusters();

    public int getgetAllRedisClustersCount();

}
