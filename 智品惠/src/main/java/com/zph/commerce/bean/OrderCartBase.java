package com.zph.commerce.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 确认订单对象
 */
public class OrderCartBase implements Serializable {
    private Address addr_info;

    private List<GoodsInfo> goods_info;
    private int goods_num;
    private String point;
    private int goods_type;
    private List<RankInfo> rank_info;
    private UserInfo user_info;

    private Shipping shipping;
    private List<Shipping> shippings;



    public Address getAddr_info() {
        return addr_info;
    }

    public List<GoodsInfo> getGoods_info() {
        return goods_info;
    }

    public int getGoods_num() {
        return goods_num;
    }

    public String getPoint() {
        return point;
    }

    public int getGoods_type() {
        return goods_type;
    }

    public List<RankInfo> getRank_info() {
        return rank_info;
    }

    public UserInfo getUser_info() {
        return user_info;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public List<Shipping> getShippings() {
        return shippings;
    }
}
