package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.OrderDetalsAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.GoodsOrderInfo;
import com.zph.commerce.bean.Logistics;
import com.zph.commerce.bean.LogisticsBase;
import com.zph.commerce.bean.OrderDetals;
import com.zph.commerce.bean.OrderGoodsInfo;
import com.zph.commerce.bean.PayData;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent17;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.ListItemClickHelp;
import com.zph.commerce.utils.DateUtil;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableRefreshScrollView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.MyListView;
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
 * 描述 ：订单详情页面
 */
public class OrderDetalActivity extends BaseActivity implements PullLayout.OnRefreshListener, ListItemClickHelp {


    /**
     * 实付款
     */
    @ViewInject(R.id.tv_shifukuan)
    private TextView tv_shifukuan;

    /**
     * 订单状态
     */
    @ViewInject(R.id.tv_order_state)
    private TextView tv_order_state;


    /**
     * 收件人姓名
     */
    @ViewInject(R.id.tv_sh_name)
    private TextView tv_sh_name;
    /**
     * 收件人电话
     */
    @ViewInject(R.id.tv_phone)
    private TextView tv_phone;
    /**
     * 收件人地址
     */
    @ViewInject(R.id.tv_address)
    private TextView tv_address;


    /**
     * 最新物流状态
     */
    @ViewInject(R.id.tv_wl_state)
    private TextView tv_wl_state;
    /**
     * 最新物流状态
     */
    @ViewInject(R.id.tv_wl_content)
    private TextView tv_wl_content;
    /**
     * 最新物流状态
     */
    @ViewInject(R.id.tv_wl_time)
    private TextView tv_wl_time;

    /**
     * 实付金额
     */
    @ViewInject(R.id.tv_shifu)
    private TextView tv_shifu;
    /**
     * 订单号
     */
    @ViewInject(R.id.tv_order_id)
    private TextView tv_order_id;
    /**
     * 交易号
     */
    @ViewInject(R.id.tv_trade_id)
    private TextView tv_trade_id;
    /**
     * 下单时间
     */
    @ViewInject(R.id.tv_buy_time)
    private TextView tv_buy_time;
    /**
     * 支付时间
     */
    @ViewInject(R.id.tv_pay_time)
    private TextView tv_pay_time;
    /**
     * 发货时间
     */
    @ViewInject(R.id.tv_fahuo_time)
    private TextView tv_fahuo_time;

    /***/
    @ViewInject(R.id.tv_use_point)
    private TextView tv_use_point;

