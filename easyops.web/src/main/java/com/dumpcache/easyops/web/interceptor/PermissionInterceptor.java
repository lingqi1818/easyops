package com.dumpcache.easyops.web.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler)
            throws Exception {
        if (request.getRequestURI() != null && request.getRequestURI().contains("login")
                || request.getRequestURI().contains("doLogin")) {
            return true;
        }
        boolean isLogin = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("loginFlag".equalsIgnoreCase(cookie.getName())
                        && "success".equalsIgnoreCase(cookie.getValue())) {
                    isLogin = true;
                }
            }
        }
        if (!isLogin) {
            response.sendRedirect("login");
            return false;
        }
        return true;
    }

}
