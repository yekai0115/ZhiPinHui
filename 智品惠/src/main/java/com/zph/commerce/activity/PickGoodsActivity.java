package com.zph.commerce.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.OrderPay;
import com.zph.commerce.bean.Shipping;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.DialogWidget;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent2;
import com.zph.commerce.eventbus.MsgEvent7;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.Inventory;
import com.zph.commerce.utils.CompuUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
import com.zph.commerce.view.PayPasswordView;
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
 * 提货页面
 */

public class PickGoodsActivity extends BaseActivity{


    @ViewInject(R.id.ll_chose_dizhi)
    private LinearLayout ll_chose_dizhi;

    @ViewInject(R.id.ll_no_address)
    private LinearLayout ll_no_address;
    @ViewInject(R.id.ll_address)
    private LinearLayout ll_address;
    /**
     * 收货人名字
     */
    @ViewInject(R.id.tv_sh_name)
    private TextView tv_sh_name;
    /**
     * 收货人手机号
     */
    @ViewInject(R.id.tv_phone)
    private TextView tv_sh_phone;

    /**
     * 收货人地址
     */
    @ViewInject(R.id.tv_address)
    private TextView tv_sh_des;



    /**
     * 提货数量
     */
    @ViewInject(R.id.pick_edit_num)
    private EditText pickEditNum;

    @ViewInject(R.id.btn_pick)
    private Button btnPick;
    /**
     * 商品标题
     */
    @ViewInject(R.id.name_robot)
    private HandyTextView name_robot;
    /**
     * 库存数量
     */
    @ViewInject(R.id.tv_num)
    private HandyTextView tv_num;


    @ViewInject(R.id.iv_goods)
    private ImageView iv_goods;
    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;
    @ViewInject(R.id.tv_yunfei)
    private TextView tv_yunfei;

    private String token;
    private Context mContext;

    private Address destination;
    /**
     * 地址id
     */
    private String fdeliveryAddrId;
    /**
     * 邮费模板id
     */
    private String shipping_id;

