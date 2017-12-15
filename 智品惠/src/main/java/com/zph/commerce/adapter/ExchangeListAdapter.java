package com.zph.commerce.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zph.commerce.R;
import com.zph.commerce.bean.ExchangeBean;
import com.zph.commerce.view.HandyTextView;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

/**
 * 适配器
 * @author Administrator
 * 
 */
public class ExchangeListAdapter extends BaseAdapter {

	private HolderView holderView;


	private LayoutInflater mInflater;
	private List<ExchangeBean> list;


	public ExchangeListAdapter(Context context, List<ExchangeBean> list) {
		this.list = list;
		this.mInflater = LayoutInflater.from(context);
	}

	public void updateListview(List<ExchangeBean> list) {
		this.list = list;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ExchangeBean exchangeBean = (ExchangeBean) getItem(position);
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

		holderView.tv_bank.setText("兑换到"+exchangeBean.getBankname());
		holderView.tv_time.setText(exchangeBean.getAdd_time());
		holderView.tv_money.setText("-"+exchangeBean.getPoints());
		holderView.iar_status.setText(exchangeBean.getStatus());
		holderView.tv_state.setVisibility(View.INVISIBLE);
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
