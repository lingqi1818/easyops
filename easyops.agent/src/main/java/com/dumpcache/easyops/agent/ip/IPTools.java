package com.dumpcache.easyops.agent.ip;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPTools {
    private final static Logger LOGGER = LoggerFactory.getLogger(IPTools.class);

    public static String getCity(String ip) {
        IpSearch finder = IpSearch.getInstance();
        String result = finder.Get(ip);
        if (!StringUtils.isEmpty(result)) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(result);
                }
                return result.split("\\|")[3];
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

}
