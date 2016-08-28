package com.dumpcache.easyops.configserver.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dumpcache.easyops.configserver.service.AbstractConfigServiceImpl;
import com.dumpcache.easyops.configserver.service.ConfigManager;

import junit.framework.TestCase;

public class MysqlConfigServiceTest extends TestCase {
    private ConfigManager configManager;

    @SuppressWarnings("resource")
    public void setUp() {
        ApplicationContext ac = new ClassPathXmlApplicationContext(
                new String[] { "spring/spring-datasource.xml", "spring/spring-service.xml" });
        configManager = ac.getBean(ConfigManager.class);
        AbstractConfigServiceImpl cs = (AbstractConfigServiceImpl) configManager;
        cs.init();
    }

    public void testGetConfig() {
        for (int i = 0; i < 1000; i++) {
            configManager.saveConfig("app1", "k" + i, "v" + i);
        }
        System.out.println(configManager.getAllConfigsCount());
    }

}
