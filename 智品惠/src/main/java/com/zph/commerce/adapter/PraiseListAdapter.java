package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zph.commerce.R;
import com.zph.commerce.bean.PraiseInfo;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.view.HandyTextView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * 适配器
 * @author Administrator
 * 
 */
public class PraiseListAdapter extends BaseAdapter {

	private HolderView holderView;

	private Context context;
	private LayoutInflater mInflater;
	private List<PraiseInfo> list;


	public PraiseListAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		this.context = context;
	}
	public PraiseListAdapter(Context context, List<PraiseInfo> list) {
		this.list = list;
		this.context = context;
		this.mInflater = LayoutInflater.from(context);

	}

	public void updateListview(List<PraiseInfo> list) {
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
		final PraiseInfo praiseInfo = (PraiseInfo) getItem(position);
		if (convertView == null) {
			holderView = new HolderView();
			convertView = mInflater.inflate(R.layout.adapter_exchange_list_item, null);
			x.view().inject(holderView, convertView);
			convertView.setTag(holderView);
			// 可以对item设置不同的动画
			// convertView.setAnimation(AnimationUtils.loadAnimation(context,
			// R.anim.push_right_in));
		} else {
			holderView = (HolderView) convertView.getTag();
		}

		holderView.tv_bank.setText(praiseInfo.getRemarks());
		holderView.tv_time.setText(praiseInfo.getAdd_time());
		holderView.tv_money.setText(praiseInfo.getPoint());
		holderView.iar_status.setVisibility(View.GONE);
		String pointStatus=praiseInfo.getPointStatus();
		if(StringUtils.isBlank(pointStatus)){
			holderView.tv_state.setVisibility(View.INVISIBLE);
		}else{
			holderView.tv_state.setText(praiseInfo.getPointStatus());
			holderView.tv_state.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	static class HolderView {
		@ViewInject(R.id.tv_bank)
		private HandyTextView tv_bank;
		@ViewInject(R.id.tv_time)
		private HandyTextView tv_time;
		@ViewInject(R.id.tv_money)
		private HandyTextView tv_money;

		@ViewInject(R.id.iar_status)
		private HandyTextView iar_status;

		@ViewInject(R.id.tv_state)
		private HandyTextView tv_state;



	}

}
