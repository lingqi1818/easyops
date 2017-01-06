package com.dumpcache.easyops.java;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Java Profiler请求拦截器，用于拦截需要监控的Java请求
 * 
 * @author chenke
 * @date 2017年1月6日 上午11:55:55
 */
public class ProfilerAspect {

    public Object aroundMethod(ProceedingJoinPoint pjd) throws Throwable {

        String className = pjd.getSignature().getDeclaringTypeName();
        String methodName = pjd.getSignature().getName();
        try {
            Profiler.enter(className, methodName, System.currentTimeMillis());
            return pjd.proceed();
        } finally {
            Profiler.release(className, methodName, System.currentTimeMillis());
        }
    }
}
