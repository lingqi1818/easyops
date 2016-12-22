package com.dumpcache.easyops.redis.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumpcache.easyops.redis.util.Utils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class ClusterRedisServiceImpl implements RedisService {
    private final static Logger  LOGGER          = LoggerFactory
            .getLogger(ClusterRedisServiceImpl.class);

    private JedisCluster         jedisCluster;

    private int                  minIdle         = 5;
    private int                  maxIdle         = 10;
    private int                  maxTotal        = 20;
    private int                  maxWait         = 5000;
    private int                  timeout         = 500;
    private int                  maxRedirections = 6;
    private String               namespace;
    private String               appName;
    private DataSource           dataSource;
    private int                  clusterId;
    private HitStatisticsService hitStatisticsService;

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setMaxRedirections(int maxRedirections) {
        this.maxRedirections = maxRedirections;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public void init() {
        initRedisClusterClient();
        hitStatisticsService = new HitStatisticsService(jedisCluster, clusterId);
    }

    private String getConfigFromRemote(int clusterId) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn
                    .prepareStatement("select master_nodes from eo_redis_cluster where id=?");
            pst.setInt(1, clusterId);
            ResultSet result = pst.executeQuery();
            if (result != null && result.next()) {
                return result.getString(1);
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return null;
    }

    private void initRedisClusterClient() {
        Set<HostAndPort> hlist = genHostAndPortFromConfigService();
        if (hlist != null && hlist.size() > 0) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMinIdle(minIdle);
            poolConfig.setMaxIdle(maxIdle);
            poolConfig.setMaxTotal(maxTotal);
            poolConfig.setMaxWaitMillis(maxWait);
            poolConfig.setTestOnBorrow(false);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setNumTestsPerEvictionRun(1);
            poolConfig.setJmxEnabled(false);
            poolConfig.setSoftMinEvictableIdleTimeMillis(-1);//代表空闲时间
            poolConfig.setTimeBetweenEvictionRunsMillis(30000);//代表回收周期
            jedisCluster = new JedisCluster(hlist, timeout, maxRedirections, poolConfig);
        }
    }

    private Set<HostAndPort> genHostAndPortFromConfigService() {
        String config = getConfigFromRemote(clusterId);
        if (!StringUtils.isEmpty(config)) {
            try {
                return Utils.formateServerConfigToNodes(config);
            } catch (Exception e) {
                LOGGER.error("host format is error:" + config);
            }
        }
        return null;
    }

    private String decorateKey(String key) {
        return namespace + "_" + appName + "_" + key;
    }

    @Override
    public void set(String key, String value) {
        key = decorateKey(key);
        try {
            jedisCluster.set(key, value);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void expire(String key, int seconds) {
        key = decorateKey(key);
        try {
            jedisCluster.expire(key, seconds);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    public String get(String key) {
        key = decorateKey(key);
        try {
            String result = jedisCluster.get(key);
            if (result == null) {
                hitStatisticsService.stat(key, false);
            } else {
                hitStatisticsService.stat(key, true);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void del(String key) {
        key = decorateKey(key);
        try {
            jedisCluster.del(key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void sadd(String key, String... members) {
        key = decorateKey(key);
        try {
            jedisCluster.sadd(key, members);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public Long scard(String key) {
        key = decorateKey(key);
        try {
            return jedisCluster.scard(key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Set<String> smembers(String key) {
        key = decorateKey(key);
        try {
            Set<String> result = jedisCluster.smembers(key);
            if (result == null) {
                hitStatisticsService.stat(key, false);
            } else {
                hitStatisticsService.stat(key, true);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void srem(String key, String member) {
        key = decorateKey(key);
        try {
            jedisCluster.srem(key, member);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean sismember(String key, String member) {
        key = decorateKey(key);
        return jedisCluster.sismember(key, member);
    }

    @Override
    public void setnx(String key, String value) {
        key = decorateKey(key);
        try {
            jedisCluster.setnx(key, value);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    public void setex(String key, int seconds, String value) {
        key = decorateKey(key);
        try {
            jedisCluster.setex(key, seconds, value);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    public Long decrBy(String key, long integer) {
        key = decorateKey(key);
        try {
            return jedisCluster.decrBy(key, integer);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long decr(String key) {
        key = decorateKey(key);
        try {
            return jedisCluster.decr(key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long incrBy(String key, long integer) {
        key = decorateKey(key);
        try {
            return jedisCluster.incrBy(key, integer);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long incr(String key) {
        key = decorateKey(key);
        try {
            return jedisCluster.incr(key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void append(String key, String value) {
        key = decorateKey(key);
        try {
            jedisCluster.append(key, value);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        try {
            jedisCluster.subscribe(jedisPubSub, channels);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public Long publish(String channel, String message) {
        try {
            return jedisCluster.publish(channel, message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long lPush(String key, String... values) {
        key = decorateKey(key);
        try {
            return jedisCluster.lpushx(key, values);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String rPop(String key) {
        key = decorateKey(key);
        try {
            return jedisCluster.rpop(key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long zcount(String key, double min, double max) {
        key = decorateKey(key);
        try {
            return jedisCluster.zcount(key, min, max);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long zcount(String key, String min, String max) {
        key = decorateKey(key);
        try {
            return jedisCluster.zcount(key, min, max);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long zadd(String key, double score, String member) {
        key = decorateKey(key);
        try {
            return jedisCluster.zadd(key, score, member);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        key = decorateKey(key);
        try {
            return jedisCluster.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        key = decorateKey(key);
        try {
            return jedisCluster.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ScanResult<String> sScan(String key, String cursor, ScanParams scanParams) {
        key = decorateKey(key);
        try {
            return jedisCluster.sscan(key, cursor, scanParams);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        try {
            this.jedisCluster.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public List<String> mget(String... keys) {
        // TODO Auto-generated method stub
        return null;
    }

}
