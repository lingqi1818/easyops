package com.dumpcache.easyops.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dumpcache.easyops.configserver.service.ConfigManager;

@Controller
public class ConfigController {
    @Autowired
    private ConfigManager configManager;

    @RequestMapping("/config/list")
    public String list(@RequestParam(value = "numPerPage", defaultValue = "20") int numPerPage,
                       @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                       @RequestParam(value = "namespace", defaultValue = "") String namespace,
                       @RequestParam(value = "app", defaultValue = "") String app, Model model) {
        int start = (pageNum - 1) * numPerPage;
        if (StringUtils.isEmpty(namespace) && StringUtils.isEmpty(app)) {
            model.addAttribute("configs", configManager.listAllConfigs(start, numPerPage));
            model.addAttribute("totalCount", configManager.getAllConfigsCount());
        } else {
            model.addAttribute("configs",
                    configManager.listConfigs(namespace, app, start, numPerPage));
            model.addAttribute("totalCount", configManager.getConfigsCount(namespace, app));
        }
        model.addAttribute("numPerPage", numPerPage);
        model.addAttribute("namespaces", configManager.getAllNamespaces());
        model.addAttribute("apps", configManager.getAllApps());
        model.addAttribute("namespace", namespace);
        model.addAttribute("app", app);
        model.addAttribute("pageNum", pageNum);
        return "config/list";
    }

    @RequestMapping("/config/add")
    public String add() {
        return "config/add";
    }

    @RequestMapping("/config/modify")
    public String modify(@RequestParam(value = "id", defaultValue = "0") int id, Model model) {
        model.addAttribute("config", configManager.getConfigById(id));
        return "config/add";
    }

    @RequestMapping("/config/save")
    public String save(@RequestParam(value = "namespace", defaultValue = "") String namespace,
                       @RequestParam(value = "app", defaultValue = "") String app,
                       @RequestParam(value = "kv_key", defaultValue = "") String key,
                       @RequestParam(value = "kv_val", defaultValue = "") String val, Model model) {
        try {
            configManager.saveConfig(namespace, app, key, val);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "保存配置项成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "保存配置项失败，系统内部错误！");
            return "error";
        }
    }

    @RequestMapping("/config/delete")
    public String delete(@RequestParam(value = "id", defaultValue = "0") int id, Model model) {
        try {
            configManager.deleteConfigById(id);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "删除配置项成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "删除配置项失败，系统内部错误！");
            return "error";
        }
    }

}
