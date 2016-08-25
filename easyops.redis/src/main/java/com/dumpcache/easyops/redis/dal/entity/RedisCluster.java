package com.dumpcache.easyops.redis.dal.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class RedisCluster {
    private int                     id;
    private String                  clusterName;
    private String                  masterNodes;
    private String                  slaveNodes;
    private String                  status;
    private Date                    gmtCreated;
    private Date                    gmtModified;
    private int                     migrateProcess;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public int getMasterSize() {
        if (StringUtils.isEmpty(masterNodes)) {
            return 0;
        }
        return masterNodes.split(",").length;
    }

    public String getFormatGmtCreated() {
        return sdf.format(gmtCreated);
    }

    public String getFormatGmtModified() {
        return sdf.format(gmtModified);
    }

    public int getMigrateProcess() {
        return migrateProcess;
    }

    public void setMigrateProcess(int migrateProcess) {
        this.migrateProcess = migrateProcess;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getMasterNodes() {
        return masterNodes;
    }

    public void setMasterNodes(String masterNodes) {
        this.masterNodes = masterNodes;
    }

    public String getSlaveNodes() {
        return slaveNodes;
    }

    public void setSlaveNodes(String slaveNodes) {
        this.slaveNodes = slaveNodes;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

}
