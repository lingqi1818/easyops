package com.dumpcache.easyops.agent.ip;

public class IPTools {
    public static String getCity(String ip) {
        IpSearch finder = IpSearch.getInstance();
        String result = finder.Get(ip);
        if (result != null) {
            try {
                return result.split("\\|")[3];
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

}
