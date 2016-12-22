package com.dumpcache.easyops.redis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisServiceImpl implements RedisService {

    private static final Logger logger    = LoggerFactory.getLogger(RedisServiceImpl.class);

    private List<JedisPool>     jedisPool = new ArrayList<JedisPool>();

    private String              servers;

    private int                 minIdle   = 5;
    private int                 maxIdle   = 10;
    private int                 maxTotal  = 20;
    private int                 maxWait   = 5000;

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

    public RedisServiceImpl() {
    }

    public RedisServiceImpl(String servers) {
        this.servers = servers;
        init();
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public void init() {
        if (StringUtils.isEmpty(servers)) {
            throw new IllegalArgumentException("the servers is empty .");
        }

        String[] serverStrs = this.servers.replace(";", ",").split(",");
        for (String serverStr : serverStrs) {
            String serverKv[] = serverStr.split(":");
            if (serverKv.length != 2) {
                logger.error("the server format is error:" + serverKv);
            }

            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMinIdle(minIdle);
            config.setMaxIdle(maxIdle);
            config.setMaxTotal(maxTotal);
            config.setMaxWaitMillis(maxWait);
            config.setTestOnBorrow(false);
            config.setTestOnReturn(true);
            config.setTestWhileIdle(true);
            config.setNumTestsPerEvictionRun(1);
            config.setJmxEnabled(false);
            config.setSoftMinEvictableIdleTimeMillis(-1);//代表空闲时间
            config.setTimeBetweenEvictionRunsMillis(30000);//代表回收周期
            JedisPool pool = new JedisPool(config, serverKv[0], Integer.valueOf(serverKv[1]));
            jedisPool.add(pool);
        }
    }

    public void sadd(String key, String... members) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.sadd(key, members);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }

    }

    public Long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            jedis.close();
        }
    }

    public Set<String> smembers(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.smembers(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private JedisPool getJedisPool(String key) {
        int hash = (key.hashCode() & 0x7FFFFFFF);
        int index = hash % jedisPool.size();
        return jedisPool.get(index);
    }

    public void srem(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.srem(key, new String[] { member });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public boolean sismember(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.set(key, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.get(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void del(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.del(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public void expire(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.expire(key, seconds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void setnx(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void setex(String key, int seconds, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public Long decrBy(String key, long integer) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.decrBy(key, integer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long decr(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.decr(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long incrBy(String key, long integer) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.incrBy(key, integer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public Long incr(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.incr(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public void append(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            jedis.append(key, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.get(0).getResource();
            jedis.subscribe(jedisPubSub, channels);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.get(0).getResource();
            return jedis.publish(channel, message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long lPush(String key, String... values) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.get(0).getResource();
            return jedis.lpush(key, values);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public String rPop(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.get(0).getResource();
            return jedis.rpop(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long zcount(String key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long zcount(String key, String min, String max) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long zadd(String key, double score, String member) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.zadd(key, score, member);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public ScanResult<String> sScan(String key, String cursor, ScanParams scanParams) {
        Jedis jedis = null;
        try {
            jedis = getJedisPool(key).getResource();
            return jedis.sscan(key, cursor, scanParams);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub    
    }

    @Override
    public List<String> mget(String... keys) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.get(0).getResource();
            return jedis.mget(keys);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

}
