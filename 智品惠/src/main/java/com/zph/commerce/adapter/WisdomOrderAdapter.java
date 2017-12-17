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
import com.zph.commerce.bean.OrderGoodsInfo;
import com.zph.commerce.bean.OrderInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.interfaces.ListItemClickHelp;
import com.zph.commerce.utils.DateUtil;
import com.zph.commerce.utils.GsonUtil;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class WisdomOrderAdapter extends BaseAdapter {

    private Context context;
    private List<OrderInfo> list;
    private HolderView holderView;
    private LayoutInflater mInflater;
    private ListItemClickHelp callback;


    public WisdomOrderAdapter(Context context, List<OrderInfo> list, ListItemClickHelp callback) {
        super();
        this.context = context;
        this.list = list;
        this.callback = callback;
        this.mInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<OrderInfo> List) {
        this.list = List;
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {

        return list.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        OrderInfo bean = list.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_wisdom_order_list_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        holderView.tv_trade_id.setText(bean.getOrder_sn());
        int status = bean.getStatus();
        String remark = bean.getRemark();
        if (status == 1) {//提货订单：待发货：
            holderView.ll_state.setVisibility(View.GONE);
            holderView.tv_order_state.setText(remark);
        } else if (status == 2) {//提货订单待收货
            holderView.tv_order_state.setText(remark);
            holderView.ll_state.setVisibility(View.VISIBLE);
            holderView.rl_detal.setVisibility(View.VISIBLE);
            holderView.ll_state.setVisibility(View.VISIBLE);
            holderView.tv_shouhuo.setVisibility(View.VISIBLE);
        } else if (status == 3) {//提货订单已完成，未评价
            holderView.tv_order_state.setText(remark);
            holderView.ll_state.setVisibility(View.VISIBLE);
            holderView.rl_detal.setVisibility(View.VISIBLE);
            holderView.tv_shouhuo.setVisibility(View.VISIBLE);
            holderView.tv_shouhuo.setText("立即评价");
        }else if (status == 4) {//提货订单已完成，已评价
            holderView.tv_order_state.setText(remark);
            holderView.ll_state.setVisibility(View.VISIBLE);
            holderView.rl_detal.setVisibility(View.VISIBLE);
            holderView.tv_shouhuo.setVisibility(View.GONE);
        }  else {//购买订单：记录
            holderView.ll_state.setVisibility(View.GONE);
            holderView.rl_detal.setVisibility(View.GONE);
            String time = DateUtil.getYearMonthDayStr(bean.getAdd_time());
            holderView.tv_order_state.setText(time);
        }
        String goods = bean.getGoods_info();
        OrderGoodsInfo goodsInfo = GsonUtil.GsonToBean(goods, OrderGoodsInfo.class);
        String goods_logo = goodsInfo.getPic();
        String[] arr = goods_logo.split(",");
        goods_logo = arr == null || arr.length == 0 ? "" : arr[0];
        Glide.with(context).load(MyConstant.ALI_PUBLIC_URL + goods_logo).fitCenter()
                //  .override(width,DimenUtils.dip2px(context,130))
                .placeholder(R.drawable.default_user).error(R.drawable.default_user).into(holderView.img_goods_pic);
        holderView.tv_goods_name.setText(goodsInfo.getName());
        holderView.tv_guige.setText(goodsInfo.getValue());
        holderView.tv_num.setText("X" + goodsInfo.getNumber());
        holderView.tv_price.setText(goodsInfo.getPrice());
        String postage = bean.getPostage();
        String totalprice = bean.getTotalprice();
        holderView.tv_total_yunfei.setText(postage);
        holderView.tv_total_num.setText("" + goodsInfo.getNumber());
        holderView.tv_total_money.setText(totalprice);


        final View view = convertView;
        final int p = position;
        final int one = holderView.tv_wuliu.getId();
        final int two = holderView.tv_shouhuo.getId();
        final int three = holderView.ll_detal.getId();

        //查看物流
        holderView.tv_wuliu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.onClick(view, parent, p, one);
            }
        });

        //立即收货
        holderView.tv_shouhuo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.onClick(view, parent, p, two);
            }
        });

        //查看详情
        holderView.ll_detal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callback.onClick(view, parent, p, three);
            }
        });
        return convertView;
    }

    static class HolderView {
        //
        @ViewInject(R.id.tv_trade_id)
        private TextView tv_trade_id;
        //
        @ViewInject(R.id.tv_order_state)
        private TextView tv_order_state;


        @ViewInject(R.id.img_goods_pic)
        private ImageView img_goods_pic;

        @ViewInject(R.id.tv_goods_name)
        private TextView tv_goods_name;

        @ViewInject(R.id.tv_guige)
        private TextView tv_guige;

        @ViewInject(R.id.tv_price)
        private TextView tv_price;


        @ViewInject(R.id.tv_num)
        private TextView tv_num;


        @ViewInject(R.id.ll_state)
        private LinearLayout ll_state;

        @ViewInject(R.id.tv_wuliu)
        private TextView tv_wuliu;
        @ViewInject(R.id.tv_shouhuo)
        private TextView tv_shouhuo;

        @ViewInject(R.id.ll_detal)
        private LinearLayout ll_detal;

        /**
         * 运费
         */
        @ViewInject(R.id.tv_total_yunfei)
        private TextView tv_total_yunfei;
        /**
         * 合计
         */
        @ViewInject(R.id.tv_total_money)
        private TextView tv_total_money;
        /**
         * 合计
         */
        @ViewInject(R.id.tv_total_num)
        private TextView tv_total_num;
        @ViewInject(R.id.rl_detal)
        private RelativeLayout rl_detal;

    }

}


