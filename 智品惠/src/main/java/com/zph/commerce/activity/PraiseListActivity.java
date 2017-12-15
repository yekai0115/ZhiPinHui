package com.zph.commerce.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.zph.commerce.R;
import com.zph.commerce.adapter.PraiseListAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.PraiseInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.popwindow.ActionItem;
import com.zph.commerce.view.popwindow.TitlePopAdapter;
import com.zph.commerce.view.popwindow.TitlePopWindow;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
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
 * 货款、鼓励积分奖励记录
 */

public class PraiseListActivity extends BaseActivity implements PullLayout.OnRefreshListener {

    private PraiseListAdapter adapter;
    private List<PraiseInfo> messageList = new ArrayList<>();
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
     * 1货款进账记录
     * 2鼓励积分进账记录
     */
    private int type;
    private TitlePopWindow window;
    private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();
    TopNvgBar6 topNvgBar;
    /*默认取全部*/
    private int status=3;

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
        initData();
    }


    @Override
    protected void initViews() {
        topNvgBar = (TopNvgBar6) findViewById(R.id.top_nvg_bar);
        topNvgBar.setTitle("记录明细");
        topNvgBar.setMyOnClickListener(new TopNvgBar6.MyOnClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRightClick() {
                showMorePopWindow();
            }
        });
        pullLayout.setOnRefreshListener(this);
        adapter = new PraiseListAdapter(mContext, messageList);
        listExchange.setAdapter(adapter);
        listExchange.canPullUp = (false);
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

    /**
     * 初始化数据
     */
    private void initData() {
        // 给标题栏弹窗添加子类
        mActionItems.add(new ActionItem("全部"));
        mActionItems.add(new ActionItem("已完成"));
        mActionItems.add(new ActionItem("待确认"));
        mActionItems.add(new ActionItem("交易关闭"));
    }


    private void showMorePopWindow() {
        window = new TitlePopWindow(this, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, mActionItems);
        if (Build.VERSION.SDK_INT == 24) {
            // 适配 android 7.0
            int[] location = new int[2];
            topNvgBar.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            window.showAtLocation(topNvgBar, Gravity.NO_GRAVITY, -100, y + topNvgBar.getHeight());
        } else {
            window.showAsDropDown(topNvgBar, 30, 0);
        }
//            window.showAsDropDown(img_pop, 30, 0);
        // 实例化标题栏按钮并设置监听
        window.setOnItemClickListener(new TitlePopAdapter.onItemClickListener() {

            @Override
            public void click(int position, View view) {
                //0 待确认  1 已确认 2 关闭交易  3  全部
                if(position==0){
                    status=3;
                }else  if(position==1){
                    status=1;
                }else  if(position==2){
                    status=0;
                }else  if(position==3){
                    status=2;
                }
                if (null != messageList) {
                    messageList.clear();
                }
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
        Call<BaseResponse<List<PraiseInfo>>> call;
        if (type == 1) {//货款
            call = userBiz.surplusMoneyLog(token,status);
        } else {//积分
            call = userBiz.encouragePointsLog(token,status);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<PraiseInfo>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<PraiseInfo>>> arg0,
                                   Response<BaseResponse<List<PraiseInfo>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    if (null != messageList) {
                        messageList.clear();
                    }
                    pullLayout.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<PraiseInfo>> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<PraiseInfo> data = baseResponse.getData();
                        if (data != null && data.size() > 0) {
                            messageList.addAll(data);
                            stateLayout.showContentView();
                        } else {
                            stateLayout.showEmptyView("暂无数据");
                        }
                    } else {
                        String desc = baseResponse.getMsg();
                        ToastUtil.showToast(mContext, desc);
                    }
                    adapter.notifyDataSetChanged();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<BaseResponse<List<PraiseInfo>>> arg0, Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView("服务器连接失败,请检查您的网络状态");
                if (state == 2) {
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
        // 加载操作
        getExchangeList(2);
        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
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
