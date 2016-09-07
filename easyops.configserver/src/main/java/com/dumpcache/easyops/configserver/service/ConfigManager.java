package com.dumpcache.easyops.configserver.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 配置管理器
 * 
 * @author chenke
 * @date 2016年5月14日 上午11:00:56
 */
public interface ConfigManager {
    public void saveConfig(String namespace, String app, String key, String val);

    public void saveConfig(String app, String key, String val);

    public void deleteConfig(String namespace, String app, String key);

    public void deleteConfig(String app, String key);

    public String getConfigTree(String namespace, String app);

    public String getConfigTree(String app);

    public List<Config> listAllConfigs(int start, int count);

    public int getAllConfigsCount();

    public List<Config> listConfigs(String namespace, String app, int start, int count);

    public int getConfigsCount(String namespace, String app);

    public List<String> getAllNamespaces();

    public List<String> getAllApps();

    public Config getConfigById(int id);

    public void deleteConfigById(int id);

    public static class Config {
        private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private int                     id;
        private String                  namespace;
        private String                  app;
        private String                  key;
        private String                  value;
        private Date                    gmtCreated;
        private Date                    gmtModified;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSimpleVal() {
            if (this.value != null && this.value.length() > 10) {
                return this.value.substring(0, 10) + "...";
            } else {
                return this.value;
            }
        }

        public String getFormatGmtCreated() {
            if (gmtCreated == null) {
                return "";
            }
            return sdf.format(gmtCreated);
        }

        public String getFormatGmtModified() {
            if (gmtModified == null) {
                return "";
            }
            return sdf.format(gmtModified);
        }

        public Date getGmtCreated() {
            return gmtCreated;
        }

        public void setGmtCreated(Date gmtCreated) {
            this.gmtCreated = gmtCreated;
        }

        public Date getGmtModified() {
            return gmtModified;
        }

        public void setGmtModified(Date gmtModified) {
            this.gmtModified = gmtModified;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

}
