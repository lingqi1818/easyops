package com.dumpcache.easyops.configserver;

/**
 * 混合工具类
 * 
 * @author chenke
 * @date 2016年5月13日 下午6:57:18
 */
public class MiscUtils {
    public static String pathOf(String parent, String children) {
        return parent + "/" + children;
    }

    public static String configRootPath() {
        return pathOf("", "config_server");
    }

    public static String configNamespacePath(String namespace) {
        return pathOf(configRootPath(), namespace);
    }

    public static String appPath(String namespace, String app) {
        return pathOf(configNamespacePath(namespace), app);
    }

    public static String keyPath(String namespace, String app, String key) {
        return pathOf(appPath(namespace, app), key);
    }
}
