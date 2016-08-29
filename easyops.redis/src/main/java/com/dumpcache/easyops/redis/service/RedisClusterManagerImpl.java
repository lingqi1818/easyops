package com.dumpcache.easyops.redis.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;
import com.dumpcache.easyops.redis.dal.mapper.RedisMapper;
import com.dumpcache.easyops.redis.service.RedisClusterManager.RedisClusterNodeInfo.Role;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.util.SafeEncoder;

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
            addNodesToCluster(nodes);
            initClusterSlots(nodes);
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

    @SuppressWarnings("static-access")
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
            //jedis.flushAll();
            jedis.clusterFlushSlots();
            jedis.clusterAddSlots(finalslots);
            jedis.close();
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("static-access")
    private void addNodesToCluster(List<RedisClusterNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (nodes.get(i).getHost().equals(nodes.get(j).getHost())
                        && nodes.get(i).getPort() == nodes.get(j).getPort()) {
                    continue;
                }
                Jedis jedis = new Jedis(nodes.get(i).getHost(), nodes.get(i).getPort());
                jedis.clusterMeet(nodes.get(j).getHost(), nodes.get(j).getPort());
                jedis.close();
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);

                }
            }
        }
    }

    @Override
    public void dismissRedisCluster(int clusterId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addNodesToCluster(int clusterId, RedisClusterNode... nodes) {
        RedisClusterInfo clusterInfo = infoCluster(clusterId);
        Jedis jedis1 = new Jedis(clusterInfo.getNodesInfo().get(0).getNode().getHost(),
                clusterInfo.getNodesInfo().get(0).getNode().getPort());
        for (RedisClusterNode node : nodes) {
            jedis1.clusterMeet(node.getHost(), node.getPort());
        }
        jedis1.close();
    }

    @Override
    public void delNodesFromCluster(int clusterId, RedisClusterNode... nodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addSlaveToMaster(int clusterId, RedisClusterNode master, RedisClusterNode slave) {
        RedisClusterInfo clusterInfo = infoCluster(clusterId);
        Jedis jedis1 = new Jedis(clusterInfo.getNodesInfo().get(0).getNode().getHost(),
                clusterInfo.getNodesInfo().get(0).getNode().getPort());
        jedis1.clusterMeet(slave.getHost(), slave.getPort());
        Jedis jedis = new Jedis(slave.getHost(), slave.getPort());
        List<RedisClusterNodeInfo> nodes = clusterInfo.getNodesInfo();
        for (RedisClusterNodeInfo node : nodes) {
            if (master.getHost().equals(node.getNode().getHost())
                    && master.getPort() == node.getNode().getPort()) {
                try {
                    jedis.clusterReplicate(node.getNode().getNodeId());
                } catch (Exception ex) {
                    LOGGER.error("cluster Replicate failed," + ex);
                }
                break;
            }
        }
        jedis.close();
        jedis1.close();
    }

    private String getNodeIdFromRedisClusterInfo(RedisClusterInfo clusterInfo,
                                                 RedisClusterNode src) {
        List<RedisClusterNodeInfo> nodes = clusterInfo.getNodesInfo();
        if (nodes != null) {
            for (RedisClusterNodeInfo node : nodes) {
                if (src.getHost().equals(node.getNode().getHost())
                        && src.getPort() == node.getNode().getPort()) {
                    return node.getNode().getNodeId();
                }
            }
        }
        return null;
    }

    @Override
    public void migrateSlots(final int clusterId, final int[] slots, final RedisClusterNode src,
                             final RedisClusterNode dest) {
        RedisCluster rc = redisMapper.getRedisClusterById(clusterId);
        if (!RedisCluster.STATUS_MIGRATEING.equalsIgnoreCase(rc.getStatus())) {
            rc.setStatus(RedisCluster.STATUS_MIGRATEING);
            rc.setMigrateProcess(0);
            redisMapper.updateRedisCluster(rc);
        }
        Thread migrateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                _migrateSlots(clusterId, slots, src, dest);
            }
        });
        migrateThread.setName("easyops-redis-mirate-thread");
        migrateThread.start();
    }

    private void _migrateSlots(int clusterId, int[] slots, RedisClusterNode src,
                               RedisClusterNode dest) {
        RedisClusterInfo clusterInfo = infoCluster(clusterId);
        String srcNodeId = getNodeIdFromRedisClusterInfo(clusterInfo, src);
        String destNodeId = getNodeIdFromRedisClusterInfo(clusterInfo, dest);
        Jedis srcJedis = new Jedis(src.getHost(), src.getPort());
        Jedis destJedis = new Jedis(dest.getHost(), dest.getPort());
        try {
            if (slots != null) {
                Long totalKeys = 0L;
                for (int slot : slots) {
                    Long count = srcJedis.clusterCountKeysInSlot(slot);
                    if (count != null) {
                        totalKeys += count;
                    }
                }
                long process = 0;
                for (int slot : slots) {
                    try {
                        srcJedis.clusterSetSlotMigrating(slot, destNodeId);
                        destJedis.clusterSetSlotImporting(slot, srcNodeId);
                        List<String> keys;
                        while ((keys = srcJedis.clusterGetKeysInSlot(slot, 10000)) != null
                                && keys.size() > 0) {
                            for (String key : keys) {
                                try {
                                    srcJedis.migrate(dest.getHost(), dest.getPort(), key, 0, 5000);
                                    process++;
                                    if (process % 1000 == 0) {
                                        RedisCluster rc = redisMapper
                                                .getRedisClusterById(clusterId);
                                        rc.setMigrateProcess(
                                                Long.valueOf(process * 100 / totalKeys).intValue());
                                        redisMapper.updateRedisCluster(rc);
                                    }
                                } catch (Exception ex) {
                                    LOGGER.error("migrate error:" + ex);
                                }
                            }
                        }
                        destJedis.clusterSetSlotNode(slot, destNodeId);
                        LOGGER.info("migrate slot:" + slot + " success !!!");
                    } catch (Exception ex) {
                        LOGGER.error("set slot migrate or import error:" + ex);
                    }
                }

                RedisCluster rc = redisMapper.getRedisClusterById(clusterId);
                rc.setMigrateProcess(100);
                rc.setStatus(RedisCluster.STATUS_NORMAL);
                redisMapper.updateRedisCluster(rc);
            }
            LOGGER.info("migrate all slots success !!!");
        } finally {
            srcJedis.close();
            destJedis.close();
        }

    }

    private Set<HostAndPort> formateServerConfigToNodes(String serverConfig) throws Exception {
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        String[] serverStrs = serverConfig.replace(";", ",").split(",");
        for (String serverStr : serverStrs) {
            String serverKv[] = serverStr.split(":");
            if (serverKv.length != 2) {
                throw new Exception("the server format is error:" + serverKv);
            }

            nodes.add(new HostAndPort(serverKv[0], Integer.valueOf(serverKv[1])));
        }

        return nodes;
    }

    @Override
    public RedisClusterInfo infoCluster(int clusterId) {
        RedisClusterInfo clusterInfo = new RedisClusterInfo();
        List<RedisClusterNodeInfo> nodesInfo = new ArrayList<RedisClusterNodeInfo>();
        clusterInfo.setNodesInfo(nodesInfo);
        RedisCluster rc = redisMapper.getRedisClusterById(clusterId);
        if (rc == null || StringUtils.isEmpty(rc.getMasterNodes())) {
            throw new RuntimeException(
                    "can't get redis cluster config from db,clusterId :" + clusterId);
        }
        Set<HostAndPort> nodes;
        try {
            nodes = formateServerConfigToNodes(rc.getMasterNodes());
        } catch (Exception e) {
            throw new RuntimeException(
                    "redis cluster config format is error," + rc.getMasterNodes());
        }
        if (nodes != null && nodes.size() > 0) {
            for (HostAndPort host : nodes) {
                Jedis jedis = new Jedis(host.getHost(), host.getPort());
                try {
                    getNodesInfoFromJedis(jedis, nodesInfo);
                    getSlotsInfoFromJedis(jedis, nodesInfo);
                    getClusterInfoFromJedis(jedis, clusterInfo);
                    break;

                } catch (Exception ex) {
                    //try next HostAndPort
                } finally {
                    jedis.close();
                }
            }
        }
        return clusterInfo;
    }

    private void getClusterInfoFromJedis(Jedis jedis, RedisClusterInfo clusterInfo) {
        String info = jedis.clusterInfo();
        if (info == null) {
            throw new RuntimeException("get cluster info error.");
        }

        if (!StringUtils.isEmpty(info)) {
            String[] lines = info.split("\r\n");
            if (lines != null) {
                for (String line : lines) {
                    String kv[] = line.split(":");
                    if (kv.length != 2) {
                        LOGGER.error("cluster info line error:" + line);
                    }
                    clusterInfo.addClusterInfo(kv[0], kv[1]);

                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void getSlotsInfoFromJedis(Jedis jedis, List<RedisClusterNodeInfo> nodesInfo) {
        List<Object> slots = jedis.clusterSlots();
        if (slots != null) {
            for (Object slotObject : slots) {
                List<Object> slot = (List<Object>) slotObject;
                if (slot.size() <= 2) {
                    continue;
                }

                int start = ((Long) slot.get(0)).intValue();
                int end = ((Long) slot.get(1)).intValue();
                LOGGER.debug("start:" + start + ",end:" + end);
                for (int i = 2; i < slot.size(); i++) {
                    List<Object> hostAndPort = (List<Object>) slot.get(i);
                    String host = SafeEncoder.encode((byte[]) hostAndPort.get(0));
                    int port = ((Long) hostAndPort.get(1)).intValue();
                    //String hostId = SafeEncoder.encode((byte[]) hostAndPort.get(2));
                    for (RedisClusterNodeInfo node : nodesInfo) {
                        if (node.getNode().getHost().equalsIgnoreCase(host)
                                && node.getNode().getPort() == port) {
                            Pair<Integer, Integer> p = new ImmutablePair<Integer, Integer>(start,
                                    end);
                            node.getSlotPairs().add(p);
                        }
                    }
                }
            }
        }

    }

    private void getNodesInfoFromJedis(Jedis jedis, List<RedisClusterNodeInfo> nodesInfo) {
        String[] lines = jedis.clusterNodes().split("\n");
        for (String line : lines) {
            String strs[] = line.split(" ");
            if (strs != null && strs.length >= 8) {
                RedisClusterNodeInfo rinfo = new RedisClusterNodeInfo();
                rinfo.getNode().setNodeId(strs[0]);
                String hkv[] = strs[1].split(":");
                if (hkv.length != 2) {
                    LOGGER.error("why host format is error?" + line);
                }
                rinfo.getNode().setHost(hkv[0]);
                rinfo.getNode().setPort(Integer.valueOf(hkv[1]));
                if (strs[2].toLowerCase().contains("master")) {
                    rinfo.setRole(Role.MASTER);
                } else if (strs[2].toLowerCase().contains("slave")) {
                    rinfo.setRole(Role.SLAVE);
                } else {
                    rinfo.setRole(Role.UNKNOW);
                }
                rinfo.setStatus(strs[7]);
                try {
                    getRedisNodeInfoFromJedis(rinfo);
                } catch (Exception ex) {
                    LOGGER.error("info redis failed, node is:" + rinfo.getNode().getHost() + ":"
                            + rinfo.getNode().getPort());
                }
                nodesInfo.add(rinfo);
            }
        }
    }

    private void getRedisNodeInfoFromJedis(RedisClusterNodeInfo nodeInfo) {
        RedisClusterNode node = nodeInfo.getNode();
        Jedis jedis = null;
        try {
            jedis = new Jedis(node.getHost(), node.getPort());
            nodeInfo.setKeys(jedis.dbSize());
            String info = jedis.info();
            if (!StringUtils.isEmpty(info)) {
                String lines[] = info.split("\n");
                for (String line : lines) {
                    if (!"#".startsWith(line)) {
                        String kv[] = line.split(":");
                        if (kv.length == 2)
                            node.getNodeInfo().put(kv[0], kv[1]);
                    }
                }
            }
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public List<RedisCluster> listClusters() {
        return redisMapper.getAllRedisClusters();
    }

    @Override
    public int getMigrateProcess(int clusterId) {
        RedisCluster rc = redisMapper.getRedisClusterById(clusterId);
        if (rc != null) {
            return rc.getMigrateProcess();
        }
        return 0;
    }

}
