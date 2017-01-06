package com.dumpcache.easyops.java;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Java Profiler请求拦截器，用于拦截需要监控的Java请求
 * 
 * @author chenke
 * @date 2017年1月6日 上午11:55:55
 */
public class ProfilerInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Profiler.enter(invocation.getMethod().getDeclaringClass().getName(),
                invocation.getMethod().getName(), System.currentTimeMillis());
        try {
            return invocation.proceed();
        } finally {
            Profiler.release(invocation.getMethod().getDeclaringClass().getName(),
                    invocation.getMethod().getName(), System.currentTimeMillis());
        }
    }

}
