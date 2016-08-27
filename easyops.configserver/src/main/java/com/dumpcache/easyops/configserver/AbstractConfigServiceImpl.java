package com.dumpcache.easyops.configserver;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfigServiceImpl implements ConfigService, ConfigManager {
    private final static Logger               LOGGER         = LoggerFactory
            .getLogger(AbstractConfigServiceImpl.class);
    protected volatile boolean                isInit         = false;
    protected Map<String, String>             configMap      = new ConcurrentHashMap<String, String>();
    protected Map<String, IntervalTimeConfig> intervalConfig = new ConcurrentHashMap<String, IntervalTimeConfig>();

    private static class IntervalTimeConfig {
        private long updateTime;
        private long interval;

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public String toString() {
            return "updateTime:" + new Date(updateTime).toString();
        }

    }

    public void init() {
        if (isInit) {
            LOGGER.warn("config service is already init !");
            return;
        }
        internalInit();
        isInit = true;
    }

    abstract protected void internalInit();

    @Override
    public String getConfig(String namespace, String app, String key, long localInvalidTime) {
        if (!isInit) {
            LOGGER.error("config service is not init !!!");
            throw new IllegalStateException("config service is not init !!!");
        }
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(app)
                || StringUtils.isEmpty(key)) {
            LOGGER.error("namespace or app or key is null !!!");
            throw new IllegalArgumentException("namespace or app or key  is null !!!");
        }

        if (localInvalidTime > 0) {
            IntervalTimeConfig tc = intervalConfig.get(MiscUtils.keyPath(namespace, app, key));
            if (tc != null
                    && (System.currentTimeMillis() - tc.getUpdateTime()) >= tc.getInterval()) {
                configMap.remove(MiscUtils.keyPath(namespace, app, key));
            }
        }

        String config = configMap.get(MiscUtils.keyPath(namespace, app, key));
        if (config != null) {
            return config;
        }

        config = getConfigFromRemote(namespace, app, key);
        if (config != null) {
            configMap.put(MiscUtils.keyPath(namespace, app, key), config);
        }
        IntervalTimeConfig tc = intervalConfig.get(MiscUtils.keyPath(namespace, app, key));
        if (tc == null) {
            tc = new IntervalTimeConfig();
            tc.setInterval(localInvalidTime);
            intervalConfig.put(MiscUtils.keyPath(namespace, app, key), tc);
        }
        tc.setUpdateTime(System.currentTimeMillis());
        return config;
    }

    abstract protected String getConfigFromRemote(String namespace, String app, String key);

    @Override
    public String getConfig(String app, String key, long localInvalidTime) {
        return getConfig("default", app, key, localInvalidTime);
    }

    @Override
    public void saveConfig(String namespace, String app, String key, String val) {
        if (!isInit) {
            LOGGER.error("config client is not init !!!");
            throw new IllegalStateException("cofnig client is not init !!!");
        }
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(app) || StringUtils.isEmpty(key)
                || StringUtils.isEmpty(val)) {
            LOGGER.error("namespace or app or key or val is null !!!");
            throw new IllegalArgumentException("namespace or app or key or val is null !!!");
        }

        saveConfigToRemote(namespace, app, key, val);

    }

    abstract protected void saveConfigToRemote(String namespace, String app, String key,
                                               String val);

    @Override
    public void saveConfig(String app, String key, String val) {
        saveConfig("default", app, key, val);
    }

    @Override
    public String getConfigTree(String app) {
        return getConfigTree("default", app);
    }

    @Override
    public void deleteConfig(String namespace, String app, String key) {
        if (!isInit) {
            LOGGER.error("config service is not init !!!");
            throw new IllegalStateException("config service is not init !!!");
        }
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(app)
                || StringUtils.isEmpty(key)) {
            LOGGER.error("namespace or app or key  is null !!!");
            throw new IllegalArgumentException("namespace or app or key  is null !!!");
        }

        deleteRemoteConfig(namespace, app, key);
    }

    abstract protected void deleteRemoteConfig(String namespace, String app, String key);

    @Override
    public void deleteConfig(String app, String key) {
        deleteConfig("default", app, key);
    }

    @Override
    public String getConfigTree(String namespace, String app) {
        if (!isInit) {
            LOGGER.error("config service is not init !!!");
            throw new IllegalStateException("config service is not init !!!");
        }
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(app)) {
            LOGGER.error("namespace or app is null !!!");
            throw new IllegalArgumentException("namespace or app is null !!!");
        }
        return getConfigTreeFromRemote(namespace, app);
    }

    protected abstract String getConfigTreeFromRemote(String namespace, String app);
}
