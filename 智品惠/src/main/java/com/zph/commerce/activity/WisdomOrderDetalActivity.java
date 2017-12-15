package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.Logistics;
import com.zph.commerce.bean.LogisticsBase;
import com.zph.commerce.bean.OrderGoodsInfo;
import com.zph.commerce.bean.WisdomOrderDetals;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.DateUtil;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableRefreshScrollView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 描述 ：智品订单详情页面
 * 
 */
public class WisdomOrderDetalActivity extends BaseActivity implements PullLayout.OnRefreshListener{



	/** 实付款*/
	@ViewInject(R.id.tv_shifukuan)
	private TextView tv_shifukuan;
	/** 查看物流、立即收货*/
	@ViewInject(R.id.btn_operate)
	private TextView btn_operate;
	/**订单状态*/
	@ViewInject(R.id.tv_order_state)
	private TextView tv_order_state;


	/**收件人姓名*/
	@ViewInject(R.id.tv_sh_name)
	private TextView tv_sh_name;
	/**收件人电话*/
	@ViewInject(R.id.tv_phone)
	private TextView tv_phone;
	/**收件人地址*/
	@ViewInject(R.id.tv_address)
	private TextView tv_address;


	/**最新物流状态*/
	@ViewInject(R.id.tv_wl_state)
	private TextView tv_wl_state;
	/**最新物流状态*/
	@ViewInject(R.id.tv_wl_content)
	private TextView tv_wl_content;
	/**最新物流状态*/
	@ViewInject(R.id.tv_wl_time)
	private TextView tv_wl_time;


	/** 商品图片 */
	@ViewInject(R.id.iv_goods_pic)
	private ImageView iv_goods_pic;

	/**商品标题*/
	@ViewInject(R.id.tv_goodsTitle)
	private TextView tv_goodsTitle;
	/**商品规格*/
	@ViewInject(R.id.tv_guige)
	private TextView tv_guige;
	/**商品单价*/
	@ViewInject(R.id.tv_goods_danjia)
	private TextView tv_goods_danjia;
	/**商品积分*/
	@ViewInject(R.id.tv_goods_point)
	private TextView tv_goods_point;
	/**商品数量*/
	@ViewInject(R.id.tv_num)
	private TextView tv_num;


	/**商品运费*/
	@ViewInject(R.id.tv_yunfei)
	private TextView tv_yunfei;


	/**实付金额*/
	@ViewInject(R.id.tv_shifu)
	private TextView tv_shifu;
	/**订单号*/
	@ViewInject(R.id.tv_order_id)
	private TextView tv_order_id;
	/**交易号*/
	@ViewInject(R.id.tv_trade_id)
	private TextView tv_trade_id;
	/**下单时间*/
	@ViewInject(R.id.tv_buy_time)
	private TextView tv_buy_time;
	/**支付时间*/
	@ViewInject(R.id.tv_pay_time)
	private TextView tv_pay_time;
	/**发货时间*/
	@ViewInject(R.id.tv_fahuo_time)
	private TextView tv_fahuo_time;

	@ViewInject(R.id.refresh_view)
	private PullLayout refresh_view;
	@ViewInject(R.id.mScrollView)
	private PullableRefreshScrollView mScrollView;


	@ViewInject(R.id.stateLayout)
	private StateLayout stateLayout;


	@ViewInject(R.id.ll_wl)
	private LinearLayout ll_wl;

	/** 上下文 **/
	private Context mContext;

