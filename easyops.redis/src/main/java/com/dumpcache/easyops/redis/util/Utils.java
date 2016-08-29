package com.dumpcache.easyops.redis.util;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;

public class Utils {

    public static Set<HostAndPort> formateServerConfigToNodes(String serverConfig)
            throws Exception {
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        String[] serverStrs = serverConfig.replace(";", ",").split(",");
        for (String serverStr : serverStrs) {
            String serverKv[] = serverStr.split(":");
            if (serverKv.length != 2) {
                throw new Exception("the server format is error:" + serverKv);
            }

            nodes.add(new HostAndPort(serverKv[0], Integer.valueOf(serverKv[1])));
        }

        return nodes;
    }

    public static int[] formatToSlotsArray(int start, int end) {
        int size = end - start + 1;
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = start++;
        }
        return slots;
    }

    public static void main(String args[]) {
        int[] slots = Utils.formatToSlotsArray(1, 100);
        for (int slot : slots) {
            System.out.println(slot);
        }
    }
}
