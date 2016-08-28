package com.dumpcache.easyops.configserver.service;

/**
 * 动态配置服务
 * 
 * @author chenke
 * @date 2016年5月13日 下午4:53:48
 */
public interface ConfigService {
    public String getConfig(String namespace, String app, String key, long localInvalidTime);

    public String getConfig(String app, String key, long localInvalidTime);
}
