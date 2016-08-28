package com.dumpcache.easyops.web.service;

public class LoginServiceImpl implements LoginService {

    @Override
    public String doLogin(String username, String password) {
        if ("admin".equals(username) && "9a1996efc97181f0aee18321aa3b3b12".equals(password)) {
            return "success";
        } else {
            return null;
        }

    }

}
