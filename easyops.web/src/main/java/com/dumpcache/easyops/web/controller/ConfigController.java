package com.dumpcache.easyops.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ConfigController {
    @RequestMapping("/config/list")
    public String list(Model model) {
        //model.addAttribute("clusters", clusters);
        return "config/list";
    }

}
