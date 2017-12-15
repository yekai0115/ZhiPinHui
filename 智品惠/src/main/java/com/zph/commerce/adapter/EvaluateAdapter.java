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
import com.zph.commerce.bean.CommentContent;
import com.zph.commerce.widget.GlideCircleTransform;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;


public class EvaluateAdapter extends BaseAdapter {

    private Context context;
    private List<CommentContent> list;
    private HolderView holderView;
    private LayoutInflater mInflater;


    public EvaluateAdapter(Context context, List<CommentContent> list) {
        super();
        this.context = context;
        this.list = list;
        this.mInflater = LayoutInflater.from(context);
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    public void updateListView(List<CommentContent> List) {
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
        CommentContent bean = list.get(position);
        if (convertView == null) {
            holderView = new HolderView();
            convertView = mInflater.inflate(R.layout.adapter_evaluate_list_item, null);
            x.view().inject(holderView, convertView);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }
        holderView.tv_name.setText(bean.getNick());
        int type = bean.getDerail();
        String url;
        if (type == 1) {
            url = bean.getHeadurl() + "/" + bean.getHead();
        } else {
            url = bean.getHeadurl() + bean.getHead();
        }
        Glide.with(context).load(url).override(25, 25).transform(new GlideCircleTransform(context))
                .placeholder(R.drawable.pic_nomal_loading_style)
                .error(R.drawable.img_default_head)
                .into(holderView.img_head);
        holderView.tv_dengji.setText(bean.getRank_name());
        holderView.tv_guige.setText(bean.getAttr_value());
        holderView.tv_evaluate.setText(bean.getComment());


        return convertView;
    }

    static class HolderView {
        //
        @ViewInject(R.id.tv_name)
        private TextView tv_name;


        @ViewInject(R.id.img_head)
        private ImageView img_head;

        @ViewInject(R.id.tv_dengji)
        private TextView tv_dengji;

        @ViewInject(R.id.tv_guige)
        private TextView tv_guige;

        @ViewInject(R.id.tv_evaluate)
        private TextView tv_evaluate;


    }

}


