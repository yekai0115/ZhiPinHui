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
import com.zph.commerce.model.InventoryDetails;
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
 * Created by 24448 on 2017/10/20.
 * 库存明细
 */

public class InventoryDetailActivity extends BaseActivity implements PullLayout.OnRefreshListener {


    @ViewInject(R.id.inventory_detail_lv)
    private PullableListView inventoryDetailLv;

    private InventoryDetailAdapter inventoryDetailAdapter;
    private List<InventoryDetails> inventoryDetailsList = new ArrayList<>();


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
        setContentView(R.layout.activity_inventory_details);
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
        inventoryDetailAdapter = new InventoryDetailAdapter();
        inventoryDetailLv.setAdapter(inventoryDetailAdapter);
        inventoryDetailLv.canPullUp = false;
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryInventoryDetailList(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryInventoryDetailList(1);
            }
        });
    }

    @Override
    protected void initEvents() {
        queryInventoryDetailList(1);
    }


    public void queryInventoryDetailList(final int state) {
        dialog.show();
        stateLayout.showProgressView();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<InventoryDetails>>> call = userBiz.getstocksLog(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<InventoryDetails>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<InventoryDetails>>> arg0,
                                   Response<BaseResponse<List<InventoryDetails>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    if (null != inventoryDetailsList) {
                        inventoryDetailsList.clear();
                    }
                    layout.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<InventoryDetails>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<InventoryDetails> data = baseResponse.getData();
                        if (data != null && data.size() > 0) {
                            inventoryDetailsList.addAll(data);
                            stateLayout.showContentView();

                        } else {
                            stateLayout.showEmptyView("暂无数据");
                        }
                    } else {
                        String msg = baseResponse.getMsg();
                        ToastUtil.showToast(mContext, msg);
                    }
                    inventoryDetailAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<InventoryDetails>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                stateLayout.showErrorView("服务器连接失败,请检查您的网络状态");
            }
        });
    }

    class InventoryDetailAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            return inventoryDetailsList.size();
        }

        @Override
        public Object getItem(int position) {
            return inventoryDetailsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            InventoryDetails inventoryDetails = (InventoryDetails) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(InventoryDetailActivity.this, R.layout.adapter_exchange_list_item, null);
                holder = new ViewHolder();
                holder.tv_bank = (HandyTextView) convertView.findViewById(R.id.tv_bank);
                holder.tv_money = (HandyTextView) convertView.findViewById(R.id.tv_money);
                holder.tv_time = (HandyTextView) convertView.findViewById(R.id.tv_time);
                holder.iar_status = (HandyTextView) convertView.findViewById(R.id.iar_status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                holder.tv_bank.setText(inventoryDetails.getRemarks());

                holder.tv_time.setText(inventoryDetails.getAdd_time());
                holder.iar_status.setVisibility(View.GONE);
                int type = inventoryDetails.getType();
                if (type == 0) {
                    holder.tv_money.setText("+" + inventoryDetails.getNumber());
                } else {
                    holder.tv_money.setText("-" + inventoryDetails.getNumber());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }

    public class ViewHolder {
        private HandyTextView tv_bank;
        private HandyTextView tv_money;
        private HandyTextView tv_time;
        private HandyTextView iar_status;

    }

    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        queryInventoryDetailList(2);

    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {
        // 加载操作
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            queryInventoryDetailList(1);
        } else {
            MyConstant.HASLOGIN = false;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
