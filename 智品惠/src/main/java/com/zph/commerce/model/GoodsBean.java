package com.zph.commerce.model;

import com.zph.commerce.bean.CommentContent;
import com.zph.commerce.bean.Specifications;

import java.util.List;

/**
 * Created by Administrator on 2017/7/3 0003.
 */

public class GoodsBean {

    private String gid;
    private String gname;
    private String goods_logo;
    private String price;
    private String price_cost;//原价
    private String point;
    private String  goods_sold;//销售量
    private String goods_attr;
    private String detail;
    private String goods_number;
    private String status;
    private int goods_type;
    private List<Specifications> attr_detail;
    private int commentNum;
    private CommentContent commentContent;
    private String headurl;

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }

    public String getGoods_logo() {
        return goods_logo;
    }

    public void setGoods_logo(String goods_logo) {
        this.goods_logo = goods_logo;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getGoods_attr() {
        return goods_attr;
    }

    public void setGoods_attr(String goods_attr) {
        this.goods_attr = goods_attr;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getGoods_number() {
        return goods_number;
    }

    public void setGoods_number(String goods_number) {
        this.goods_number = goods_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGoods_sold() {
        return goods_sold;
    }

    public List<Specifications> getAttr_detail() {
        return attr_detail;
    }

    public int getGoods_type() {
        return goods_type;
    }

    public String getPrice_cost() {
        return price_cost;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public CommentContent getCommentContent() {
        return commentContent;
    }

    public String getHeadurl() {
        return headurl;
    }
}
