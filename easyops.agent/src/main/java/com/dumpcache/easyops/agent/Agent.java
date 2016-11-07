package com.dumpcache.easyops.agent;

import java.io.File;
import java.util.Properties;

import com.dumpcache.easyops.agent.accesslog.NGXLogCollector;
import com.dumpcache.easyops.agent.accesslog.NGXLogStatistics;

/**
 * 机器数据采集代理程序
 * 
 * @author chenke
 * @date 2016年11月5日 下午3:36:24
 */
public class Agent {

    public static void main(String[] args) throws InterruptedException, Exception {
        String mode = args[0];
        Properties pros = new Properties();
        pros.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("agent.properties"));
        String redis = pros.getProperty("redis.server");
        if ("agent".equalsIgnoreCase(mode)) {
            String ngxLogP = pros.getProperty("ngx.accesslog");
            String ngxLogs[] = ngxLogP.replaceAll(";", ",").split(",");
            int id = 0;
            for (String ngxLog : ngxLogs) {
                try {
                    if (new File(ngxLog).exists()) {
                        new NGXLogCollector(ngxLog, redis, id++).start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("easyops agent is start success !");
        }

//        if ("server".equalsIgnoreCase(mode)) {
//            int serverN = 1;
//            try {
//                serverN = Integer.valueOf(args[1]);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            for (int i = 0; i < serverN; i++) {
//                new NGXLogStatistics(i, redis).start();
//            }
//            System.out.println("easyops server is start success !");
//        }
        Thread.currentThread().join();
    }

}
