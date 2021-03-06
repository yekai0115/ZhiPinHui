package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.zph.commerce.R;
import com.zph.commerce.adapter.ExchangeListAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.ExchangeBean;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar6;

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
 * Created by 24448 on 2017/10/20.
 */

public class ExchangeListActivity extends BaseActivity implements PullLayout.OnRefreshListener {

    private ExchangeListAdapter adapter;
    private List<ExchangeBean> messageList = new ArrayList<>();
    @ViewInject(R.id.list_exchanges)
    private PullableListView listExchange;
    @ViewInject(R.id.product_refresh_view)
    private PullLayout pullLayout;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;
    /**
     * 上下文
     **/
    private Context mContext;
    private String token;
    /**
     * 1货款提现记录
     * 2积分提现记录
     */
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_exchange_list);
        mContext = this;
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        type = getIntent().getIntExtra("state", 1);
        x.view().inject(this);
        // 注册事件
        EventBus.getDefault().register(this);
        initViews();
        initDialog();
        initEvents();
    }


    @Override
    protected void initViews() {
        TopNvgBar6 topNvgBar = (TopNvgBar6) findViewById(R.id.top_nvg_bar);
        topNvgBar.setVisibility(View.GONE);
        topNvgBar.setMyOnClickListener(new TopNvgBar6.MyOnClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRightClick() {

            }
        });
        pullLayout.setOnRefreshListener(this);
        adapter = new ExchangeListAdapter(mContext, messageList);
        listExchange.setAdapter(adapter);
        listExchange.canPullUp=false;
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExchangeList(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExchangeList(1);
            }
        });
    }

    @Override
    protected void initEvents() {
        getExchangeList(1);
    }


    public void getExchangeList(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<ExchangeBean>>> call;
        if (type == 1) {//货款
            call = userBiz.zphExchangeLogSurplus(token);
        } else {//积分
            call = userBiz.getExchangeList(token);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<ExchangeBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<ExchangeBean>>> arg0,
                                   Response<BaseResponse<List<ExchangeBean>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if(state==2){
                    if(null!=messageList){
                        messageList.clear();
                    }
                    pullLayout.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<ExchangeBean>> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<ExchangeBean> data = baseResponse.getData();
                        if (data != null && data.size() > 0) {
                            messageList.addAll(data);
                            stateLayout.showContentView();
                        } else {
                            stateLayout.showEmptyView("暂无数据");
                        }
                    }else{
                        String desc = baseResponse.getMsg();
                        ToastUtil.showToast(mContext,desc);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ExchangeBean>>> arg0, Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView("服务器连接失败,请检查您的网络状态");
                if(state==2){
                    pullLayout.refreshFinish(PullLayout.FAIL);
                }
            }
        });


    }

    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        getExchangeList(2);
    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {


    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {

        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            getExchangeList(1);
        } else {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
