package com.dumpcache.easyops.agent.accesslog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumpcache.easyops.agent.URLAnalysis;
import com.dumpcache.easyops.agent.ip.IPTools;
import com.dumpcache.easyops.redis.service.RedisService;
import com.dumpcache.easyops.redis.service.RedisServiceImpl;

/**
 * NGX日志收集器
 * 
 * @author chenke
 * @date 2016年11月6日 下午5:14:12
 */
public class NGXLogCollector implements LogCollector, Runnable {
    private final static Logger           LOGGER = LoggerFactory.getLogger(LogCollector.class);
    private final static SimpleDateFormat sdf    = new SimpleDateFormat("yyyy-MM-dd");
    private RedisService                  redisService;
    private int                           threadId;
    private BufferedReader                br;

    public NGXLogCollector(String path, String redis, int threadId) {
        this.threadId = threadId;
        try {
            Process p = Runtime.getRuntime().exec("tail -n 1 -F " + path);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //this.accesslog = new RandomAccessFile(path, "r");
        } catch (Exception e) {
            LOGGER.error("load ngx access log error:", e);
            return;
        }

        redisService = new RedisServiceImpl(redis);
    }

    public void run() {
        try {
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                String line = br.readLine();
                if (line != null) {
                    //redisService.lPush("ngx_access_log", line);
                    try {
                        parseLineAndLog(line);
                    } catch (Exception ex) {
                        LOGGER.error("parseLineAndLog log error:", ex);
                    }
                    try {
                        if (i++ % 100 == 0) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("read ngx access log error:", e);
        }
    }

    private void parseLineAndLog(String line) {
        String[] strs = line.split(" ");
        String ip = strs[0];
        String city = IPTools.getCity(ip);
        if (StringUtils.isEmpty(city)) {
            city = "UNKNOW";
        }
        Date d = new Date();
        //秒pv
        String s_pv = "s_pv_" + sdf.format(d) + "_" + strs[1].substring(13, 21);
        redisService.incr(s_pv);
        redisService.expire(s_pv, 60 * 60 * 24 * 7);
        //日pv
        String d_pv = "d_pv_" + sdf.format(d);
        redisService.incr(d_pv);
        redisService.expire(d_pv, 60 * 60 * 24 * 180);
        //城市秒pv
        String c_s_pv = city + "_s_pv_" + sdf.format(d) + "_" + strs[1].substring(13, 21);
        redisService.incr(c_s_pv);
        redisService.expire(c_s_pv, 60 * 60 * 24 * 7);
        //城市日pv
        String c_d_pv = city + "_d_pv_" + sdf.format(d);
        redisService.incr(c_d_pv);
        redisService.expire(c_d_pv, 60 * 60 * 24 * 180);
        String url = strs[4];
        //分析下单数据
        if (url.startsWith("/zmw/v2/submit_order") || url.startsWith("/app-customer/order/submit")
                || url.startsWith("/customer/order/submit")) {
            URLAnalysis urlAnalysis = new URLAnalysis();
            urlAnalysis.analysis(strs[9]);
            String price = "0";
            price = urlAnalysis.getParam("order_price");
            if (StringUtils.isEmpty(price)) {
                price = urlAnalysis.getParam("orderPrice");
            }
            if (!StringUtils.isEmpty(price)) {
                price = price.replaceAll("\"", "");
                //日gmv
                String gmv_pv = "gmv_" + sdf.format(d);
                redisService.incrBy(gmv_pv, Double.valueOf(price).intValue());
                redisService.expire(gmv_pv, 60 * 60 * 24 * 180);
                //城市日gmv
                String c_gmv_pv = city + "_gmv_" + sdf.format(d);
                redisService.incrBy(c_gmv_pv, Double.valueOf(price).intValue());
                redisService.expire(c_gmv_pv, 60 * 60 * 24 * 180);
                //日下单
                String order = "order_" + sdf.format(d);
                redisService.incr(order);
                redisService.expire(order, 60 * 60 * 24 * 180);
                //城市日下单
                String c_order = city + "_order_" + sdf.format(d);
                redisService.incr(c_order);
                redisService.expire(c_order, 60 * 60 * 24 * 180);
            }
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.setName("NGXLogCollector-Thread-" + threadId);
        t.setDaemon(true);
        t.start();

    }

    public static void main(String args[]) throws Exception {
        String line = "117.136.40.218 [06/Nov/2016:12:39:57 +0800] \"POST /zmw/v2/submit_order HTTP/1.1\" 200 0.114 59 \"userId=1e74c417bfee4c0eb1d1aa1ed9fd8a8f&guid=73D88D15-D4D5-416F-AB72-CDE480E3D107&app_name=client&user_id=1e74c417bfee4c0eb1d1aa1ed9fd8a8f&device_name=xuanxuan%E7%9A%84%20iPhone%20%282%29&latitude=22543188.000000&city=440300&order_price=70&version=3.0.0&os=10.0.2&product_id=ca1a483d43954a27aa9427ff3e65f047&token=CYCF3N2obaOiTvzNJEwT&longitude=113982376.000000&mobile=13510453080&openUDID=8d4a1390fa5885f5fdce4692d037abcffc4a94f8&model=iPhone&device_screen_height=667.000000&identifierForVendor=A9351BFB-C4DF-4851-BBA4-0A124EEE8D25&reserve_time=2016-11-12%2014%3A00&device_type=iOS&device_screen_width=375.000000&address=%E5%B9%BF%E4%B8%9C%E7%9C%81%E6%B7%B1%E5%9C%B3%E5%B8%82%E5%8D%97%E5%B1%B1%E5%8C%BA%E6%B7%B1%E5%8D%97%E5%A4%A7%E9%81%939028%E5%8F%B7&is_reserve=true\" \"-\" \"%E6%B2%B3%E7%8B%B8%E5%AE%B6/3000 CFNetwork/808.0.2 Darwin/16.0.0\" \"-\" 0.196 Upstream:\"10.0.120.42:8200\" - set_cookie=\"accessToken=CYCF3N2obaOiTvzNJEwT; userId=1e74c417bfee4c0eb1d1aa1ed9fd8a8f; search_test=1; search_r=45; beacon_id=MTAxLjI1MS4yMTQuMTMwLTFDNjEzLTUyRTJCQkM4RTI4QUUtMjg#-\"";
        String[] strs = line.split(" ");
        System.out.println(strs[0]);
        System.out.println(strs[1].substring(1, 12));
        System.out.println(strs[1].substring(13, 21));
        System.out.println(sdf.format(new Date()));
        System.out.println(strs[4]);
        System.out.println(strs[9]);
        URLAnalysis urlAnalysis = new URLAnalysis();
        urlAnalysis.analysis(strs[9]);
        System.out.println(urlAnalysis.getParam("order_price"));
    }

}
