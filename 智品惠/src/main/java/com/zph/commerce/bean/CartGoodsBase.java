package com.zph.commerce.bean;

public class CartGoodsBase {

	private String id;
	private String goods_attr_id;
	private String goods_logo;
	private String gname;
	private String attr_value;
	private String attr_point;
	private String attr_price;
	private String price_cost;
	private String number;
	private int goods_type;
	private boolean isChecked;
	public String getId() {
		return id;
	}

	public String getGoods_attr_id() {
		return goods_attr_id;
	}

	public String getGoods_logo() {
		return goods_logo;
	}

	public String getGname() {
		return gname;
	}

	public String getAttr_value() {
		return attr_value;
	}

	public String getAttr_point() {
		return attr_point;
	}

	public String getAttr_price() {
		return attr_price;
	}

	public String getPrice_cost() {
		return price_cost;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getGoods_type() {
		return goods_type;
	}

	public void setGoods_attr_id(String goods_attr_id) {
		this.goods_attr_id = goods_attr_id;
	}
}
