package com.dumpcache.easyops.java.test;

public class TestBean {
    private TestBean1 testBean1;

    public void test1() {
        System.out.println("test1");
        //test2();
        testBean1.test4();
        testBean1.test4();
        testBean1.test4();
    }

    public void test2() {
        System.out.println("test2");
        test3();
    }

    public void test3() {
        System.out.println("test3");
    }

    public void setTestBean1(TestBean1 testBean1) {
        this.testBean1 = testBean1;
    }

}
