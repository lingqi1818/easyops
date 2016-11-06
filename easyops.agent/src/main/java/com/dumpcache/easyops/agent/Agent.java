package com.dumpcache.easyops.agent;

import java.io.File;
import java.util.Properties;

import com.dumpcache.easyops.agent.accesslog.NGXLogCollector;

/**
 * 机器数据采集代理程序
 * 
 * @author chenke
 * @date 2016年11月5日 下午3:36:24
 */
public class Agent {

    public static void main(String[] args) throws InterruptedException, Exception {
        Properties pros = new Properties();
        pros.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("agent.properties"));
        String redis = pros.getProperty("redis.server");
        String ngxLogP = pros.getProperty("ngx.accesslog");
        String ngxLogs[] = ngxLogP.replaceAll(";", ",").split(",");
        for (String ngxLog : ngxLogs) {
            try {
                if (new File(ngxLog).exists()) {
                    new NGXLogCollector(ngxLog, redis).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("easyops agent is start success !");
        Thread.currentThread().join();
    }

}
