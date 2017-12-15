package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.AddressChooseAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent12;
import com.zph.commerce.eventbus.MsgEvent13;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * 描述 ：收货人地址选择
 *
 */
public class AddressChooseActivity extends BaseActivity implements
		PullToRefreshLayout.OnRefreshListener{




	@ViewInject(R.id.des_refresh_view)
	private PullToRefreshLayout des_refresh_view;

	/** 收货列表 */
	@ViewInject(R.id.des_listview)
	private PullableListView des_listview;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;


	/** 上下文 **/
	private Context mContext;
	private LoadingDialog dialog;
	/** 收货地址数据 **/
	private List<Address> addressList = new ArrayList<Address>();
	private AddressChooseAdapter adapter;


	private String token;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.address_choose);
		mContext = this;
		x.view().inject(this);
		setWidget();
		// 注册事件
		EventBus.getDefault().register(this);
		token=(String) SPUtils.get(mContext, "token", "");
		token= EncodeUtils.base64Decode2String(token);
		initDialog();
		adapter = new AddressChooseAdapter(mContext, addressList);
		des_listview.setAdapter(adapter);
		des_listview.canPullUp=false;
		des_refresh_view.setOnRefreshListener(this);
		getAcceptAddressList(1);
	}

	protected void initDialog() {
		dialog = new LoadingDialog(mContext, R.style.dialog, "加载中...");
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
	}

	private void setWidget() {
		TopNvgBar5 topNvgBar = (TopNvgBar5) findViewById(R.id.top_nvg_bar);
		topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
			@Override
			public void onLeftClick() {

				finish();
			}

			@Override
			public void onRightClick() {
                Intent intent = new Intent(mContext, AddressMangeActivity.class);
				intent.putExtra("fromType", 2);
                startActivity(intent);
			}
		});
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  stateLayout.showProgressView();
                getAcceptAddressList(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  stateLayout.showProgressView();
                getAcceptAddressList(1);
            }
        });

		des_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Address address = (Address) adapter.getItem(i);
				Intent intent = new Intent();
				intent.putExtra("place", address);
				intent.putExtra("result", 1);
				setResult(1, intent);
				finish();
			}
		});


	}


	@Override
	protected void initViews() {

	}

	@Override
	protected void initEvents() {

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
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(null!=addressList){
			addressList.clear();
			addressList=null;
		}
		EventBus.getDefault().unregister(this);
	}










	/**
	 * 获取收货地址列表
	 * @param state
	 */
	private void getAcceptAddressList(final int state) {
		dialog.show();
		APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
		Call<BaseResponse<List<Address>>> call = userBiz.getAcceptAddressRepo(token);
		call.enqueue(new HttpCallBack<BaseResponse<List<Address>>>() {

			@Override
			public void onResponse(Call<BaseResponse<List<Address>>> arg0,
                                   Response<BaseResponse<List<Address>>> response) {
				dialog.dismiss();
				if (state == 2) {
					if (addressList != null) {
						addressList.clear();
					}
					des_refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
				}
				super.onResponse(arg0,response);
				BaseResponse<List<Address>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
						addressList = baseResponse.getData();
                        if (null == addressList || addressList.isEmpty()) {
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        } else {
                            stateLayout.showContentView();
                        }
                        adapter.updateListView(addressList);
                    } else {
                        	ToastUtil.showToast(mContext, desc);
                    }
                }
			}

			@Override
			public void onFailure(Call<BaseResponse<List<Address>>> arg0, Throwable arg1) {
				dialog.dismiss();
                stateLayout.showErrorView();
                stateLayout.showErrorView("网络错误");
				if (state == 2) {
					// 刷新完成调用
					des_refresh_view.refreshFinish(PullToRefreshLayout.FAIL);
				}

				ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
			}
		});
	}



	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {

		getAcceptAddressList(2);
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEventMain(LoginMsgEvent messageEvent) {
			String login = (String) SPUtils.get(mContext, "login", "");
			if (!StringUtils.isBlank(login)&& login.equals(MyConstant.SUC_RESULT)) {// 已登录
				MyConstant.HASLOGIN = true;
				token = (String) SPUtils.get(mContext, "token", "");
				token = EncodeUtils.base64Decode2String(token);
				getAcceptAddressList(1);
			} else {// 未登录
				finish();
				MyConstant.HASLOGIN = false;
			}
	}

	//地址新增编辑页面有操作
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEventMain(MsgEvent13 messageEvent) {
		getAcceptAddressList(2);//重新请求地址列表
	}

	//地址管理页面有操作
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEventMain(MsgEvent12 messageEvent) {
		getAcceptAddressList(2);//重新请求地址列表
	}


}
