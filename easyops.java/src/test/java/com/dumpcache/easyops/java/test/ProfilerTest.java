package com.dumpcache.easyops.java.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class ProfilerTest extends TestCase {
    private TestBean tb;

    public void setUp() {
        @SuppressWarnings("resource")
        ApplicationContext ac = new ClassPathXmlApplicationContext("profiler1.xml");
        tb = (TestBean) ac.getBean("testBean");
    }

    public void testTestBean() {
        tb.test1();
    }

    
}
