package com.dumpcache.easyops.agent.accesslog;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.dumpcache.easyops.agent.URLAnalysis;
import com.dumpcache.easyops.agent.ip.IPTools;
import com.dumpcache.easyops.redis.service.RedisService;
import com.dumpcache.easyops.redis.service.RedisServiceImpl;

/**
 * NGX日志统计
 * 
 * @author chenke
 * @date 2016年11月6日 下午11:38:29
 */
public class NGXLogStatistics implements LogStatistics, Runnable {
    //private final static Logger           LOGGER = LoggerFactory.getLogger(NGXLogStatistics.class);
    private int                           threadId;
    private RedisService                  redisService;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    

    public NGXLogStatistics(int threadId, String redis) {
        this.threadId = threadId;
        this.redisService = new RedisServiceImpl(redis);
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String line = redisService.rPop("ngx_access_log");
            if (line != null)
                parseLineAndLog(line);
        }

    }

    private void parseLineAndLog(String line) {
        String[] strs = line.split(" ");
        String ip = strs[0];
        String city = IPTools.getCity(ip);
        if (StringUtils.isEmpty(city)) {
            city = "UNKNOW";
        }
        //秒pv
        String s_pv = "s_pv_" + strs[1].substring(13, 21);
        redisService.incr(s_pv);
        redisService.expire(s_pv, 60 * 60 * 24 + 60);
        //日pv
        String d_pv = "d_pv_" + sdf.format(new Date());
        redisService.incr(d_pv);
        redisService.expire(s_pv, 60 * 60 * 24 * 180);
        //城市秒pv
        String c_s_pv = city + "_s_pv_" + strs[1].substring(13, 21);
        redisService.incr(c_s_pv);
        redisService.expire(c_s_pv, 60 * 60 * 24 + 60);
        //城市日pv
        String c_d_pv = city + "_d_pv_" + sdf.format(new Date());
        redisService.incr(c_d_pv);
        redisService.expire(c_d_pv, 60 * 60 * 24 * 180);
        String url = strs[4];
        //分析下单数据
        if ("/zmw/v2/submit_order".equals(url)) {
            URLAnalysis urlAnalysis = new URLAnalysis();
            urlAnalysis.analysis(strs[9]);
            String price = urlAnalysis.getParam("order_price");
            if (!StringUtils.isEmpty(price)) {
                //日gmv
                String gmv_pv = "gmv_" + sdf.format(new Date());
                redisService.incrBy(gmv_pv, Integer.valueOf(price));
                redisService.expire(gmv_pv, 60 * 60 * 24 + 60);
                //城市日gmv
                String c_gmv_pv = city + "_gmv_" + sdf.format(new Date());
                redisService.incrBy(c_gmv_pv, Integer.valueOf(price));
                redisService.expire(c_gmv_pv, 60 * 60 * 24 + 60);
                //日下单
                String order = "order_" + sdf.format(new Date());
                redisService.incr(order);
                redisService.expire(order, 60 * 60 * 24 + 60);
                //城市日下单
                String c_order = city + "_order_" + sdf.format(new Date());
                redisService.incr(c_order);
                redisService.expire(c_order, 60 * 60 * 24 + 60);
            }
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.setName("NGXLogStatistics-Thread-" + threadId);
        t.setDaemon(true);
        t.start();

    }

}
