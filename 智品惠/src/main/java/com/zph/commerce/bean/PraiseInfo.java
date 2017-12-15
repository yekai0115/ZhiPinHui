package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/21.
 */

public class PraiseInfo {


    private String point;//积分
    private int type;//类型
    private String remarks;//备注
    private String add_time;//消费时间
    private int status;
    private String pointStatus;

    private int id;
    private int uid;


    public String getPoint() {
        return point;
    }

    public int getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getAdd_time() {
        return add_time;
    }

    public int getId() {
        return id;
    }

    public int getUid() {
        return uid;
    }

    public String getPointStatus() {
        return pointStatus;
    }
}
