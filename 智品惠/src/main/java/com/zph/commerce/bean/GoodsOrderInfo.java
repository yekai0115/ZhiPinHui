package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/28.
 */

public class GoodsOrderInfo {

    private String goods_info;

    private String nocan_points;
    private String totalprice;
    private String buildtime;
    private String postage;
    public String getGoods_info() {
        return goods_info;
    }

    public void setGoods_info(String goods_info) {
        this.goods_info = goods_info;
    }

    public String getNocan_points() {
        return nocan_points;
    }

    public void setNocan_points(String nocan_points) {
        this.nocan_points = nocan_points;
    }

    public String getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(String totalprice) {
        this.totalprice = totalprice;
    }

    public String getBuildtime() {
        return buildtime;
    }

    public void setBuildtime(String buildtime) {
        this.buildtime = buildtime;
    }

    public String getPostage() {
        return postage;
    }

    public void setPostage(String postage) {
        this.postage = postage;
    }
}
