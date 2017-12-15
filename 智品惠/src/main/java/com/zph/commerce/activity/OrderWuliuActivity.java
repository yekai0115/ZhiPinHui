package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zph.commerce.R;
import com.zph.commerce.adapter.OrderWuliuAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.Logistics;
import com.zph.commerce.bean.LogisticsBase;
import com.zph.commerce.bean.LogisticsCompany;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.FileUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 描述 ：订单物流页面
 * 
 */
public class OrderWuliuActivity extends BaseActivity {



	/** 物流公司 */
	@ViewInject(R.id.tv_company)
	private TextView tv_wl_comp;
	/** 物流电话*/
	@ViewInject(R.id.tv_phone)
	private TextView tv_contanct_wl;
	/**物流单号 */
	@ViewInject(R.id.tv_wl_num)
	private TextView tv_wl_dh;

	/** 商品图片 */
	@ViewInject(R.id.img_goods)
	private ImageView img_goods;
	@ViewInject(R.id.scroll_detal)
	private ScrollView scroll_detal;


	@ViewInject(R.id.stateLayout)
	private StateLayout stateLayout;


	/** 列表 */
	@ViewInject(R.id.lv_wuliu)
	private ListView listview;

	/** 上下文 **/
	private Context mContext;

	private Intent intent;
	private List<Logistics> wlList = new ArrayList<Logistics>();
	private OrderWuliuAdapter adapter;
	/**物流单号*/
	private String delivery_sn;
	/**物流公司电话*/
	private String fcompanyTel;
	private String goods_logo;
	/**订单号*/
	private String order_sn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_wuliu);
		mContext = this;
		x.view().inject(this);
		initDialog();
		intent = getIntent();
		delivery_sn = intent.getStringExtra("delivery_sn");
		goods_logo = intent.getStringExtra("goods_logo");
		order_sn = intent.getStringExtra("order_sn");
		initViews();
		adapter = new OrderWuliuAdapter(mContext, wlList);
		listview.setAdapter(adapter);
		getOrderWl(1);
		scroll_detal.smoothScrollTo(0, 0);//避免
		Picasso.with(mContext)
	    .load(MyConstant.ALI_PUBLIC_URL+goods_logo)
	    .resize(DimenUtils.dip2px(mContext, 80), DimenUtils.dip2px(mContext, 80))
	    .config(Bitmap.Config.RGB_565)
	//加载过程中的图片显示
	    .placeholder(R.drawable.pic_nomal_loading_style)
	//加载失败中的图片显示
	//如果重试3次（下载源代码可以根据需要修改）还是无法成功加载图片，则用错误占位符图片显示。
	    .error(R.drawable.pic_nomal_loading_style)
	    .into(img_goods);
		

	}


	@Override
	protected void initEvents() {

	}

	@Override
	protected void initViews() {
		tv_wl_dh.setText(delivery_sn);
		TopNvgBar5 topNvgBar = (TopNvgBar5) findViewById(R.id.llytTitle);
		topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
			@Override
			public void onLeftClick() {
					finish();
			}

			@Override
			public void onRightClick() {

			}
		});
		stateLayout.setEmptyAction(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getOrderWl(2);
			}
		});
		stateLayout.setErrorAction(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getOrderWl(2);
			}
		});
	}

	/**
	 * 查询物流
	 * @param state
	 */
	private void getOrderWl(final int state) {
		dialog.show();
		APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
		Call<Object> call =userBiz.getExpressInfomation(order_sn);
		call.enqueue(new Callback<Object>() {
			
			@Override
			public void onResponse(Call<Object> arg0,
					Response<Object> response) {
				dialog.dismiss();
				Object baseResponse=response.body();
				if(null==baseResponse|| StringUtils.isBlank(baseResponse.toString())){
					ToastUtil.showToast(mContext, "暂无物流信息");
					stateLayout.showEmptyView("暂无物流信息");
				}else{
					stateLayout.showContentView();
					try{
						String json=GsonUtil.GsonString(baseResponse);
						LogisticsBase logisticsBase=GsonUtil.GsonToBean(json,LogisticsBase.class);
						updateState(logisticsBase);
						if (state == 2) {
							if (wlList != null) {
								wlList.clear();
							}
						}
						wlList=logisticsBase.getList();
						adapter.updatelistview(wlList);
					}catch (Exception e){
						e.printStackTrace();
					}

				}
			}
			
			@Override
			public void onFailure(Call<Object> arg0, Throwable arg1) {
				dialog.dismiss();
				ToastUtil.showToast(mContext, "请检查你的网络设置");
				stateLayout.showErrorView("请检查你的网络设置");
			}
		});

	}

	
	protected void updateState(LogisticsBase logisticsBase) {
		try{
			String  type=logisticsBase.getType();
			String json = FileUtils.readAssets(mContext, "wl.json");
			List<LogisticsCompany> logisticsCompanyList=GsonUtil.jsonToList(json,LogisticsCompany.class);
			for(LogisticsCompany logisticsCompany:logisticsCompanyList){

				String compType=logisticsCompany.getType();
				if(type.equalsIgnoreCase(compType)){
					fcompanyTel=logisticsCompany.getTel();
					String name=logisticsCompany.getName();
					tv_wl_comp.setText(name);
					tv_contanct_wl.setText(fcompanyTel);
					break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}



	}



	
	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();	
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}	
}