    @ViewInject(R.id.refresh_view)
    private PullLayout refresh_view;
    @ViewInject(R.id.mScrollView)
    private PullableRefreshScrollView mScrollView;


    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.lv_Order)
    private MyListView lv_Order;

    @ViewInject(R.id.btn_evaluate)
    private Button btn_evaluate;


    /**
     * 上下文
     **/
    private Context mContext;

    private Intent intent;
    /**
     * 订单号
     */
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
    List<OrderGoodsInfo> goodsInfoList = new ArrayList<>();
    private OrderDetalsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detal);
        mContext = this;
        x.view().inject(this);
        EventBus.getDefault().register(this);
        initDialog();
        intent = getIntent();
        status = intent.getIntExtra("status", 0);
        order_sn = intent.getStringExtra("order_sn");
        delivery_sn = intent.getStringExtra("delivery_sn");
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        refresh_view.setOnRefreshListener(this);
        adapter = new OrderDetalsAdapter(mContext, goodsInfoList, this, status);
        lv_Order.setAdapter(adapter);
        initViews();
        getOrderDetail(1);
        mScrollView.smoothScrollTo(0, 0);//避免
        tv_order_id.setText(order_sn);
        if (status == 1) {//未发货
            tv_order_state.setText("待发货");
        } else if (status == 2) {//已发货
            tv_order_state.setText("待收货");
        } else {
            tv_order_state.setText("已完成");
        }
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
        Call<Object> call = userBiz.getExpressInfomation(order_sn);
        call.enqueue(new Callback<Object>() {
            //LogisticsBase
            @Override
            public void onResponse(Call<Object> arg0,
                                   Response<Object> response) {
                dialog.dismiss();
                stateLayout.showContentView();
                Object baseResponse = response.body();
                if (null == baseResponse || StringUtils.isBlank(baseResponse.toString())) {
                    tv_wl_state.setText("处理中");
                    tv_wl_content.setText("订单正在处理中");
                    tv_wl_time.setText(DateUtil.getStandardTime(System.currentTimeMillis()));
                } else {
                    try {
                        String json = GsonUtil.GsonString(baseResponse);
                        LogisticsBase logisticsBase = GsonUtil.GsonToBean(json, LogisticsBase.class);
                        Logistics logistics = logisticsBase.getList().get(0);
                        switch (logisticsBase.getDeliverystatus()) {
                            case 1:
                                tv_wl_state.setText("在途中");
                                break;
                            case 2:
                                tv_wl_state.setText("派件中");
                                break;
                            case 3:
                                tv_wl_state.setText("已签收");
                                break;
                            case 4:
                                tv_wl_state.setText("派送失败");
                                break;

                        }
                        tv_wl_content.setText(logistics.getStatus());
                        tv_wl_time.setText(logistics.getTime());
                    } catch (Exception e) {
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
        if (state == 1) {
            stateLayout.showProgressView();
        }
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<OrderDetals>>> call = userBiz.myOrderListDetail(token, order_sn);
        call.enqueue(new HttpCallBack<BaseResponse<List<OrderDetals>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<OrderDetals>>> arg0,
                                   Response<BaseResponse<List<OrderDetals>>> response) {
                dialog.dismiss();
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<OrderDetals>> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        OrderDetals orderDetals = baseResponse.getData().get(0);
                        Address addr_info = orderDetals.getAddr_info();
                        GoodsOrderInfo goods_info = orderDetals.getGoods_info();
                        PayData payData = orderDetals.getPayData();
                        tv_sh_name.setText(addr_info.getAddr_name());
                        tv_phone.setText(addr_info.getAddr_mobile());
                        tv_address.setText(addr_info.getAddr_province() + addr_info.getAddr_city() + addr_info.getAddr_county() + addr_info.getAddr_detail());
                        goodsInfoList = GsonUtil.jsonToList(goods_info.getGoods_info(), OrderGoodsInfo.class);
                        adapter.updateListview(goodsInfoList,payData.getStatus());
                        setListviewHeight(lv_Order);
                        tv_use_point.setText(goods_info.getNocan_points());
                        tv_shifu.setText(goods_info.getTotalprice());
                        tv_shifukuan.setText(goods_info.getTotalprice());
                        tv_buy_time.setText(payData.getAdd_time());
                        tv_order_id.setText(payData.getOrder_sn());
                        tv_trade_id.setText(payData.getPay_return_id());
                        tv_pay_time.setText(payData.getPaytime());
                        tv_fahuo_time.setText(payData.getUpdate_time());
                        status = payData.getStatus();
                        if (status == 1) {//未发货
                            tv_order_state.setText("待发货");
                            btn_evaluate.setVisibility(View.GONE);
                        } else if (status == 2) {//已发货
                            tv_order_state.setText("待收货");
                            btn_evaluate.setVisibility(View.GONE);
                        } else {
                            tv_order_state.setText("已完成");
                            btn_evaluate.setVisibility(View.VISIBLE);
                        }
                        if (StringUtils.isBlank(delivery_sn) || delivery_sn.equals("0")) {
                            stateLayout.showContentView();
                            tv_wl_state.setText("处理中");
                            tv_wl_content.setText("订单正在处理中");
                            tv_wl_time.setText(DateUtil.getStandardTime(System.currentTimeMillis()));
                            return;
                        }
                        getOrderWl();
                    } else {
                        stateLayout.showEmptyView(msg);
                    }
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderDetals>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "请检查你的网络设置");
                stateLayout.showErrorView("请检查你的网络设置");
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.FAIL);
                }
            }
        });

    }

    @Event({R.id.ll_wl, R.id.btn_operate})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.ll_wl://查看物流
                queryDelivery();
                break;
            case R.id.btn_operate:
                queryDelivery();
                break;
        }
    }

    private void queryDelivery() {
        if (StringUtils.isBlank(delivery_sn) || delivery_sn.equals("0")) {
            ToastUtil.showToast(mContext, "暂无物流信息");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("delivery_sn", delivery_sn);//物流单号
        String goods_logo = goodsInfoList.get(0).getPic();
        String[] arr = goods_logo.split(",");
        intent.putExtra("goods_logo", arr[0]);
        intent.putExtra("order_sn", order_sn);
        intent.setClass(OrderDetalActivity.this, OrderWuliuActivity.class);
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

    //换货申请提交成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent17 messageEvent) {
            getOrderDetail(1);
    }



    /**
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + 30;
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }

    @Override
    public void onClick(View item, View widget, int position, int which) {

        OrderGoodsInfo orderGoodsInfo = goodsInfoList.get(position);
        String status = orderGoodsInfo.getStatus();
        if (StringUtils.isBlank(status)) {//申请售后
            String goods_logo = orderGoodsInfo.getPic();
            String[] arr = goods_logo.split(",");
            intent = new Intent(mContext, AfterSaleActivity.class);
            intent.putExtra("value",orderGoodsInfo.getValue());
            intent.putExtra("order_sn",order_sn);
            intent.putExtra("name",orderGoodsInfo.getName());
            intent.putExtra("attr_id",orderGoodsInfo.getAttr_id());
            intent.putExtra("pic",MyConstant.ALI_PUBLIC_URL + arr[0]);
            startActivity(intent);
        }


    }
}
