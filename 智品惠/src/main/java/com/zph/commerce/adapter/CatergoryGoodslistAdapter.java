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
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.StringUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class CatergoryGoodslistAdapter extends BaseAdapter {

    private HolderView holderView;
    private Context context;
    private List<GoodsBean> arrayList;
    private LayoutInflater mInflater;

    public CatergoryGoodslistAdapter(Context context, List<GoodsBean> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.mInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<GoodsBean> arrayList) {

        this.arrayList = arrayList;
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GoodsBean goods = arrayList.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_goods_list_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        holderView.tv_goods_name.setText(goods.getGname());
        holderView.tv_price.setText(goods.getPrice_cost());
        holderView.tv_member_price.setText(goods.getPrice());
        String point = goods.getPoint();
        if (StringUtils.isBlank(point)) {
            point = "0";
        }
        holderView.tv_point.setText(point);


        holderView.tv_sail.setText("已销售" + goods.getGoods_sold() + "件");
        String goods_logo = goods.getGoods_logo();
        String[] arr = goods_logo.split(",");
        Glide.with(context).load(MyConstant.ALI_PUBLIC_URL + arr[0])
                //     .override(DimenUtils.px2dip(context, 100), DimenUtils.px2dip(context, 100))
                // .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.bg_loading_style)
                .error(R.drawable.bg_loading_style)
                .into(holderView.img_goods_pic);


        return convertView;
    }


    static class HolderView {
        /**
         * 价格
         */
        @ViewInject(R.id.tv_price)
        private TextView tv_price;
        @ViewInject(R.id.tv_point)
        private TextView tv_point;
        @ViewInject(R.id.tv_member_price)
        private TextView tv_member_price;

        /**
         * 商品名称
         */
        @ViewInject(R.id.tv_goods_name)
        private TextView tv_goods_name;
        /**
         * 销量
         */
        @ViewInject(R.id.tv_sail)
        private TextView tv_sail;
        /**
         * 商品图片
         */
        @ViewInject(R.id.img_goods_pic)
        private ImageView img_goods_pic;
    }
}
