package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.IntegralPoint;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
import com.zph.commerce.view.pullableview.PullableListView;
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
import retrofit2.Response;

/**
 * 智品积分记录
 */

public class WisdomIntegralActivity extends BaseActivity implements PullLayout.OnRefreshListener {


    @ViewInject(R.id.wisdom_integral_lv)
    private PullableListView wisdomIntegralLv;

    private IntegralPointAdapter integralPointAdapter;
    private List<IntegralPoint> integralPointList = new ArrayList<>();


    @ViewInject(R.id.product_refresh_view)
    private PullLayout layout;


    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;
    /**
     * 上下文
     **/
    private Context mContext;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wisdom_integral);
        x.view().inject(this);
        mContext = this;
        // 注册事件
        EventBus.getDefault().register(this);
        initViews();
        initDialog();
        initEvents();
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
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        layout.setOnRefreshListener(this);
        wisdomIntegralLv.canPullUp=false;
        integralPointAdapter = new IntegralPointAdapter();
        wisdomIntegralLv.setAdapter(integralPointAdapter);
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateLayout.showProgressView();
                queryWisdomIntegralList(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateLayout.showProgressView();
                queryWisdomIntegralList(1);
            }
        });
    }

    @Override
    protected void initEvents() {
        queryWisdomIntegralList(1);
    }

    public void queryWisdomIntegralList(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<IntegralPoint>>> call = userBiz.intelligencePointsLog(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<IntegralPoint>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<IntegralPoint>>> arg0,
                                   Response<BaseResponse<List<IntegralPoint>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    if (null != integralPointList) {
                        integralPointList.clear();
                    }
                    layout.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<IntegralPoint>> baseResponse = response.body();
                if (null != baseResponse) {
                    stateLayout.showContentView();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<IntegralPoint> data = baseResponse.getData();
                        if (data != null && data.size() > 0) {
                            integralPointList.addAll(data);
                        } else {
                            stateLayout.showEmptyView("暂无数据");
                        }
                    } else {
                        String msg = baseResponse.getMsg();
                        ToastUtil.showToast(mContext, msg);
                    }

                    integralPointAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<IntegralPoint>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    layout.refreshFinish(PullLayout.FAIL);
                }
                stateLayout.showErrorView("服务器连接失败,请检查您的网络设置");
                integralPointAdapter.notifyDataSetChanged();
            }
        });
    }

    class IntegralPointAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            return integralPointList.size();
        }

        @Override
        public Object getItem(int position) {
            return integralPointList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(WisdomIntegralActivity.this, R.layout.adapter_commdiy_points_list, null);
                holder = new ViewHolder();
                holder.pPointTitle = (HandyTextView) convertView.findViewById(R.id.point_title);
                holder.pPointNum = (HandyTextView) convertView.findViewById(R.id.point_num);
                holder.pPointTime = (HandyTextView) convertView.findViewById(R.id.point_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                holder.pPointTitle.setText(integralPointList.get(position).getRemarks());
                holder.pPointNum.setText(integralPointList.get(position).getPoint());
                holder.pPointTime.setText(integralPointList.get(position).getAdd_time());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    public class ViewHolder {
        private HandyTextView pPointTitle;
        private HandyTextView pPointNum;
        private HandyTextView pPointTime;

    }

    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        queryWisdomIntegralList(2);

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
            queryWisdomIntegralList(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
