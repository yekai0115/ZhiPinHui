package com.zph.commerce.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 物流
 * @author yg
 */
public class LogisticsBase implements Serializable {

  private String  number;
    private String  type;
    private int  deliverystatus;
    private int issign;
    private List<Logistics> list;


    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public int getDeliverystatus() {
        return deliverystatus;
    }

    public int getIssign() {
        return issign;
    }

    public List<Logistics> getList() {
        return list;
    }
}
