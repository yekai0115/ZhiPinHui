package com.zph.commerce.bean;

import java.io.Serializable;

/**
 * Created by yuguang on 2017/10/21.
 */

public class PersonAuth implements Serializable{

    private int status;
    private String name;
    private String card_number;
    private String hand_logo;
    private String front_card;
    private String rear_card;
    private int bank_id;
    private String bank_card;
    private String branch;
    private String bank_logo;
    private String bankname;
    private String add_time;
    private String pic_uri;
    private AuthError   remarks;
    private String province;
    private String city;
    private String county;


    public int getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getCard_number() {
        return card_number;
    }

    public String getHand_logo() {
        return hand_logo;
    }

    public String getFront_card() {
        return front_card;
    }

    public String getRear_card() {
        return rear_card;
    }

    public int getBank_id() {
        return bank_id;
    }

    public String getBank_card() {
        return bank_card;
    }

    public String getBranch() {
        return branch;
    }

    public String getBank_logo() {
        return bank_logo;
    }

    public String getBankname() {
        return bankname;
    }

    public String getAdd_time() {
        return add_time;
    }

    public String getPic_uri() {
        return pic_uri;
    }

    public AuthError getRemarks() {
        return remarks;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public void setHand_logo(String hand_logo) {
        this.hand_logo = hand_logo;
    }

    public void setFront_card(String front_card) {
        this.front_card = front_card;
    }

    public void setRear_card(String rear_card) {
        this.rear_card = rear_card;
    }

    public void setBank_id(int bank_id) {
        this.bank_id = bank_id;
    }

    public void setBank_card(String bank_card) {
        this.bank_card = bank_card;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setBank_logo(String bank_logo) {
        this.bank_logo = bank_logo;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public void setPic_uri(String pic_uri) {
        this.pic_uri = pic_uri;
    }

    public void setRemarks(AuthError remarks) {
        this.remarks = remarks;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCounty(String county) {
        this.county = county;
    }



}
