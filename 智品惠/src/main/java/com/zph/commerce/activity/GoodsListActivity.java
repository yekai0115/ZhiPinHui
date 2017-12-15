package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.ProductsListAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * 描述 ：商品列表
 *
 */
public class GoodsListActivity extends BaseActivity implements
		PullToRefreshLayout.OnRefreshListener{

	@ViewInject(R.id.refresh_view)
	private PullToRefreshLayout refresh_view;

	/** 商品列表 */
	@ViewInject(R.id.lv_goods)
	private PullableListView lv_goods;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;


	/** 上下文 **/
	private Context mContext;
	/** 收货地址数据 **/
	private List<GoodsBean> goodsBeanList = new ArrayList<GoodsBean>();
	private ProductsListAdapter adapter;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goods_list);
		mContext = this;
		x.view().inject(this);
		setWidget();
		initDialog();
		adapter = new ProductsListAdapter(mContext, goodsBeanList);
		lv_goods.setAdapter(adapter);
		lv_goods.canPullUp=false;
		refresh_view.setOnRefreshListener(this);
		searchGood(1);
	}



	private void setWidget() {
		title=getIntent().getStringExtra("title");
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
                //  stateLayout.showProgressView();
				searchGood(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  stateLayout.showProgressView();
				searchGood(1);
            }
        });

		lv_goods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String gid=goodsBeanList.get(i).getGid();
				Intent intent = new Intent();
				intent.putExtra("productId", gid);
				intent.setClass(mContext, GoodsDetalActivity.class);
				startActivity(intent);


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
		if(null!=goodsBeanList){
			goodsBeanList.clear();
			goodsBeanList=null;
		}

	}



	/**
	 * 获取搜索列表
	 * @param state
	 */
	private void searchGood(final int state) {
		dialog.show();
		APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
		Call<BaseResponse<List<GoodsBean>>> call = userBiz.searchGood(title);
		call.enqueue(new HttpCallBack<BaseResponse<List<GoodsBean>>>() {

			@Override
			public void onResponse(Call<BaseResponse<List<GoodsBean>>> arg0,
                                   Response<BaseResponse<List<GoodsBean>>> response) {
				dialog.dismiss();
				if (state == 2) {
					if (goodsBeanList != null) {
						goodsBeanList.clear();
					}
					refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
				}
				super.onResponse(arg0,response);
				BaseResponse<List<GoodsBean>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
						goodsBeanList = baseResponse.getData();
                        if (null == goodsBeanList || goodsBeanList.isEmpty()) {
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        } else {
                            stateLayout.showContentView();
                        }
                        adapter.updateListview(goodsBeanList);
                    } else {
                        	ToastUtil.showToast(mContext, desc);
                    }
                }
			}

			@Override
			public void onFailure(Call<BaseResponse<List<GoodsBean>>> arg0, Throwable arg1) {
				dialog.dismiss();
                stateLayout.showErrorView();
                stateLayout.showErrorView("网络错误");
				if (state == 2) {
					// 刷新完成调用
					refresh_view.refreshFinish(PullToRefreshLayout.FAIL);
				}

				ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
			}
		});
	}



	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {

		searchGood(2);
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

	}




}