    /**
     * 收货人
     */
    private String accept_name;
    /**
     * 收货人详细地址
     */
    private String address;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 省市区
     */
    private String cr_id;
    /**
     * 可提货数量
     */
    private String number;
    private String goods_id;
    private DialogWidget mDialogWidget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platform_pick_goods);
        x.view().inject(this);
        EventBus.getDefault().register(this);
        initViews();
        initEvents();
        mContext = this;
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        initDialog();
        getPlatformStocksInfo();
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
        pickEditNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String num= pickEditNum.getText().toString();
                if(StringUtils.isBlank(num)||num.startsWith("0")){
                    tv_yunfei.setText("");
                    return;
                }
                if(StringUtils.isBlank(fdeliveryAddrId)){
                    tv_yunfei.setText("");
                    return;
                }
                getDeliveryPriceByAddId(num);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Event({R.id.btn_pick,R.id.ll_chose_dizhi})
    private void click(View view) {
        switch (view.getId()){
            case R.id.btn_pick://提货动作
                if(StringUtils.isBlank(fdeliveryAddrId)){
                    ToastUtil.showToast(mContext,"请选择提货地址");
                    return;
                }
               String num= pickEditNum.getText().toString();
                if(StringUtils.isBlank(num)||num.startsWith("0")){
                    ToastUtil.showToast(mContext,"请输入提货数量");
                    return;
                }
                int compare=CompuUtils.compareTo(num,number);
                if(compare>0){
                    ToastUtil.showToast(mContext,"超过最大可提货数量");
                    return;
                }
                checkPayPwdState(num);
                break;
            case R.id.ll_chose_dizhi: //跳转收获地址
                Intent intent = new Intent(mContext, AddressChooseActivity.class);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
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
                            goods_id= inventory.getGoods_id();
                            number=inventory.getNumber();
                            tv_num.setText("X" + number);
                            name_robot.setText(inventory.getName());
                            String goods_logo = inventory.getPic();
                            String[] arr = goods_logo.split(",");
                            goods_logo= arr == null||arr.length==0 ? "" : arr[0];
                            Glide.with(mContext)
                                    .load(MyConstant.ALI_PUBLIC_URL + goods_logo)
                                    .fitCenter()
                                    .placeholder(R.drawable.pic_nomal_loading_style)
                                    .error(R.drawable.pic_nomal_loading_style)
                                    .into(iv_goods);
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




    public void pickUpGood(String num,String password) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<OrderPay>>> call = userBiz.pickUpGood(token,fdeliveryAddrId,goods_id,num,password);
        call.enqueue(new HttpCallBack<BaseResponse<List<OrderPay>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<OrderPay>>> arg0,
                                   Response<BaseResponse<List<OrderPay>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                super.onResponse(arg0, response);
                BaseResponse<List<OrderPay>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<OrderPay> data = baseResponse.getData();
                        if (null == data || data.isEmpty()) {
                            return;
                        }
                        OrderPay orderPay = data.get(0);
                        String trade_id = orderPay.getTrade_id();
                        String pay_money = orderPay.getPay_money();
                        Intent intent = new Intent(mContext, PayOrderActivity.class);
                        intent.putExtra("trade_id", trade_id);
                        intent.putExtra("pay_money", pay_money);
                        intent.putExtra("type", 2);
                        startActivity(intent);
                    }else{
                        String msg= baseResponse.getMsg();
                        ToastUtil.showToast(mContext,msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderPay>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                ToastUtil.showToast(mContext,"服务器连接失败,请检查您的网络设置");
            }
        });
    }




    public void getDeliveryPriceByAddId(final String num) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<Shipping>>> call = userBiz.getDeliveryPriceByAddId(token,fdeliveryAddrId,goods_id);
        call.enqueue(new HttpCallBack<BaseResponse<List<Shipping>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Shipping>>> arg0,
                                   Response<BaseResponse<List<Shipping>>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                super.onResponse(arg0, response);
                BaseResponse<List<Shipping>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<Shipping> data = baseResponse.getData();
                        if (null == data || data.isEmpty()) {
                            return;
                        }
                        Shipping shipping = data.get(0);
                        String price=shipping.getPrice();
                        int compare=CompuUtils.compareTo("0",price);
                        if(StringUtils.isBlank(price)||compare>=0){
                            tv_yunfei.setText("快递 包邮");
                        }else{
                            price=CompuUtils.multiply(price,num).toString();
                            tv_yunfei.setText("快递 ¥"+price);
                        }
                    }else{
                        String msg= baseResponse.getMsg();
                        ToastUtil.showToast(mContext,msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Shipping>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                ToastUtil.showToast(mContext,"服务器连接失败,请检查您的网络设置");
            }
        });
    }







    /**
     * 1:地址选择回调
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case 1:// 地址回调
                try {
                    int resultType = bundle.getInt("result");// 从收货地址列表重新选择一个地址地址回调(2)、没有地址新增一个地址回调(1)
                    if (resultType == 0) {
                        ll_address.setVisibility(View.GONE);
                        ll_no_address.setVisibility(View.VISIBLE);
                        tv_yunfei.setText("");
                    } else {
                        ll_no_address.setVisibility(View.GONE);
                        ll_address.setVisibility(View.VISIBLE);
                        destination = bundle.getParcelable("place");
                        accept_name = destination.getAddr_name();
                        tv_sh_name.setText(accept_name);
                        fdeliveryAddrId = destination.getAddr_id();
                        mobile = destination.getAddr_mobile();
                        tv_sh_phone.setText(mobile);
                        address = destination.getAddr_detail();
                        String area = destination.getAddr_county();
                        if (StringUtils.isBlank(area)) {
                            cr_id = destination.getAddr_province_name() + " " + destination.getAddr_city_name();
                        } else {
                            cr_id = destination.getAddr_province_name() + " " + destination.getAddr_city_name() + " " + destination.getAddr_county_name();
                        }
                        tv_sh_des.setText("收货地址:" + cr_id + " " + address);

                        String num= pickEditNum.getText().toString();
                        if(StringUtils.isBlank(num)||num.startsWith("0")){
                            return;
                        }
                        getDeliveryPriceByAddId(num);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected View getDecorViewDialog(final String num) {
        return PayPasswordView.getInstance("", "", this, new PayPasswordView.OnPayListener() {

            @Override
            public void onSurePay(String password) {
                mDialogWidget.dismiss();
                mDialogWidget = null;
                pickUpGood(num,password);
            }

            @Override
            public void onCancelPay() {
                mDialogWidget.dismiss();
                mDialogWidget = null;
            }
        }).getView();
    }

    //提货支付成功回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent7 msgEvent7) {
        getPlatformStocksInfo();
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


    private void checkPayPwdState(String num) {
        int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
        if (payState == MyConstant.SUCCESS) {//已设置支付密码
            mDialogWidget = new DialogWidget(PickGoodsActivity.this, getDecorViewDialog(num));
            mDialogWidget.show();
        } else {//未设置支付密码
            showSetPayPwdDialog();
        }
    }

    /**
     * 设置支付密码弹窗
     */
    private void showSetPayPwdDialog() {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//进入设置支付密码页面
                    startActivity(SetPayPwdActivity.class);
                }

            }
        });
        alert.showDialog(PickGoodsActivity.this, getResources().getString(R.string.tv_40), "确定", "取消");
    }

    //支付密码设置成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent2 msgEvent2) {
        SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
    }
}
