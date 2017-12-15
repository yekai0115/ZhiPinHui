package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.StringUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * 商品列表适配器
 *
 * @author Administrator
 */
public class ProductsListAdapter extends BaseAdapter {

    private HolderView holderView;

    private Context context;
    private LayoutInflater mInflater;
    private List<GoodsBean> list;

    // private Map<Integer, Boolean> isFrist;
    // private Animation animation;

    public ProductsListAdapter(Context context, List<GoodsBean> list) {
        this.list = list;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        // isFrist = new HashMap<Integer, Boolean>();
        // animation = AnimationUtils.loadAnimation(context,
        // R.anim.woniu_list_item);
    }

    public void updateListview(List<GoodsBean> list) {
        this.list = list;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        GoodsBean goods = (GoodsBean) getItem(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_goods_list_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        holderView.tv_goods_name.setText(goods.getGname());

        int good_type = goods.getGoods_type();
        if (good_type == 1) {
            holderView.tv_price.setText(goods.getPrice_cost());
            holderView.ll_point.setVisibility(View.VISIBLE);
            holderView.tv_member_price.setText(goods.getPrice());
            String point = goods.getPoint();
            if (StringUtils.isBlank(point)) {
                point = "0";
            }
            holderView.tv_point.setText(point);
        } else {
            holderView.ll_point.setVisibility(View.GONE);
            holderView.tv_price.setText(goods.getPrice());
        }
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
        @ViewInject(R.id.ll_point)
        private LinearLayout ll_point;


    }

}
