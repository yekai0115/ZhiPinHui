package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/21.
 */

public class ExchangeInfo {

    private int status;
    private String points;//可兑换鼓励积分、可兑换货款
    private String bankname;
    private String remarks;

    private String can_cash;//兑换成功后返回的可兑换鼓励积分

    private String can_cash1;//冻结的鼓励积分、冻结的货款


    public int getStatus() {
        return status;
    }

    public String getPoints() {
        return points;
    }

    public String getBankname() {
        return bankname;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getCan_cash() {
        return can_cash;
    }

    public String getCan_cash1() {
        return can_cash1;
    }
}
