package com.dumpcache.easyops.web.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtils {
    public static SimpleDateFormat       second_sdf = new SimpleDateFormat("HH:mm:ss");
    public final static SimpleDateFormat day_sdf    = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat full_sdf   = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    public static List<Long> getTwiceMinutePointsByTime(String time) throws ParseException {
        List<Long> list = new ArrayList<Long>();
        long timestamp = day_sdf.parse(time).getTime();
        for (int i = 0; i < 720; i++) {
            list.add(timestamp + i * 1000 * 60 * 2);
        }
        return list;
    }

    public static List<String> getSecondsByCurrent(int distances, int num, boolean plus) {
        List<String> list = new ArrayList<String>();
        Date d = new Date();
        if (plus) {
            for (int i = num; i < distances; i++) {
                long t = d.getTime() + 1000 * i;
                list.add(full_sdf.format(new Date(t)));
            }
        } else {
            for (int i = distances; i > distances - num; i--) {
                long t = d.getTime() - 1000 * i;
                list.add(full_sdf.format(new Date(t)));
            }
        }
        return list;
    }

    public static void main(String args[]) throws ParseException {
        System.out.println(DateUtils.getSecondsByCurrent(0, 100, false));
        System.out.println(DateUtils.getTwiceMinutePointsByTime("201-01-01"));
        System.out.println(DateUtils.day_sdf.format(new Date()));
    }
}
