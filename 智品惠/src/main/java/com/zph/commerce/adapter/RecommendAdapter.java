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
import com.zph.commerce.bean.RecomFriend;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.widget.GlideCircleTransform;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class RecommendAdapter extends BaseAdapter {

    private Context context;
    private List<RecomFriend> list;
    private HolderView holderView;
    private LayoutInflater mInflater;
    private String headurl;


    public RecommendAdapter(Context context, List<RecomFriend> list) {
        super();
        this.context = context;
        this.list = list;
        this.mInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<RecomFriend> List, String headurl) {
        this.list = List;
        this.headurl = headurl;
        notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
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
        RecomFriend bean = list.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_friend_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        holderView.tv_time.setText(bean.getTimes());
        holderView.tv_phone.setText(bean.getMobile());
        String head= bean.getHead();
        String url;
        if(head.length()>10){
            url= MyConstant.ALI_PUBLIC_URL +"/"+ bean.getHead();
        }else{
            url=headurl + bean.getHead();
        }
        Glide.with(context).load(url).override(30, 30)
                .placeholder(R.drawable.pic_nomal_loading_style)
                .error(R.drawable.img_default_head)
                .transform(new GlideCircleTransform(context))
                .into(holderView.img_head);


        return convertView;
    }

    static class HolderView {
        // 手机号
        @ViewInject(R.id.tv_phone)
        private TextView tv_phone;
        //
        @ViewInject(R.id.tv_time)
        private TextView tv_time;

        @ViewInject(R.id.img_head)
        private ImageView img_head;

    }

}


