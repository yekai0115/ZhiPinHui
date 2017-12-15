package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.bean.Shipping;
import com.zph.commerce.utils.CompuUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class FreightAdapter extends BaseAdapter {

    private HolderView holderView;
    private List<Shipping> arrayList;

    private LayoutInflater mInflater;

    public FreightAdapter(Context context, List<Shipping> arrayList) {
        this.arrayList = arrayList;
        this.mInflater = LayoutInflater.from(context);
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
    public View getView(final int posions, View convertView, ViewGroup parent) {
        final Shipping shipping = arrayList.get(posions);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_freight_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        holderView.tv_freight_name.setText(shipping.getShipping_str());
        holderView.cb_freight.setChecked(shipping.isCheck());
        String postprice = CompuUtils.multiply(shipping.getPrice(), shipping.getNum() + "").toString();
        holderView.tv_freight_price.setText(postprice);
        return convertView;
    }


    static class HolderView {

        @ViewInject(R.id.tv_freight_name)
        private TextView tv_freight_name;
        @ViewInject(R.id.tv_freight_price)
        private TextView tv_freight_price;
        @ViewInject(R.id.cb_freight)
        private CheckBox cb_freight;

    }
}
