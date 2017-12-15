package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent7;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.Inventory;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
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
import retrofit2.Response;

/**
 * 库存页面
 */

public class PlatformInventoryActivity extends BaseActivity {


    @ViewInject(R.id.image_robot)
    private ImageView imageRobot;

    @ViewInject(R.id.name_robot)
    private HandyTextView nameRobot;

    @ViewInject(R.id.platform_inventory_num)
    private HandyTextView pInventoryNum;

    @ViewInject(R.id.available_inventory_num)
    private HandyTextView aInventoryNum;

    @ViewInject(R.id.btn_confirm)
    private HandyTextView btnConfirm;

    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    private String token;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_inventory);
        x.view().inject(this);
        mContext = this;
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
                startActivity(InventoryDetailActivity.class);
            }
        });
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPlatformStocksInfo();
            }
        });
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPlatformStocksInfo();
            }
        });
    }

    @Event({R.id.btn_confirm})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                startActivity(PickGoodsActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initEvents() {
        getPlatformStocksInfo();
    }

    public void getPlatformStocksInfo() {
        dialog.show();
        stateLayout.showProgressView();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<Inventory>>> call = userBiz.getstocks(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<Inventory>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Inventory>>> arg0,
                                   Response<BaseResponse<List<Inventory>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                super.onResponse(arg0, response);
                BaseResponse<List<Inventory>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<Inventory> data = baseResponse.getData();
                        if (null==data||data.isEmpty()) {
                            stateLayout.showEmptyView("暂无数据");
                        }else{
                            stateLayout.showContentView();
                            Inventory inventory=data.get(0);
                            pInventoryNum.setText("X" + inventory.getNumber());
                            nameRobot.setText(inventory.getName());
                            String goods_logo = inventory.getPic();
                            String[] arr = goods_logo.split(",");
                            goods_logo= arr == null||arr.length==0 ? "" : arr[0];
                            Glide.with(PlatformInventoryActivity.this)
                                    .load(MyConstant.ALI_PUBLIC_URL + goods_logo)
                                    .fitCenter()
                                    .placeholder(R.drawable.pic_nomal_loading_style)
                                    .error(R.drawable.pic_nomal_loading_style)
                                    .into(imageRobot);
                        }

                    }else{
                        stateLayout.showEmptyView("暂无数据");
                        String msg=baseResponse.getMsg();
                        ToastUtil.showToast(mContext,msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Inventory>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                stateLayout.showErrorView("网络连接失败");
            }
        });
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
            getPlatformStocksInfo();
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }

    //提货支付成功回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent7 msgEvent7) {
        getPlatformStocksInfo();
    }
}
