package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.AfterSaleOrderAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.AfterSaleGoodsInfo;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.OrderInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pulltorefresh.PullLayout;
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
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 售后订单
 */

public class AfterSaleOrderActivity extends BaseActivity implements PullLayout.OnRefreshListener {
    private Context context;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.lv_order)
    private ListView lv_order;
    @ViewInject(R.id.refresh_view)
    private PullLayout ptrl;


    private AfterSaleOrderAdapter adapter;
    private List<AfterSaleGoodsInfo> orderInfoList = new ArrayList<AfterSaleGoodsInfo>();
    private String token;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_sale_order_list);
        MyApplication.addActivity(this);
        x.view().inject(this);
        context = this;
        EventBus.getDefault().register(this);
        token = (String) SPUtils.get(context, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        initViews();
        initDialog();
        getOrderList(1);

    }

    @Override
    protected void initViews() {
        ptrl.setOnRefreshListener(this);
        // ptrl.autoRefresh(); //去掉自动刷新
        adapter = new AfterSaleOrderAdapter(context, orderInfoList);
        lv_order.setAdapter(adapter);
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOrderList(2);
            }
        });

        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOrderList(2);
            }
        });
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
    }

    @Override
    protected void initEvents() {

    }


    // 进入采购详情返回后刷新
    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * 获取智品订单
     *
     * @param state
     */
    private void getOrderList(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<AfterSaleGoodsInfo>>> call = userBiz.exchangeGoodList(token);
        call.enqueue(new Callback<BaseResponse<List<AfterSaleGoodsInfo>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<AfterSaleGoodsInfo>>> arg0,
                                   Response<BaseResponse<List<AfterSaleGoodsInfo>>> response) {
                dialog.dismiss();
                if (state == 2) {
                    if (orderInfoList != null) {
                        orderInfoList.clear();
                    }
                    ptrl.refreshFinish(PullLayout.SUCCEED);
                }
                BaseResponse<List<AfterSaleGoodsInfo>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<AfterSaleGoodsInfo> data = baseResponse.getData();
                        if (null == data || data.isEmpty()) {
                            stateLayout.showEmptyView("暂无数据");
                            adapter.notifyDataSetChanged();
                        } else {
                            orderInfoList.addAll(data);
                            stateLayout.showContentView();
                            adapter.updateListView(orderInfoList);
                            setListviewHeight(lv_order);
                        }
                    } else {
                        ToastUtil.showToast(context, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<AfterSaleGoodsInfo>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView("连接服务器失败");
                if (state == 1) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    // 刷新完成调用
                    ptrl.refreshFinish(PullLayout.FAIL);
                }
                ToastUtil.showToast(context, "网络状态不佳,请检查您的网络设置");
            }
        });
    }


    /**
     * 下拉
     */
    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        getOrderList(2);

    }

    /**
     * 上拉
     */
    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 自动匹配listview的高度
     *
     * @param
     */
    private void setListviewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listitemView = listAdapter.getView(i, null, listView);
            listitemView.measure(0, 0);
            totalHeight += listitemView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + 60;
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(context, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(context, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            getOrderList(2);
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }
}