	private Intent intent;
	/**订单号*/
	private String order_sn;
	/**
	 * 订单状态
	 */
	private int status;
	/**
	 * 物流单号
	 */
	private String delivery_sn;
	private String token;
	private OrderGoodsInfo goodsInfo;
	private String goods_logo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wisdom_order_detal);
		mContext = this;
		x.view().inject(this);
		EventBus.getDefault().register(this);
		initDialog();
		intent = getIntent();
		status= intent.getIntExtra("status",0);
		order_sn= intent.getStringExtra("order_sn");
		delivery_sn= intent.getStringExtra("delivery_sn");
		goods_logo= intent.getStringExtra("goods_logo");
		token = (String) SPUtils.get(mContext, "token", "");
		token = EncodeUtils.base64Decode2String(token);
		refresh_view.setOnRefreshListener(this);
		initViews();
        getOrderDetail(1);
		mScrollView.smoothScrollTo(0, 0);//避免
		tv_order_id.setText(order_sn);
	}


	@Override
	protected void initEvents() {

	}


	@Override
	public void onRefresh(PullLayout pullToRefreshLayout) {
		getOrderDetail(2);
	}

	@Override
	public void onLoadMore(PullLayout pullToRefreshLayout) {

	}

	@Override
	protected void initViews() {
		TopNvgBar5 topNvgBar = (TopNvgBar5) findViewById(R.id.top_nvg_bar);
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
                getOrderDetail(1);
			}
		});
		stateLayout.setErrorAction(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			 getOrderDetail(1);
			}
		});
	}

	/**
	 * 查询物流
	 */
	private void getOrderWl() {

		dialog.show();
		APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
		Call<Object> call =userBiz.getExpressInfomation(order_sn);
		call.enqueue(new Callback<Object>() {
//LogisticsBase
			@Override
			public void onResponse(Call<Object> arg0,
								   Response<Object> response) {
				dialog.dismiss();
				stateLayout.showContentView();
				Object baseResponse=response.body();
				if(null==baseResponse|| StringUtils.isBlank(baseResponse.toString())){
					tv_wl_state.setText("处理中");
					tv_wl_content.setText("订单正在处理中");
					tv_wl_time.setText(DateUtil.getStandardTime(System.currentTimeMillis()));
				}else{
					try{
						String json=GsonUtil.GsonString(baseResponse);
						LogisticsBase logisticsBase=GsonUtil.GsonToBean(json,LogisticsBase.class);
						Logistics logistics = logisticsBase.getList().get(0);
						switch (logisticsBase.getDeliverystatus()){
							case  1 :
								tv_wl_state.setText("在途中");
								break;
							case  2:
								tv_wl_state.setText("派件中");
								break;
							case  3 :
								tv_wl_state.setText("已签收");
								break;
							case  4 :
								tv_wl_state.setText("派送失败");
								break;

						}
						tv_wl_content.setText(logistics.getStatus());
						tv_wl_time.setText(logistics.getTime());
					}catch (Exception e){
						e.printStackTrace();
					}

				}
			}

			@Override
			public void onFailure(Call<Object> arg0, Throwable arg1) {
				dialog.dismiss();
				ToastUtil.showToast(mContext, "请检查你的网络设置");
			}
		});

	}
    /**
     * 查询订单详细
     */
    private void getOrderDetail(final int state) {
        dialog.show();
		if(state==1){
			stateLayout.showProgressView();
		}
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<WisdomOrderDetals>>> call =userBiz.myOrdersDetail(token,order_sn);
        call.enqueue(new HttpCallBack<BaseResponse<List<WisdomOrderDetals>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<WisdomOrderDetals>>> arg0,
                                   Response<BaseResponse<List<WisdomOrderDetals>>> response) {
                dialog.dismiss();
				if(state==2){
					refresh_view.refreshFinish(PullLayout.SUCCEED);
				}
				super.onResponse(arg0,response);
                BaseResponse<List<WisdomOrderDetals>> baseResponse=response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
						List<WisdomOrderDetals> wisdomOrderDetalsList=baseResponse.getData();
						if(null==wisdomOrderDetalsList||wisdomOrderDetalsList.isEmpty()){
							stateLayout.showEmptyView("暂无数据");
							return;
						}


						WisdomOrderDetals orderDetals=baseResponse.getData().get(0);

                        tv_sh_name.setText(orderDetals.getAddr_name());
                        tv_phone.setText(orderDetals.getAddr_mobile());
                        tv_address.setText(orderDetals.getProvince()+orderDetals.getCity()+orderDetals.getCounty() +orderDetals.getAddr_detail());
						goodsInfo = GsonUtil.GsonToBean(orderDetals.getGoods_info(),OrderGoodsInfo.class);
						Glide.with(mContext).load(MyConstant.ALI_PUBLIC_URL + goods_logo).fitCenter()
                                //  .override(width,DimenUtils.dip2px(context,130))
                                .placeholder(R.drawable.pic_nomal_loading_style).error(R.drawable.pic_nomal_loading_style).into(iv_goods_pic);
                        tv_goodsTitle.setText(goodsInfo.getName());
                        tv_guige.setText(goodsInfo.getValue());
                        tv_goods_danjia.setText(goodsInfo.getPrice());
                        tv_num.setText("x"+goodsInfo.getNumber());
                        tv_yunfei.setText(orderDetals.getPostage());
                        tv_shifu.setText(orderDetals.getTotalprice());
						tv_shifukuan.setText(orderDetals.getTotalprice());
						tv_buy_time.setText(orderDetals.getBuildtime());
						tv_order_id.setText(orderDetals.getOrder_sn());
						tv_trade_id.setText(orderDetals.getPay_return_id());
						tv_pay_time.setText(orderDetals.getPay_time());
						tv_fahuo_time.setText(orderDetals.getDeli_time());
						status=orderDetals.getDeli_status();
						if (status==1) {//未发货
							tv_order_state.setText("待发货");
						} else if (status==2) {//已发货
							tv_order_state.setText("待收货");
						}else{
							tv_order_state.setText("已完成");
						}
						if (StringUtils.isBlank(delivery_sn)||delivery_sn.equals("0")){
							stateLayout.showContentView();
							tv_wl_state.setText("处理中");
							tv_wl_content.setText("订单正在处理中");
							tv_wl_time.setText(DateUtil.getStandardTime(System.currentTimeMillis()));
							return;
						}
						getOrderWl();
                    }else{
						stateLayout.showEmptyView(msg);
					}
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<List<WisdomOrderDetals>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "请检查你的网络设置");
                stateLayout.showErrorView("请检查你的网络设置");
				if(state==2){
					refresh_view.refreshFinish(PullLayout.FAIL);
				}
            }
        });

    }

	@Event({R.id.ll_wl,R.id.btn_operate})
	private void click(View view) {
		switch (view.getId()){
			case R.id.ll_wl:
				queryDelivery();
				break;
			case R.id.btn_operate:
				queryDelivery();
				break;
		}
	}

	private void queryDelivery(){
		if (StringUtils.isBlank(delivery_sn)||delivery_sn.equals("0")){
			ToastUtil.showToast(mContext, "暂无物流信息");
			return;
		}
		Intent intent = new Intent();
		intent.putExtra("delivery_sn", delivery_sn);//物流单号
		intent.putExtra("goods_logo", goods_logo);
		intent.putExtra("order_sn", order_sn);
		intent.setClass(WisdomOrderDetalActivity.this,OrderWuliuActivity.class);
		startActivity(intent);
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
		EventBus.getDefault().unregister(this);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEventMain(LoginMsgEvent messageEvent) {
		String login = (String) SPUtils.get(mContext, "login", "");
		if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
			MyConstant.HASLOGIN = true;
			token = (String) SPUtils.get(mContext, "token", "");
			token = EncodeUtils.base64Decode2String(token);
			getOrderDetail(1);
		} else {// 未登录
			finish();
			MyConstant.HASLOGIN = false;
		}
	}
}
