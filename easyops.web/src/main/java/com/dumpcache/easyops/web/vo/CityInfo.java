package com.dumpcache.easyops.web.vo;

public class CityInfo {
    private String gmv;
    private String pv;
    private String orderNum;

    public String getGmv() {
        if (this.gmv == null)
            return "0";
        return gmv;
    }

    public void setGmv(String gmv) {
        this.gmv = gmv;
    }

    public String getPv() {
        if (this.pv == null)
            return "0";
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getOrderNum() {
        if (this.orderNum == null)
            return "0";
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

}
