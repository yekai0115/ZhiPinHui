package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.EvaluateAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.CommentContent;
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
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 评价列表
 */

public class EvaluateListActivity extends BaseActivity implements PullLayout.OnRefreshListener {
    private Context context;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.lv_evaluate)
    private ListView lv_evaluate;
    @ViewInject(R.id.refresh_view)
    private PullLayout ptrl;

    @ViewInject(R.id.tv_total)
    private TextView tv_total;

    @ViewInject(R.id.tv_good)
    private TextView tv_good;

    @ViewInject(R.id.tv_medium)
    private TextView tv_medium;

    @ViewInject(R.id.tv_negative)
    private TextView tv_negative;

    private EvaluateAdapter adapter;
    private List<CommentContent> orderInfoList = new ArrayList<CommentContent>();
    private String token;
    private int leval;
    private String goods_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_list);
        MyApplication.addActivity(this);
        x.view().inject(this);
        context = this;
        EventBus.getDefault().register(this);
        token = (String) SPUtils.get(context, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        leval = getIntent().getIntExtra("leval", 0);
        goods_id = getIntent().getStringExtra("goods_id");
        initViews();
        initDialog();
        setStatus(1);

    }

    @Override
    protected void initViews() {
        ptrl.setOnRefreshListener(this);
        // ptrl.autoRefresh(); //去掉自动刷新
        adapter = new EvaluateAdapter(context, orderInfoList);
        lv_evaluate.setAdapter(adapter);
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentList(2);
            }
        });

        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentList(2);
            }
        });

        TopNvgBar5 top_nvg_bar=(TopNvgBar5)findViewById(R.id.top_nvg_bar);
        top_nvg_bar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
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


    @Event({R.id.tv_total, R.id.tv_good, R.id.tv_medium, R.id.tv_negative})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.tv_total://全部
                leval = 0;
                setStatus(2);
                break;
            case R.id.tv_good://好评
                leval = 1;
                setStatus(2);

                break;
            case R.id.tv_medium://中评
                leval = 2;
                setStatus(2);

                break;
            case R.id.tv_negative://差评
                leval = 3;
                setStatus(2);
                break;
            default:
                break;
        }
    }


    private void setStatus(int state) {
        switch (leval) {
            case 0:
                tv_total.setSelected(true);
                tv_good.setSelected(false);
                tv_medium.setSelected(false);
                tv_negative.setSelected(false);
                break;
            case 1:
                tv_good.setSelected(true);
                tv_medium.setSelected(false);
                tv_negative.setSelected(false);
                tv_total.setSelected(false);
                break;
            case 2:
                tv_medium.setSelected(true);
                tv_good.setSelected(false);
                tv_negative.setSelected(false);
                tv_total.setSelected(false);
                break;
            case 3:
                tv_negative.setSelected(true);
                tv_medium.setSelected(false);
                tv_good.setSelected(false);
                tv_total.setSelected(false);
                break;
        }
        commentList(state);
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
    private void commentList(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<CommentContent>>> call = userBiz.commentList(token, goods_id, leval);
        call.enqueue(new Callback<BaseResponse<List<CommentContent>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CommentContent>>> arg0,
                                   Response<BaseResponse<List<CommentContent>>> response) {
                dialog.dismiss();
                if (state == 2) {
                    if (orderInfoList != null) {
                        orderInfoList.clear();
                    }
                    ptrl.refreshFinish(PullLayout.SUCCEED);
                }
                BaseResponse<List<CommentContent>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<CommentContent> data = baseResponse.getData();
                        if (null == data || data.isEmpty()) {
                            stateLayout.showEmptyView("暂无数据");
                            adapter.notifyDataSetChanged();
                        } else {
                            orderInfoList.addAll(data);
                            stateLayout.showContentView();
                            adapter.updateListView(orderInfoList);
                            setListviewHeight(lv_evaluate);
                        }
                    } else {
                        ToastUtil.showToast(context, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CommentContent>>> arg0,
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
        commentList(2);

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
            commentList(2);
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }
}
