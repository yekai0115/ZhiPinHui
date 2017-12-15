package com.zph.commerce.view.popwindow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zph.commerce.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

public class TitlePopAdapter extends BaseAdapter{

	private int mPosition;
	private ArrayList<ActionItem> mActionItems;
	private TitlePopWindow mWindow;
	private Context context;
	private onItemClickListener mListener;
	private ViewHolder holder;
	private LayoutInflater mInflater;

	public TitlePopAdapter(TitlePopWindow window, Context context, ArrayList<ActionItem> mActionItems){
		this.context=context;
		this.mActionItems=mActionItems;
		this.mWindow=window;
		this.mInflater=LayoutInflater.from(context);
	}

	public int getCount() {
		return mActionItems.size();
	}

	public Object getItem(int position) {
		return mActionItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		@ViewInject(R.id.tv_text)
		private TextView text;
		@ViewInject(R.id.img_pop_item)
		private ImageView img_pop_item;
	}

	public View getView(final int arg0, final View arg1, ViewGroup arg2) {
		//获取设置好的listener
		mListener=mWindow.getListener();
		View view=arg1;
		holder=null;
		if(view==null){
			view=View.inflate(context, R.layout.title_pop_item, null);
			holder = new ViewHolder();
			x.view().inject(holder,view);
			view.setTag(holder);
		}else {
			holder = (ViewHolder) view.getTag();
		}
		holder.text.setText(mActionItems.get(arg0).getmTitle());
		Drawable mDrawable=mActionItems.get(arg0).getmDrawable();
		if(null!=mDrawable){
			holder.img_pop_item.setBackground(mActionItems.get(arg0).getmDrawable());
		}else{
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.text.getLayoutParams();
			layoutParams.setMargins(15,0,0,0);
			holder.text.setGravity(Gravity.CENTER|Gravity.LEFT);
			holder.text.setLayoutParams(layoutParams);
		}
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPosition=arg0;
				mWindow.close();
				mListener.click(mPosition,arg1);
			}
		});
		return view;
	}
	//定义接口和一个为实现的方法
	public interface onItemClickListener{
		public void click(int position, View view);
	}




}
