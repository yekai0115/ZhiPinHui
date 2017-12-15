package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.bean.GoodsInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.interfaces.ListItemClickHelp;
import com.zph.commerce.utils.CompuUtils;
import com.zph.commerce.utils.StringUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


/**
 * 购物车确认订单适配器
 *
 * @author Administrator
 */
public class SubmitCarOrderAdapter extends BaseAdapter {
    private Context context;
    private List<GoodsInfo> list;
    private LayoutInflater mInflater;
    private ListItemClickHelp callback;
    private int goods_type;
    private int rank_id;
    private int since_order;
    private String shipping_str;

    public SubmitCarOrderAdapter(Context context, List<GoodsInfo> list, ListItemClickHelp callback, int goods_type, int rank_id, int since_order, String shipping_str) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        this.goods_type = goods_type;
        this.rank_id = rank_id;
        this.since_order = since_order;
        this.shipping_str = shipping_str;
        this.mInflater = LayoutInflater.from(context);
    }

    public SubmitCarOrderAdapter(Context context) {
        this.context = context;

    }

    public void updateListview(List<GoodsInfo> list, int rank_id, int since_order, String shipping_str) {
        this.list = list;
        this.rank_id = rank_id;
        this.since_order = since_order;
        this.shipping_str = shipping_str;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        HolderView holderView = null;
        final GoodsInfo goods = list.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.order_queren_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        if (list.size() != 0) {
            holderView.tv_goodsTitle.setText(goods.getGname());
            holderView.tv_guige.setText(goods.getAttr_value());
            holderView.tv_num.setText("X" + goods.getNumber() + "");
            holderView.tv_buy_num.setText(goods.getNumber() + "");
            if (goods_type == 1) {
                holderView.tv_goods_danjia.setText(goods.getPrice_cost());
                holderView.tv_member_price.setText(goods.getAttr_price());
                String point = goods.getAttr_point();
                if (StringUtils.isBlank(point)) {
                    point = "0";
                }
                holderView.tv_point.setText(point);
                holderView.tv_yunfei.setCompoundDrawables(null, null, null, null);
            } else {
                holderView.tv_goods_danjia.setText(goods.getAttr_price());
                holderView.ll_point.setVisibility(View.GONE);
                holderView.tv_goods_num.setVisibility(View.VISIBLE);
                holderView.tv_goods_num.setText("X" + goods.getNumber() + "");
            }
            if (rank_id == 1) {
                holderView.rl_kuaidi.setVisibility(View.VISIBLE);
                if (since_order == 1) {//自提
                    holderView.tv_yunfei.setText("自提");
                } else {//非自提
                    String postprice = goods.getPostprice();//邮费
                    int compare = CompuUtils.compareTo(postprice, "0");
                    if (compare > 0) {
                        if(StringUtils.isBlank(shipping_str)){
                            holderView.tv_yunfei.setText("快递  " + postprice);
                        }else{
                            holderView.tv_yunfei.setText(shipping_str+" " + postprice);
                        }

                    } else {
                        holderView.tv_yunfei.setText("快递  包邮");
                    }
                }

            } else {//创客以上
                holderView.rl_kuaidi.setVisibility(View.GONE);
            }
            String goods_logo = goods.getGoods_logo();
            String[] arr = goods_logo.split(",");
            Glide.with(context).load(MyConstant.ALI_PUBLIC_URL + arr[0])
                    //     .override(DimenUtils.px2dip(context, 100), DimenUtils.px2dip(context, 100))
                    // .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.bg_loading_style)
                    .error(R.drawable.bg_loading_style)
                    .into(holderView.iv_goods_pic);


            final View view = convertView;
            final int p = position;
            final int one = holderView.tv_num_jian.getId();
            final int two = holderView.tv_num_jia.getId();
            final int three = holderView.tv_yunfei.getId();
            //减
            holderView.tv_num_jian.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    callback.onClick(view, parent, p, one);
                }
            });

            //加
            holderView.tv_num_jia.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    callback.onClick(view, parent, p, two);
                }
            });

            holderView.tv_yunfei.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    callback.onClick(view, parent, p, three);
                }
            });


        }
        return convertView;
    }

    static class HolderView {
        @ViewInject(R.id.iv_goods_pic)
        private ImageView iv_goods_pic;
        /**
         * 商品
         */
        @ViewInject(R.id.tv_goodsTitle)
        private TextView tv_goodsTitle;

        /**
         * 原价
         */
        @ViewInject(R.id.tv_goods_danjia)
        private TextView tv_goods_danjia;
        /**
         * 规格
         */
        @ViewInject(R.id.tv_guige)
        private TextView tv_guige;

        /**
         * 运费
         */
        @ViewInject(R.id.tv_yunfei)
        private TextView tv_yunfei;
        /**
         * 会员价
         */
        @ViewInject(R.id.tv_member_price)
        private TextView tv_member_price;
        @ViewInject(R.id.tv_point)
        private TextView tv_point;
        /**
         * 数量
         */
        @ViewInject(R.id.tv_num)
        private TextView tv_num;

        /**
         * 数量
         */
        @ViewInject(R.id.tv_buy_num)
        private TextView tv_buy_num;

        @ViewInject(R.id.tv_num_jian)
        private TextView tv_num_jian;

        @ViewInject(R.id.tv_num_jia)
        private TextView tv_num_jia;

        @ViewInject(R.id.tv_goods_num)
        private TextView tv_goods_num;
        @ViewInject(R.id.ll_point)
        private LinearLayout ll_point;

        @ViewInject(R.id.rl_kuaidi)
        private RelativeLayout rl_kuaidi;
        @ViewInject(R.id.img_right)
        private ImageView img_right;


    }

}
