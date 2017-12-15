package com.zph.commerce.bean;

import java.io.Serializable;

/**
 * 订单详情对象
 */
public class OrderDetals implements Serializable {
    private Address addr_info;
    private GoodsOrderInfo goods_info;
    private PayData payData;


    public Address getAddr_info() {
        return addr_info;
    }

    public GoodsOrderInfo getGoods_info() {
        return goods_info;
    }

    public PayData getPayData() {
        return payData;
    }
}
