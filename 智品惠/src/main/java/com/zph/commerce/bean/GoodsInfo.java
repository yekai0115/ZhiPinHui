package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/28.
 */

public class GoodsInfo {

    private String goods_id;
    private String attr_value;
    private String attr_price;
    private String attr_point;
    private String attr_number;
    private String goods_logo;
    private String gname;
    private String goods_type;
    private String shipping_tpl_id;
    private String price_cost;



    private String  postprice;//邮费
    private String  goods_price;//原价
    private int  number;
    private String goods_attr_id;








    public String getGoods_id() {
        return goods_id;
    }

    public String getAttr_value() {
        return attr_value;
    }

    public String getAttr_price() {
        return attr_price;
    }

    public String getAttr_point() {
        return attr_point;
    }

    public String getAttr_number() {
        return attr_number;
    }

    public String getGoods_logo() {
        return goods_logo;
    }

    public String getGname() {
        return gname;
    }

    public String getGoods_type() {
        return goods_type;
    }

    public String getShipping_tpl_id() {
        return shipping_tpl_id;
    }

    public String getPrice_cost() {
        return price_cost;
    }


    public String getPostprice() {
        return postprice;
    }

    public String getGoods_price() {
        return goods_price;
    }

    public void setAttr_number(String attr_number) {
        this.attr_number = attr_number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getGoods_attr_id() {
        return goods_attr_id;
    }

    public void setPostprice(String postprice) {
        this.postprice = postprice;
    }
}
