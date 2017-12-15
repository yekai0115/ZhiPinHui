package com.zph.commerce.bean;

/**
 * Created by yuguang on 2017/10/21.
 */

public class UserBean {

    private String mobile;//手机号码
    private String nick;
    private String sale;//
    private String sales_volume;//小哈累计销量
    private String places;//排单名额
    private String rank;//会员等级
    private int verified;//会员实名认证0未提交,1认证失败,2审核中,3认证通过
    private String pwd;//
    private String surplus;//货款
    private String can_cash;//鼓励积分
    private String nocan_cash;//智品积分
    private String remarks;//认证状态
    private String headurl;//前缀
    private String head;//后缀
    private int derail;
    private String agent;


    public String getMobile() {
        return mobile;
    }

    public String getSale() {
        return sale;
    }

    public String getPlaces() {
        return places;
    }

    public String getRank() {
        return rank;
    }

    public int getVerified() {
        return verified;
    }

    public String getSurplus() {
        return surplus;
    }

    public String getCan_cash() {
        return can_cash;
    }

    public String getNocan_cash() {
        return nocan_cash;
    }

    public String getPwd() {
        return pwd;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getHeadurl() {
        return headurl;
    }

    public String getHead() {
        return head;
    }

    public String getSales_volume() {
        return sales_volume;
    }

    public String getNick() {
        return nick;
    }

    public int getDerail() {
        return derail;
    }

    public String getAgent() {
        return agent;
    }

}
