package com.dumpcache.easyops.web.service;

import java.util.List;

public interface RedisStatService {
    public static class Stat {
        private int    clusterId;
        private String keyStart;
        private String hitRate;

        public int getClusterId() {
            return clusterId;
        }

        public void setClusterId(int clusterId) {
            this.clusterId = clusterId;
        }

        public String getKeyStart() {
            return keyStart;
        }

        public void setKeyStart(String keyStart) {
            this.keyStart = keyStart;
        }

        public String getHitRate() {
            return hitRate;
        }

        public void setHitRate(String hitRate) {
            this.hitRate = hitRate;
        }

    }

    public List<Stat> getAllStats();

    public void addMonitorKey(int clusterId, String key);

    public void deleteMonitorKey(int clusterId, String key);
}
