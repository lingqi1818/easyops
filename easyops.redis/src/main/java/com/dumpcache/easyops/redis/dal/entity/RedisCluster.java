package com.dumpcache.easyops.redis.dal.entity;

import java.util.Date;

public class RedisCluster {
    private int    id;
    private String clusterName;
    private String masterNodes;
    private String slaveNodes;
    private Date   gmtCreated;
    private Date   gmtModified;

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
