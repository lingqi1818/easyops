package com.dumpcache.easyops.web.vo.demo;

import java.io.Serializable;

/**
 * 学生类，本类仅用于测试
 * 
 * @author chenke
 */
public class Student implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String            name;                 // 名字
    private int               age;                  // 年龄

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
