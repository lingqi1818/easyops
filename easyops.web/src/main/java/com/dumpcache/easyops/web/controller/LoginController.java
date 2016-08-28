package com.dumpcache.easyops.web.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dumpcache.easyops.web.service.LoginService;
import com.dumpcache.easyops.web.service.LoginServiceImpl;

@Controller
public class LoginController {
    private LoginService loginService = new LoginServiceImpl();

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/doLogin")
    public String login(@RequestParam(value = "username", defaultValue = "") String username,
                        @RequestParam(value = "password", defaultValue = "") String password,
                        Model model, HttpServletResponse response) {

        if ("success".equalsIgnoreCase(loginService.doLogin(username, password))) {
            Cookie login = new Cookie("loginFlag", "success");
            login.setMaxAge(60 * 60 * 24); //过期时间为一天     
            response.addCookie(login);
            return "index";
        } else {
            model.addAttribute("msg", "输入的用户名或者密码出错！");
            return "login";
        }
    }
}
