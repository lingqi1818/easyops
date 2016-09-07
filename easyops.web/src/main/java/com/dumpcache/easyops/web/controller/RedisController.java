package com.dumpcache.easyops.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dumpcache.easyops.redis.dal.entity.RedisCluster;
import com.dumpcache.easyops.redis.service.ClusterRedisServiceImpl;
import com.dumpcache.easyops.redis.service.RedisClusterManager;
import com.dumpcache.easyops.redis.service.RedisClusterManager.RedisClusterInfo;
import com.dumpcache.easyops.redis.service.RedisClusterManager.RedisClusterNode;
import com.dumpcache.easyops.redis.util.Utils;
import com.dumpcache.easyops.web.service.RedisStatService;
import com.dumpcache.easyops.web.service.RedisStatService.Stat;

/**
 * Redis管理控制台
 * 
 * @author chenke
 * @date 2016年8月18日 下午4:50:31
 */
@Controller
public class RedisController {
    @Autowired
    private RedisClusterManager redisClusterManager;
    @Autowired
    private DataSource          dataSource;
    @Autowired
    private RedisStatService    redisStatService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisController.class);

    @RequestMapping("/redis/cluster/list")
    public String clusterList(Model model) {
        List<RedisCluster> clusters = redisClusterManager.listClusters();
        model.addAttribute("clusters", clusters);
        return "redis/cluster/list";
    }

    @RequestMapping("/redis/cluster/add")
    public String addCluster() {
        return "redis/cluster/add";
    }

    @RequestMapping("/redis/hit/addKey")
    public String addHitKey() {
        return "redis/hit/addKey";
    }

    @RequestMapping("/redis/hit/deleteKey")
    public String delete(@RequestParam(value = "key") String key, Model model) {
        try {
            String args[] = key.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length - 1; i++) {
                sb.append(args[i]).append("_");
            }
            redisStatService.deleteMonitorKey(Integer.valueOf(args[args.length - 1]),
                    sb.toString().substring(0, sb.toString().length() - 1));
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "删除key成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "删除key失败，系统内部错误！");
            return "error";
        }
    }

    @RequestMapping("/redis/hit/saveKey")
    public String saveKey(@RequestParam(value = "clusterId") int clusterId,
                          @RequestParam(value = "monitorKey") String monitorKey, Model model) {

        try {
            redisStatService.addMonitorKey(clusterId, monitorKey);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "添加key成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "添加key失败，系统内部错误！");
            return "error";
        }
    }

    @RequestMapping("/redis/hit/keylist")
    public String hitKeyList(Model model) {
        List<Stat> stats = redisStatService.getAllStats();
        model.addAttribute("stats", stats);
        return "/redis/hit/keylist";
    }

    @RequestMapping("/redis/cluster/slave/add")
    public String addSlave(@RequestParam(value = "clusterId") int clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "redis/cluster/slave/add";
    }

    @RequestMapping("/redis/cluster/master/add")
    public String addMaster(@RequestParam(value = "clusterId") int clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "redis/cluster/master/add";
    }

    @RequestMapping("/redis/node/list")
    public String nodeList(Model model) {
        // model.addAttribute("clusterId", clusterId);
        return "redis/node/list";
    }

    @RequestMapping("/redis/cluster/slave/create")
    public String addSlave(@RequestParam(value = "masterHost") String masterHost,
                           @RequestParam(value = "masterPort") int masterPort,
                           @RequestParam(value = "slaveHost") String slaveHost,
                           @RequestParam(value = "slavePort") int slavePort,
                           @RequestParam(value = "clusterId") int clusterId, Model model) {
        RedisClusterNode master = new RedisClusterNode(masterHost, masterPort);
        RedisClusterNode slave = new RedisClusterNode(slaveHost, slavePort);
        try {
            redisClusterManager.addSlaveToMaster(clusterId, master, slave);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "添加slave节点成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "添加slave节点失败，系统内部错误！");
            return "error";
        }
    }

    @RequestMapping("/redis/cluster/master/create")
    public String addMaster(@RequestParam(value = "masterHost") String masterHost,
                            @RequestParam(value = "masterPort") int masterPort,
                            @RequestParam(value = "clusterId") int clusterId, Model model) {
        RedisClusterNode master = new RedisClusterNode(masterHost, masterPort);
        try {
            redisClusterManager.addNodesToCluster(clusterId, master);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "添加master节点成功！");
            return "error";
        } catch (Exception ex) {
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "添加master节点失败，系统内部错误！");
            return "error";
        }
    }

    @RequestMapping("/redis/cluster/info")
    public String infoCluster(@RequestParam(value = "clusterId") int clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "redis/cluster/info";
    }

    @RequestMapping("/redis/cluster/migrate")
    public String migrate(@RequestParam(value = "clusterId") int clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "redis/cluster/migrate";
    }

    @RequestMapping("/redis/cluster/keySearch")
    public String keySearch(@RequestParam(value = "clusterId") int clusterId, Model model) {
        model.addAttribute("clusterId", clusterId);
        return "redis/cluster/keySearch";
    }

    @RequestMapping("/redis/cluster/doKeySearch")
    public String doKeySearch(@RequestParam(value = "namespace") String namespace,
                              @RequestParam(value = "app") String appName,
                              @RequestParam(value = "kv_key") String key,
                              @RequestParam(value = "clusterId") int clusterId, Model model) {
        ClusterRedisServiceImpl redisService = new ClusterRedisServiceImpl();
        try {
            redisService.setDataSource(dataSource);
            redisService.setClusterId(clusterId);
            redisService.setNamespace(namespace);
            redisService.setAppName(appName);
            redisService.init();
            model.addAttribute("result", redisService.get(key));
        } finally {
            redisService.close();
        }
        return "redis/cluster/keySearchResult";
    }

    @RequestMapping("/redis/cluster/doMigrate")
    public @ResponseBody String doMigrate(@RequestParam(value = "clusterId") int clusterId,
                                          @RequestParam(value = "startSlot") int startSlot,
                                          @RequestParam(value = "endSlot") int endSlot,
                                          @RequestParam(value = "srcIp") String srcIp,
                                          @RequestParam(value = "srcPort") int srcPort,
                                          @RequestParam(value = "destIp") String destIp,
                                          @RequestParam(value = "destPort") int destPort) {
        RedisClusterNode src = new RedisClusterNode(srcIp, srcPort);
        RedisClusterNode dest = new RedisClusterNode(destIp, destPort);
        redisClusterManager.migrateSlots(clusterId, Utils.formatToSlotsArray(startSlot, endSlot),
                src, dest);
        return "success";
    }

    @RequestMapping("/redis/cluster/getMigrateInfo")
    public @ResponseBody int doMigrate(@RequestParam(value = "clusterId") int clusterId) {
        return redisClusterManager.getMigrateProcess(clusterId);
    }

    @RequestMapping("/redis/cluster/getInfo")
    public @ResponseBody RedisClusterInfo getClusterInfo(@RequestParam(value = "clusterId") int clusterId) {
        return redisClusterManager.infoCluster(clusterId);
    }

    @RequestMapping("/redis/cluster/create")
    public String createCluster(@RequestParam(value = "clusterName") String clusterName,
                                @RequestParam(value = "clusterNodes") String clusterNodes,
                                Model model) {
        try {
            if (StringUtils.isEmpty(clusterName) || StringUtils.isEmpty(clusterNodes)) {
                model.addAttribute("statusCode", 300);
                model.addAttribute("msg", "集群名称和集群节点不能为空！");
                return "error";
            }
            List<RedisClusterNode> nodes = formatClusterNodesToList(clusterNodes);
            if (nodes.size() < 3) {
                model.addAttribute("statusCode", 300);
                model.addAttribute("msg", "节点数量必须大于等于3！");
                return "error";
            }
            redisClusterManager.createRedisCluster(clusterName, nodes);
            model.addAttribute("statusCode", 200);
            model.addAttribute("msg", "创建集群成功！");
            return "error";
        } catch (Exception e) {
            LOGGER.error("createCluster failed:", e);
            model.addAttribute("statusCode", 300);
            model.addAttribute("msg", "添加的集群节点失败，系统内部错误！");
            return "error";
        }
    }

    private List<RedisClusterNode> formatClusterNodesToList(String clusterNodes) throws Exception {
        List<RedisClusterNode> list = new ArrayList<RedisClusterNode>();
        clusterNodes = clusterNodes.replaceAll("，", ",");
        clusterNodes = clusterNodes.replaceAll("：", ":");
        String[] nodes = clusterNodes.split(",");
        if (nodes != null) {
            for (String node : nodes) {
                String[] kv = node.split(":");
                if (kv.length != 2) {
                    throw new Exception("clusterNodes 格式不正确，" + clusterNodes);
                }
                RedisClusterNode rn = new RedisClusterNode();
                rn.setHost(kv[0]);
                rn.setPort(Integer.valueOf(kv[1]));
                list.add(rn);
            }
        }
        return list;
    }

}
