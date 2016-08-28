package com.dumpcache.easyops.configserver.service;

/**
 * zookeeper客户端
 * 
 * @author chenke
 * @date 2016年5月14日 下午5:46:02
 */
public interface ZKClient {
    void start();

    void close();
}
