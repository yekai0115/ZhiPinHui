package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/28.
 */

public class Shipping {


    private String shipping_id;
    private String shipping_str;
    private String price;
    private String shipping_tpl_id;
    private boolean isCheck;
    private int num;
    public String getShipping_id() {
        return shipping_id;
    }

    public String getShipping_str() {
        return shipping_str;
    }

    public String getPrice() {
        return price;
    }

    public String getShipping_tpl_id() {
        return shipping_tpl_id;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
