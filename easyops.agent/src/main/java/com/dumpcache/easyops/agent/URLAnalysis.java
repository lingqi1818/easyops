package com.dumpcache.easyops.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * URLAnalysis
 * 
 * @author chenke
 * @date 2016年11月6日 下午5:04:16
 */
public class URLAnalysis {
    private Map<String, String> paramMap = new HashMap<String, String>();

    public void analysis(String url) {
        paramMap.clear();
        if (!"".equals(url)) {// 如果URL不是空字符串
            url = url.substring(url.indexOf('?') + 1);
            String paramaters[] = url.split("&");
            for (String param : paramaters) {
                String values[] = param.split("=");
                if (values != null && values.length == 2) {
                    paramMap.put(values[0], values[1]);
                }
            }
        }
    }

    public String getParam(String name) {
        return paramMap.get(name);
    }

    public static void main(String[] args) {
        String test = "http://xxx.com?name=helddlo&id=100";
        URLAnalysis urlAnalysis = new URLAnalysis();
        urlAnalysis.analysis(test);
        System.out.println("name = " + urlAnalysis.getParam("name"));
        System.out.println("id = " + urlAnalysis.getParam("id"));
    }
}
