package com.dumpcache.easyops.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MonitorController {

    @RequestMapping("/monitor/pvuv")
    public String execute() {
        return "monitor/pvuv";
    }
}
