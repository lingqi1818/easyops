package com.dumpcache.easyops.configserver;

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

}
