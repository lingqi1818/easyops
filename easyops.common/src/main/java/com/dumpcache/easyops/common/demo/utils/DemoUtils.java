package com.dumpcache.easyops.common.demo.utils;

/**
 * demo util 本类仅为演示使用，并无实际用处 理论上util为最稳定的抽象，所以类应该是final并且不可初始化 of DemoUtils
 * 
 * @author chenke
 */
final public class DemoUtils {

    private DemoUtils() {
    }

    public static String doLogic(String p) {
        System.out.println("do demo util logic");
        return p;
    }

}
