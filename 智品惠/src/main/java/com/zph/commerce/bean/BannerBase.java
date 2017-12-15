package com.zph.commerce.bean;

import java.util.List;

/**
 * 首页banner、活动图片
 */

public class BannerBase {

    private List<BannerBean> banner1;
    private List<BannerBean> banner2;
    private List<BannerBean> banner3;
    private List<BannerBean> banner4;
    private String fronturl;
    private Campaign activity;

    public List<BannerBean> getBanner1() {
        return banner1;
    }

    public List<BannerBean> getBanner2() {
        return banner2;
    }

    public List<BannerBean> getBanner3() {
        return banner3;
    }

    public List<BannerBean> getBanner4() {
        return banner4;
    }

    public String getFronturl() {
        return fronturl;
    }

    public Campaign getActivity() {
        return activity;
    }
}
