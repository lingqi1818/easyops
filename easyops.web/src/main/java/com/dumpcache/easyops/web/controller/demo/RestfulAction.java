package com.dumpcache.easyops.web.controller.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 本示例仅简单介绍如何使用URI Template Patterns开发restful接口 详细请参考：
 * 
 * <pre>
 * http://docs.spring.io/spring/docs/4.0.5.RELEASE/spring-framework-reference/htmlsingle/#mvc-introduction
 * </pre>
 * 
 * 中的URI Template Patterns部分
 * 
 * @author chenke
 * @date 2014-5-29 19:41:43
 */
@Controller
public class RestfulAction {
    /**
     * 获取uri变量
     * 
     * @param ownerId
     * @return
     */
    @RequestMapping(value = "/owners/{ownerId}", method = RequestMethod.GET)
    public @ResponseBody
    String findOwner(@PathVariable String ownerId) {
        return ownerId;
    }

    /**
     * 获取多个uri参数
     * 
     * @param ownerId
     * @param petId
     * @return
     */
    @RequestMapping(value = "/pets/{ownerId}/pets/{petId}", method = RequestMethod.GET)
    public @ResponseBody
    String findPet(@PathVariable String ownerId, @PathVariable String petId) {
        return petId;
    }

    /**
     * 测试正则表达式
     * 
     * @param version
     * @param extension
     */
    @RequestMapping("/spring-web/{version:[a-z]}")
    public @ResponseBody
    String testReg(@PathVariable String version) {
        return version;
    }

}
