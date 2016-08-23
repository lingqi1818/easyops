package com.dumpcache.easyops.redis.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;
import com.dumpcache.easyops.redis.dal.mapper.RedisMapper;

import redis.clients.jedis.Jedis;

@Service
public class RedisClusterManagerImpl implements RedisClusterManager {
    private static final Logger LOGGER    = LoggerFactory.getLogger(RedisClusterManagerImpl.class);
    private static int          MAX_SLOTS = 16384 - 1;
    @Autowired
    private RedisMapper         redisMapper;

    @Override
    public void createRedisCluster(String clusterName, List<RedisClusterNode> nodes) {
        if (StringUtils.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName is null!");
        }
        try {
            //addNodesToCluster(nodes);
            //initClusterSlots(nodes);
            insertToDB(clusterName, nodes);
        } catch (Exception ex) {
            throw new RuntimeException("create cluster failed:", ex);
        }
    }

    private void insertToDB(String clusterName, List<RedisClusterNode> nodes) {
        StringBuilder masterNodes = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            RedisClusterNode node = nodes.get(i);
            masterNodes.append(node.getHost()).append(":").append(node.getPort());
            if (i != nodes.size() - 1) {
                masterNodes.append(",");
            }
        }
        RedisCluster rc = new RedisCluster();
        rc.setClusterName(clusterName);
        rc.setMasterNodes(masterNodes.toString());
        rc.setSlaveNodes("");
        redisMapper.insertRedisCluster(rc);
    }

    private void initClusterSlots(List<RedisClusterNode> nodes) {
        int start = 0;
        int regionSlots = MAX_SLOTS / nodes.size() + 1;
        for (RedisClusterNode node : nodes) {
            Jedis jedis = new Jedis(node.getHost(), node.getPort());
            int[] slots = new int[regionSlots];
            int index = 0;
            for (int i = start; i < start + regionSlots; i++) {
                if (i > MAX_SLOTS) {
                    break;
                }
                slots[index++] = i;
            }
            start += regionSlots;
            int finalslots[] = new int[index];
            System.arraycopy(slots, 0, finalslots, 0, index);
            LOGGER.debug("slots:" + "min:" + finalslots[0] + ",max:" + finalslots[index - 1]);
            jedis.flushAll();
            jedis.clusterFlushSlots();
            jedis.clusterAddSlots(finalslots);
            jedis.close();
        }
    }

    private void addNodesToCluster(List<RedisClusterNode> nodes) {
        Jedis jedis = new Jedis(nodes.get(0).getHost(), nodes.get(0).getPort());
        for (int i = 1; i < nodes.size(); i++) {
            RedisClusterNode node = nodes.get(i);
            jedis.clusterMeet(node.getHost(), node.getPort());
            jedis.close();
        }
    }

    @Override
    public void dismissRedisCluster(String namespace, String appName, String clusterName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addNodesToCluster(String namespace, String appName, String clusterName,
                                  RedisClusterNode... nodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delNodesFromCluster(String namespace, String appName, String clusterName,
                                    RedisClusterNode... nodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSlaveToMaster(String namespace, String appName, String clusterName,
                                 RedisClusterNode master, RedisClusterNode slave) {
        // TODO Auto-generated method stub

    }

    @Override
    public void migrateSlots(String namespace, String appName, String clusterName, int[] slots,
                             RedisClusterNode src, RedisClusterNode dest) {
        // TODO Auto-generated method stub

    }

    @Override
    public RedisClusterInfo infoCluster(String namespace, String appName, String clusterName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RedisCluster> listClusters() {
        return redisMapper.getAllRedisClusters();
    }

}
