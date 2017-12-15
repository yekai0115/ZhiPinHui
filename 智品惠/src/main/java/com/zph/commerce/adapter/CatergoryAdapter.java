package com.zph.commerce.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.activity.CategoryGoodsListActivity;
import com.zph.commerce.bean.Catergory;
import com.zph.commerce.bean.ParentCatergory;
import com.zph.commerce.view.flowlayout.FlowLayout;
import com.zph.commerce.view.flowlayout.TagAdapter;
import com.zph.commerce.view.flowlayout.TagFlowLayout;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CatergoryAdapter extends BaseAdapter {

	private HolderView holderView;
	private Context context;
	private List<ParentCatergory> arrayList;
	private List<Catergory> list = new ArrayList<Catergory>();
	private LayoutInflater mInflater;  
	
	public CatergoryAdapter(Context context,
                            List<ParentCatergory> arrayList) {
		this.context = context;
		this.arrayList = arrayList;
		 this.mInflater=LayoutInflater.from(context);  
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView

	 */
	public void updateListView(List<ParentCatergory> arrayList) {

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
	public View getView(final int posions, View convertView, ViewGroup parent) {
		final ParentCatergory parentCatergory=arrayList.get(posions);
		if (convertView == null) {
			holderView = new HolderView();
			convertView = mInflater.inflate(R.layout.catergory_list_item, null);
			 x.view().inject(holderView,convertView);
			convertView.setTag(holderView);
		} else {
			holderView = (HolderView) convertView.getTag();
		}
		holderView.tv_firstCategory.setText(arrayList.get(posions).getTitle());

		final List<Catergory> list = parentCatergory.getCatelist();

		holderView.categoty_flowlayout.setAdapter(new TagAdapter(list) {
			@Override
			public View getView(FlowLayout parent, int position, Object o) {
				TextView tv = (TextView) mInflater.inflate(R.layout.category_tag_view, holderView.categoty_flowlayout, false);
				Catergory historyTable=list.get(position);
				tv.setText(historyTable.getName());
				return tv;
			}
		});

		holderView.categoty_flowlayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
			@Override
			public boolean onTagClick(View view, int position, FlowLayout parent) {

				Catergory historyTable=list.get(position);
				Intent intent = new Intent(context,CategoryGoodsListActivity.class);
				Bundle bundle = new Bundle();
				intent.putExtra("jump_id",historyTable.getCid());
				intent.putExtra("jump_name",historyTable.getName());
				intent.putExtra("type", 2);
				intent.putExtra("name", arrayList.get(posions).getTitle());
				bundle.putSerializable("CatergoryList",(Serializable) list);
				intent.putExtra("catergory",bundle);
				context.startActivity(intent);
//				ToastUtil.showToast(context,historyTable.getName());
				return false;
			}
		});
		return convertView;
	}


	static class HolderView {
		/** 一级分类标题 */
		@ViewInject(R.id.tv_firstCategory)
		private TextView tv_firstCategory;
		/** 一级分类图标 */
		@ViewInject(R.id.iv_adapter_list_pic)
		private ImageView iv_adapter_list_pic;
		@ViewInject(R.id.linearLayout)
		private LinearLayout linearLayout;
		@ViewInject(R.id.categoty_flowlayout)
		private TagFlowLayout categoty_flowlayout;
	}
}
