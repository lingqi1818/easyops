package com.dumpcache.easyops.web.controller.demo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dumpcache.easyops.web.vo.demo.Student;

/**
 * 通用控制器（非restful风格）
 * 
 * @date 2014-5-29 19:41:43
 * @author chenke
 */
@Controller
public class GeneralAction {
    Logger log = LoggerFactory.getLogger("demo-log");

    /**
     * url后带参数的http请求
     * 
     * @param name url中“？”号分割的参数name=xxx，如果为空，默认值为World
     * @param model用于向模板传递context请求
     * @return
     */
    @RequestMapping("/hello")
    public String hello(@RequestParam(value = "name", required = false, defaultValue = "World") String name,
                        Model model) {
        log.debug("log in hello uri");
        model.addAttribute("name", name);
        return "demo/hello";
    }

    /**
     * 本方法仅支持post请求
     */
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public String testPost(@RequestParam(value = "p1") String p1, Model model) {
        System.out.println(p1);
        model.addAttribute("p1", p1);
        return "demo/post";
    }

    /**
     * 获取cookie信息
     * 
     * @param fooCookie
     * @return
     */
    @RequestMapping("/getCookie")
    public @ResponseBody String getCookie(@CookieValue(value = "foo") String fooCookie) {
        return fooCookie;
    }

    /**
     * 写cookie
     * 
     * @param response
     * @return
     */
    @RequestMapping("/writeCookie")
    public @ResponseBody String writeCookie(HttpServletResponse response) {
        Cookie foo = new Cookie("foo", "bar"); //bake cookie
        foo.setMaxAge(1000); //set expire time to 1000 sec       
        response.addCookie(foo); //put cookie in response 
        return "success!";
    }

    /**
     * 返回json串
     * 
     * @return pojo对象，渲染后自动转json
     */
    @RequestMapping(value = "/hello/json", produces = "application/json")
    public @ResponseBody Student testJson(HttpServletResponse response) {
        Student s = new Student();
        s.setAge(100);
        s.setName("shabi");
        return s;
    }

    //TODO jsonp支持
    public void testJsonp() {
    }

    public void writeSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("k1", "v1");
        //===============================================
        String val = (String) session.getAttribute("k1");
        System.out.println(val);
    }
}
