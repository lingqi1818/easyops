package com.dumpcache.easyops.web.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtils {
    public static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static List<String> getSecondsByCurrent(int distances, int num, boolean plus) {
        List<String> list = new ArrayList<String>();
        Date d = new Date();
        if (plus) {
            for (int i = num; i < distances; i++) {
                long t = d.getTime() + 1000 * i;
                list.add(sdf.format(new Date(t)));
            }
        } else {
            for (int i = distances; i > distances - num; i--) {
                long t = d.getTime() - 1000 * i;
                list.add(sdf.format(new Date(t)));
            }
        }
        return list;
    }

    public static void main(String args[]) {
        System.out.println(DateUtils.getSecondsByCurrent(120, 30, false).size());
    }
}
