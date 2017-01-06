package com.dumpcache.easyops.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java调用分析器
 * 
 * @author chenke
 * @date 2016年12月22日 上午11:02:44
 */
public class Profiler {
    private static final Logger                                        LOGGER  = LoggerFactory
            .getLogger(Profiler.class);
    private static ThreadLocal<Stack<MutablePair<String, Long>>>       sHolder = new ThreadLocal<Stack<MutablePair<String, Long>>>();
    private static ThreadLocal<List<MutablePair<String, Long>>>        lHolder = new ThreadLocal<List<MutablePair<String, Long>>>();
    private static ArrayBlockingQueue<List<MutablePair<String, Long>>> queue   = new ArrayBlockingQueue<List<MutablePair<String, Long>>>(
            1024);
    private static ThreadPoolExecutor                                  tpe     = new ThreadPoolExecutor(
            1, 1, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1024),
            new DiscardAndLogPolicy());

    static {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        List<MutablePair<String, Long>> pList = queue.take();
                        parsePairs(pList);
                    } catch (InterruptedException e) {
                        LOGGER.error("Java Profiler ThreadPoolExecutor rejectedExecution:", e);
                    }
                }
            }

            private void parsePairs(List<MutablePair<String, Long>> pList) {
                if (pList != null && pList.size() > 0) {
                    StringBuilder sb = new StringBuilder("callstack:");
                    for (int i = pList.size() - 1; i >= 0; i--) {
                        MutablePair<String, Long> p = pList.get(i);
                        sb.append(p.getLeft()).append(":").append(p.getRight());
                        if (i != 0) {
                            sb.append(",");
                        }
                    }
                    System.out.println(sb.toString());
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void enter(String classname, String method, long timestamp) {
        Stack<MutablePair<String, Long>> stack = sHolder.get();
        if (stack == null) {
            stack = new Stack<MutablePair<String, Long>>();
            sHolder.set(stack);
        }
        MutablePair<String, Long> p = new MutablePair<String, Long>(classname + "#" + method,
                timestamp);
        stack.push(p);
    }

    public static void release(String classname, String method, long timestamp) {
        Stack<MutablePair<String, Long>> stack = sHolder.get();
        if (stack != null) {
            MutablePair<String, Long> p = stack.pop();
            p.setRight(timestamp - p.getRight());
            List<MutablePair<String, Long>> list = lHolder.get();
            if (list == null) {
                list = new ArrayList<MutablePair<String, Long>>();
                lHolder.set(list);
            }
            list.add(p);
            if (stack.isEmpty()) {
                final List<MutablePair<String, Long>> sublist = list;
                tpe.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            queue.offer(sublist, 100, TimeUnit.MICROSECONDS);
                        } catch (InterruptedException e) {
                            LOGGER.error("offer java profiler list to queue error:", e);
                        }
                    }
                });
                sHolder.remove();
                lHolder.remove();
            }
        }
    }

    private static class DiscardAndLogPolicy extends ThreadPoolExecutor.DiscardPolicy {
        public DiscardAndLogPolicy() {
            super();
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            LOGGER.error("Java Profiler ThreadPoolExecutor rejectedExecution:", e);
            super.rejectedExecution(r, e);
        }
    }
}
