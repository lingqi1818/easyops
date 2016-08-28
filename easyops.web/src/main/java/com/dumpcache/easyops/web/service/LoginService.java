package com.dumpcache.easyops.web.service;

public interface LoginService {
    /**
     * 登陆完返回sessionId
     * 
     * @param username
     * @param password
     * @return
     */
    public String doLogin(String username, String password);
}
