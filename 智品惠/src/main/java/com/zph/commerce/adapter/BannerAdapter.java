package com.zph.commerce.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.activity.GoodsDetalActivity;
import com.zph.commerce.activity.MyWebViewActivity;
import com.zph.commerce.bean.BannerBean;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;


/**
 * 首页banner适配器
 */
public class BannerAdapter extends PagerAdapter {
    private List<BannerBean> list;
    private Context context;
    private HolderView holderView;
    private LayoutInflater mInflater;
    private LinkedList<View> mViewCache = null;
    private int width;

    public BannerAdapter(Context context, List<BannerBean> subjectsInfos) {
        this.context = context;
        this.list = subjectsInfos;
        this.mInflater = LayoutInflater.from(context);
        this.mViewCache = new LinkedList<>();
        width = DimenUtils.getWidth(context);
    }

    public void update(List<BannerBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        View convertView = null;
        if (mViewCache.size() == 0) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.banner_item_view, null, false);
            holderView.iv_banner = (ImageView) convertView.findViewById(R.id.iv_banner);
            convertView.setTag(holderView);
        } else {
            convertView = mViewCache.removeFirst();
            holderView = (HolderView) convertView.getTag();
        }
        final BannerBean bean = list.get(position);
        String banner = (MyConstant.BANNER_PUBLIC_URL + bean.getLogo() + MyConstant.PIC_DPI2).trim();
        Glide.with(context).load(banner)
                .fitCenter()
                .override(width, DimenUtils.dip2px(context, 540))
                .placeholder(R.drawable.pic_nomal_loading_style)
                .error(R.drawable.pic_nomal_loading_style)
                .into(holderView.iv_banner);

        holderView.iv_banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int fast_way = bean.getFast_way();
                String site_url = bean.getSite_url();
                String name = bean.getName();
                String good_id = bean.getJump_id();
                if (fast_way == 1) {
                    return;
                } else if (fast_way == 2) {
                    if (StringUtils.isBlank(good_id)) {
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("productId", good_id);
                    intent.setClass(context, GoodsDetalActivity.class);
                    context.startActivity(intent);
                } else if (fast_way == 3) {
                    Intent intent = new Intent();
                    intent.putExtra("site_url", site_url);
                    intent.putExtra("name", name);
                    intent.setClass(context, MyWebViewActivity.class);
                    context.startActivity(intent);
                } else if (fast_way == 4) {

                } else if (fast_way == 5) {

                }
            }
        });
        // container.addView(convertView);


        container.addView(convertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return convertView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View contentView = (View) object;
        container.removeView(contentView);
        this.mViewCache.add(contentView);

    }

    static class HolderView {

        private ImageView iv_banner;
    }
}