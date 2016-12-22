package com.dumpcache.easyops.web.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dumpcache.easyops.redis.service.RedisService;
import com.dumpcache.easyops.redis.service.RedisServiceImpl;
import com.dumpcache.easyops.web.utils.DateUtils;
import com.dumpcache.easyops.web.vo.CityInfo;
import com.dumpcache.easyops.web.vo.Qps;

@Controller
public class MonitorController {
    private static List<String> citys = new ArrayList<String>();
    private static RedisService rs    = new RedisServiceImpl("10.0.10.136:6379");
    static {
        citys.add("北京");
        citys.add("上海");
        citys.add("深圳");
        citys.add("杭州");
        citys.add("成都");
        citys.add("广州");
        citys.add("武汉");
        citys.add("南京");
        citys.add("天津");
        citys.add("重庆");
        citys.add("西安");
    }

    @RequestMapping("/monitor/snapshots")
    public String snapshots(@RequestParam(value = "city") String city,
                            @RequestParam(value = "day") String day, Model model) {
        boolean isFound = false;
        List<String> pvs = new ArrayList<String>();
        List<String> gmvs = new ArrayList<String>();
        List<String> orders = new ArrayList<String>();
        for (String c : citys) {
            if (c.equals(city)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            city = "全国";
        }
        List<Long> timestamps = null;
        try {
            timestamps = DateUtils.getTwiceMinutePointsByTime(day);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        List<String> times = new ArrayList<String>();
        Map<String, String> timesPV = new HashMap<String, String>();
        for (Long t : timestamps) {
            String time = DateUtils.second_sdf.format(new Date(t));
            times.add(time);
            if ("全国".equals(city)) {
                Long total = 0L;
                String[] keys = new String[120];
                for (long i = 0L; i < 120L; i++) {
                    Long tt = t + 1000L * i;
                    keys[(int) i] = "s_pv_" + DateUtils.full_sdf.format(new Date(tt));

                }
                List<String> vals = rs.mget(keys);
                if (vals != null) {
                    for (String val : vals) {
                        if (!StringUtils.isEmpty(val)) {
                            total += Long.valueOf(val);
                        }
                    }
                }
                timesPV.put(time, String.valueOf(total / 120L));
            } else {
                Long total = 0L;
                String[] keys = new String[120];
                for (long i = 0L; i < 120L; i++) {
                    Long tt = t + 1000L * i;
                    keys[(int) i] = city + "_s_pv_" + DateUtils.full_sdf.format(new Date(tt));

                }
                List<String> vals = rs.mget(keys);
                if (vals != null) {
                    for (String val : vals) {
                        if (!StringUtils.isEmpty(val)) {
                            total += Long.valueOf(val);
                        }
                    }
                }
                timesPV.put(time, String.valueOf(total / 120L));
            }
        }

        String key = day;
        for (String c : citys) {
            String c_d_pv = rs.get(c + "_d_pv_" + key);
            if (StringUtils.isEmpty(c_d_pv)) {
                pvs.add("0");
            } else {
                pvs.add(c_d_pv);
            }
            String c_gmv_pv = rs.get(c + "_gmv_" + key);
            if (StringUtils.isEmpty(c_gmv_pv)) {
                gmvs.add("0");
            } else {
                gmvs.add(c_gmv_pv);
            }
            String c_order = rs.get(c + "_order_" + key);
            if (StringUtils.isEmpty(c_order)) {
                orders.add("0");
            } else {
                orders.add(c_order);
            }
        }

        CityInfo ci = new CityInfo();
        if ("全国".equals(city)) {
            String d_pv = rs.get("d_pv_" + key);
            String gmv_pv = rs.get("gmv_" + key);
            String order = rs.get("order_" + key);
            ci.setGmv(gmv_pv);
            ci.setOrderNum(order);
            ci.setPv(d_pv);
        } else {
            String c_d_pv = rs.get(city + "_d_pv_" + key);
            String c_gmv_pv = rs.get(city + "_gmv_" + key);
            String c_order = rs.get(city + "_order_" + key);
            ci.setGmv(c_gmv_pv);
            ci.setOrderNum(c_order);
            ci.setPv(c_d_pv);
        }
        model.addAttribute("city", city);
        model.addAttribute("times", times);
        model.addAttribute("timesPV", timesPV);
        model.addAttribute("time", day);
        model.addAttribute("citys", citys);
        model.addAttribute("pvs", pvs);
        model.addAttribute("gmvs", gmvs);
        model.addAttribute("orders", orders);
        model.addAttribute("cityInfo", ci);
        return "monitor/snapshots";
    }

    @RequestMapping("/monitor/pvuv")
    public String pvuv(@RequestParam(value = "city") String city, Model model) {
        boolean isFound = false;
        List<String> pvs = new ArrayList<String>();
        List<String> gmvs = new ArrayList<String>();
        List<String> orders = new ArrayList<String>();

        for (String c : citys) {
            if (c.equals(city)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            city = "全国";
        }
        List<String> times = DateUtils.getSecondsByCurrent(60, 50, false);
        Map<String, String> timesPV = new HashMap<String, String>();
        for (String t : times) {

            if ("全国".equals(city)) {
                String d = rs.get("s_pv_" + t);
                if (d == null)
                    d = "0";
                timesPV.put(t, d);
            } else {
                String d = rs.get(city + "_s_pv_" + t);
                if (d == null)
                    d = "0";
                timesPV.put(t, d);
            }
        }
        String key = DateUtils.day_sdf.format(new Date());
        for (String c : citys) {
            String c_d_pv = rs.get(c + "_d_pv_" + key);
            if (StringUtils.isEmpty(c_d_pv)) {
                pvs.add("0");
            } else {
                pvs.add(c_d_pv);
            }
            String c_gmv_pv = rs.get(c + "_gmv_" + key);
            if (StringUtils.isEmpty(c_gmv_pv)) {
                gmvs.add("0");
            } else {
                gmvs.add(c_gmv_pv);
            }
            String c_order = rs.get(c + "_order_" + key);
            if (StringUtils.isEmpty(c_order)) {
                orders.add("0");
            } else {
                orders.add(c_order);
            }
        }
        model.addAttribute("city", city);
        model.addAttribute("times", times);
        model.addAttribute("timesPV", timesPV);
        model.addAttribute("time", DateUtils.day_sdf.format(new Date()));
        model.addAttribute("citys", citys);
        model.addAttribute("pvs", pvs);
        model.addAttribute("gmvs", gmvs);
        model.addAttribute("orders", orders);
        return "monitor/pvuv";
    }

    @RequestMapping(value = "/monitor/qps", produces = "application/json")
    public @ResponseBody Qps getQps(@RequestParam(value = "city") String city) {
        boolean isFound = false;

        for (String c : citys) {
            if (c.equals(city)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            city = "全国";
        }
        Qps q = new Qps();
        String key = DateUtils.full_sdf.format(new Date(new Date().getTime() - 20 * 1000));
        q.setKey(key);
        if ("全国".equals(city)) {
            int v = 0;
            String d = rs.get("s_pv_" + key);
            if (d != null)
                v = Integer.valueOf(d);
            q.setData(v);
        } else {
            int v = 0;
            String d = rs.get(city + "_s_pv_" + key);
            if (d != null)
                v = Integer.valueOf(d);
            q.setData(v);
        }

        return q;
    }

    @RequestMapping(value = "/monitor/cityInfo", produces = "application/json")
    public @ResponseBody CityInfo getCityInfo(@RequestParam(value = "city") String city) {
        boolean isFound = false;

        for (String c : citys) {
            if (c.equals(city)) {
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            city = "全国";
        }
        CityInfo ci = new CityInfo();
        String key = DateUtils.day_sdf.format(new Date());
        if ("全国".equals(city)) {
            String d_pv = rs.get("d_pv_" + key);
            String gmv_pv = rs.get("gmv_" + key);
            String order = rs.get("order_" + key);
            ci.setGmv(gmv_pv);
            ci.setOrderNum(order);
            ci.setPv(d_pv);
        } else {
            String c_d_pv = rs.get(city + "_d_pv_" + key);
            String c_gmv_pv = rs.get(city + "_gmv_" + key);
            String c_order = rs.get(city + "_order_" + key);
            ci.setGmv(c_gmv_pv);
            ci.setOrderNum(c_order);
            ci.setPv(c_d_pv);
        }

        return ci;
    }
}
