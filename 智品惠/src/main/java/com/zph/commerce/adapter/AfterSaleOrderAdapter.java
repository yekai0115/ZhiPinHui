package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.bean.AfterSaleGoodsInfo;
import com.zph.commerce.bean.OrderGoodsInfo;
import com.zph.commerce.bean.OrderInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.utils.GsonUtil;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class AfterSaleOrderAdapter extends BaseAdapter {

    private Context context;
    private List<AfterSaleGoodsInfo> list;
    private HolderView holderView;
    private LayoutInflater mInflater;



    public AfterSaleOrderAdapter(Context context, List<AfterSaleGoodsInfo> list) {
        super();
        this.context = context;
        this.list = list;
        this.mInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<AfterSaleGoodsInfo> List) {
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
        AfterSaleGoodsInfo bean = list.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_aftersale_order_list_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        holderView.tv_after_sale_state.setText(bean.getRemark());
        String goods_logo = bean.getGoods_logo();
        String[] arr = goods_logo.split(",");
        goods_logo = arr == null || arr.length == 0 ? "" : arr[0];
        Glide.with(context).load(MyConstant.ALI_PUBLIC_URL + goods_logo).fitCenter()
                //  .override(width,DimenUtils.dip2px(context,130))
                .placeholder(R.drawable.default_user).error(R.drawable.default_user).into(holderView.img_goods_pic);
        holderView.tv_goods_name.setText(bean.getGname());
        holderView.tv_guige.setText("规格:"+bean.getAttr_value());
        return convertView;
    }

    static class HolderView {
        //
        @ViewInject(R.id.tv_after_sale_state)
        private TextView tv_after_sale_state;


        @ViewInject(R.id.img_goods_pic)
        private ImageView img_goods_pic;

        @ViewInject(R.id.tv_goods_name)
        private TextView tv_goods_name;

        @ViewInject(R.id.tv_guige)
        private TextView tv_guige;

    }

}


