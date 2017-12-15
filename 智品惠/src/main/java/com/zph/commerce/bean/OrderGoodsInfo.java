package com.zph.commerce.bean;

import java.io.Serializable;

/**
 * Created by yuguang on 2017/10/28.
 */

public class OrderGoodsInfo implements Serializable{

    private String name;
    private String  pic;
    private String  value;
    private String  price;
    private int  number;
    private String point;
    private String price_cost;
    private String good_id;
    private String attr_id;
    private String status;
    public String getName() {
        return name;
    }

    public String getPic() {
        return pic;
    }

    public String getValue() {
        return value;
    }

    public String getPrice() {
        return price;
    }

    public int getNumber() {
        return number;
    }

    public String getPoint() {
        return point;
    }

    public String getPrice_cost() {
        return price_cost;
    }

    public String getGood_id() {
        return good_id;
    }

    public String getAttr_id() {
        return attr_id;
    }

    public String getStatus() {
        return status;
    }
}
