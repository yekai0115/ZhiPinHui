package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/28.
 */

public class PayData {

    private int status;//订单状态
    private String order_sn;
    private String pay_return_id;
    private String add_time;
    private String paytime;
    private String update_time;
    public String getOrder_sn() {
        return order_sn;
    }

    public String getPay_return_id() {
        return pay_return_id;
    }

    public String getAdd_time() {
        return add_time;
    }

    public String getPaytime() {
        return paytime;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public int getStatus() {
        return status;
    }
}
