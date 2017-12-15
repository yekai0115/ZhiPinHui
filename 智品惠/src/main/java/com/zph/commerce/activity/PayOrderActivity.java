package com.zph.commerce.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;
import com.zph.commerce.R;
import com.zph.commerce.alipay.PayResult;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.PayMethod;
import com.zph.commerce.bean.WxPayResult;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.constant.WxConstants;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.DialogWidget;
import com.zph.commerce.eventbus.AliPayMsg;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent2;
import com.zph.commerce.eventbus.MsgEvent7;
import com.zph.commerce.eventbus.WeiXinPayMsg;
import com.zph.commerce.eventbus.YinLianPayMsg;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.PayPasswordView;
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
 * Created by 24448 on 2017/10/20.
 */

public class PayOrderActivity extends BaseActivity {

    private final String mMode = "00";
    @ViewInject(R.id.tv_order_money)
    private TextView tv_order_money;
    @ViewInject(R.id.cb_weixin)
    private CheckBox cb_weixin;
    @ViewInject(R.id.cb_zhifubao)
    private CheckBox cb_zhifubao;
    @ViewInject(R.id.cb_card)
    private CheckBox cb_card;
    @ViewInject(R.id.cb_point)
    private CheckBox cb_point;

    @ViewInject(R.id.rl_point)
    private RelativeLayout rl_point;

    @ViewInject(R.id.rl_yinlian)
    private RelativeLayout rl_yinlian;

    @ViewInject(R.id.rl_zhifubao)
    private RelativeLayout rl_zhifubao;

    @ViewInject(R.id.rl_weixin)
    private RelativeLayout rl_weixin;


    @ViewInject(R.id.tv_name)
    private TextView tv_name;


    private int payType;
    private Context mContext;
    private String token;
    private String trade_id;
    private String pay_money;
    /**
     * 商品类型
     */
    private int goods_type;
    /**
     * 会员等级
     */
    private int rank_id;
    /**
     * 1:商品支付
     * 2：提货支付
     */
    private int type;

    //微信支付
    private PayReq req;
    private IWXAPI api;
    private DialogWidget mDialogWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_order);
        x.view().inject(this);
        EventBus.getDefault().register(this);
        mContext = this;
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        initViews();
        initEvents();
        initDialog();
        getPayMethod();
        type = getIntent().getIntExtra("type", 1);
        trade_id = getIntent().getStringExtra("trade_id");
        pay_money = getIntent().getStringExtra("pay_money");
        if (type == 1) {
            goods_type = getIntent().getIntExtra("goods_type", 1);
            rank_id = getIntent().getIntExtra("rank_id", 0);
        } else {
            rl_point.setVisibility(View.GONE);
            tv_name.setText("运费");
        }
        tv_order_money.setText(pay_money);
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
        req = new PayReq();
        api = WXAPIFactory.createWXAPI(mContext, WxConstants.APP_ID);
        api.registerApp(WxConstants.APP_ID);// 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
    }

    @Override
    protected void initEvents() {
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

    @Event({R.id.rl_weixin, R.id.rl_zhifubao, R.id.rl_yinlian, R.id.rl_point, R.id.tv_buy})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.rl_weixin://
                payType = 1;
                cb_zhifubao.setChecked(false);
                cb_card.setChecked(false);
                cb_point.setChecked(false);
                cb_weixin.setChecked(true);
                break;
            case R.id.rl_zhifubao://
                payType = 2;
                cb_weixin.setChecked(false);
                cb_card.setChecked(false);
                cb_point.setChecked(false);
                cb_zhifubao.setChecked(true);
                break;
            case R.id.rl_yinlian:
                payType = 3;
                cb_weixin.setChecked(false);
                cb_zhifubao.setChecked(false);
                cb_point.setChecked(false);
                cb_card.setChecked(true);
                break;
            case R.id.rl_point://
                payType = 4;
                cb_weixin.setChecked(false);
                cb_zhifubao.setChecked(false);
                cb_card.setChecked(false);
                cb_point.setChecked(true);
                break;
            case R.id.tv_buy://
                switch (payType) {
                    case 0:
                        ToastUtil.showToast(mContext, "请选择支付方式");
                        break;
                    case 1:
                        dialog.show();
                        weiXinPay();
                        break;
                    case 2:
                        dialog.show();
                        aliPay();
                        break;
                    case 3:
                        dialog.show();
                        payYinLian();
                        break;
                    case 4://鼓励积分支付
                        checkPayPwdState();
                        break;

                }
                break;
            default:
                break;
        }
    }

    private void checkPayPwdState() {
        int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
        if (payState == MyConstant.SUCCESS) {//已设置支付密码
            mDialogWidget = new DialogWidget(PayOrderActivity.this, getDecorViewDialog());
            mDialogWidget.show();
        } else {//未设置支付密码
            showSetPayPwdDialog();
        }
    }

    protected View getDecorViewDialog() {
        return PayPasswordView.getInstance("", "", this, new PayPasswordView.OnPayListener() {

            @Override
            public void onSurePay(String password) {
                mDialogWidget.dismiss();
                mDialogWidget = null;
                pointPay(password);
            }

            @Override
            public void onCancelPay() {
                mDialogWidget.dismiss();
                mDialogWidget = null;
            }
        }).getView();
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
        alert.showDialog(PayOrderActivity.this, getResources().getString(R.string.tv_40), "确定", "取消");
    }

    //支付密码设置成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent2 msgEvent2) {
        SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
    }




    /**
     * 鼓励积分支付
     */
    private void pointPay(String verifySecret) {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<Object>> call = userBiz.pointPay(token, trade_id,verifySecret);

        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        payScucess();
                    } else {
                        ToastUtil.showToast(mContext, msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    //银联支付
    private void payYinLian() {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<String>>> call;
        if (type == 1) {
            call = userBiz.getUnionOrder(token, trade_id);
        } else {
            call = userBiz.deliveryUnionOrder(token, trade_id);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<String>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<String>>> arg0,
                                   Response<BaseResponse<List<String>>> response) {
                super.onResponse(arg0, response);
                BaseResponse<List<String>> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<String> payOrder = baseResponse.getData();
                        if (null == payOrder || payOrder.isEmpty()) {
                            dialog.dismiss();
                            return;
                        }
                        EventBus.getDefault().post(new YinLianPayMsg(payOrder.get(0)));//发送事件：
                    } else {
                        dialog.dismiss();
                        ToastUtil.showToast(mContext, msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }

    /**
     * 支付宝支付
     */
    private void aliPay() {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<String>>> call;
        if (type == 1) {
            call = userBiz.getAlipayOrder(token, trade_id);
        } else {
            call = userBiz.deliveryAlipayOrder(token, trade_id);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<String>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<String>>> arg0,
                                   Response<BaseResponse<List<String>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<String>> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<String> payOrder = baseResponse.getData();
                        if (null == payOrder || payOrder.isEmpty()) {
                            return;
                        }
                        zhifubaoPay(payOrder.get(0));
                    } else if (code == (MyConstant.ERR_AUTH)) {//token过期

                    } else {
                        ToastUtil.showToast(mContext, msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<String>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    /**
     * 请求支付宝客户端
     */
    private void zhifubaoPay(final String payInfo) {

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(PayOrderActivity.this);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                EventBus.getDefault().post(new AliPayMsg(result));//发送事件：
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    private void weiXinPay() {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<WxPayResult>>> call = userBiz.getWXOrder(token, trade_id, type);

        call.enqueue(new HttpCallBack<BaseResponse<List<WxPayResult>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<WxPayResult>>> arg0,
                                   Response<BaseResponse<List<WxPayResult>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<WxPayResult>> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<WxPayResult> payOrder = baseResponse.getData();
                        if (null == payOrder || payOrder.isEmpty()) {
                            return;
                        }
                        weiXinPay(payOrder.get(0));
                    }else {
                        ToastUtil.showToast(mContext, msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<WxPayResult>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    /**
     * 打开微信客户端支付
     *
     * @param data
     */
    private void weiXinPay(WxPayResult data) {

        try {
            String appid = data.getAppid();//应用ID
            String partnerid = data.getPartnerid();//商户号
            String prepayid = data.getPrepayid();//预支付交易会话ID
            String packageValue = data.getPackageValue();//扩展字段
            String noncestr = data.getNoncestr();//随机字符串
            String timestamp = data.getTimestamp();//时间戳
            String sign = data.getSign();//签名
            packageValue = "Sign=WXPay";
            req.appId = appid;
            req.partnerId = partnerid;
            req.prepayId = prepayid;
            req.nonceStr = noncestr;
            req.timeStamp = timestamp;
            req.packageValue = packageValue;
            req.sign = sign;
            Runnable payRunnable = new Runnable() {

                @Override
                public void run() {
                    api.sendReq(req);
                }
            };
            //异步调用
            Thread payThread = new Thread(payRunnable);
            payThread.start();
        } catch (Exception e) {

        }
    }


    //支付宝支付回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(AliPayMsg aliPayMsg) {
        PayResult payResult = new PayResult((String) aliPayMsg.getMessage());
        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
        String resultInfo = payResult.getResult();
        String resultStatus = payResult.getResultStatus();
        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
        if (TextUtils.equals(resultStatus, "9000")) {
            ToastUtil.showToast(mContext, "支付成功");
            payScucess();
        } else {
            // 判断resultStatus 为非“9000”则代表可能支付失败
            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
            if (TextUtils.equals(resultStatus, "8000")) {
                ToastUtil.showToast(mContext, "支付结果确认中");
            } else {// resultStatus={6002};memo={网络连接异常};result={}
                // resultStatus={6001};memo={用户取消};result={}
                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                ToastUtil.showToast(mContext, "支付失败");

            }
        }
    }


    //微信支付回调
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(WeiXinPayMsg weiXinPayMsg) {
        int error_code = weiXinPayMsg.getTage();
        // 根据返回码判断，0是支付成功，-2是取消操作，其他是支付失败。返回0的话，再次请求一次接口，把上一个页面获取
        if (error_code == 0) {
            dialog.show();
         //   queryWxOrder();
            payScucess();
        } else if (error_code == -2) {// 用户取消支付
            ToastUtil.showToast(mContext, "取消支付");
            //  canclePay();
        } else {// 支付失败
            // canclePay();
        }
    }


    //银联支付
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(YinLianPayMsg payMsg) {
        String msg = payMsg.getMsg();
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (StringUtils.isBlank(msg)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PayOrderActivity.this);
            builder.setTitle("错误提示");
            builder.setMessage("网络连接失败,请重试!");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        } else {
            /*************************************************
             * 步骤2：通过银联工具类启动支付插件
             ************************************************/
            //在调用支付控件的代码按以下方式调用支付控件
                /*参数说明：
                    activity —— 用于启动支付控件的活动对象
                    spId —— 保留使用，这里输入null
                    sysProvider —— 保留使用，这里输入null
                    orderInfo —— 订单信息为交易流水号，即TN，为商户后台从银联后台获取。
                    mode —— 银联后台环境标识，“00”将在银联正式环境发起交易,“01”将在银联测试环境发起交易
                    返回值：
                    UPPayAssistEx.PLUGIN_VALID —— 该终端已经安装控件，并启动控件
                    UPPayAssistEx.PLUGIN_NOT_FOUND — 手机终端尚未安装支付控件，需要先安装支付控件
                    */
            UPPayAssistEx.startPay(PayOrderActivity.this, null, null, msg, mMode);
        }
    }

    //银联支付回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*************************************************
         * 步骤3：处理银联手机支付控件返回的支付结果
         ************************************************/
        if (data == null) {
            return;
        }
        String msg = "";
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            msg = "支付成功！";
            payScucess();
        } else if (str.equalsIgnoreCase("fail")) {
            msg = "支付失败！";
        } else if (str.equalsIgnoreCase("cancel")) {
            msg = "取消支付";
        }
        showShortToast(msg);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            if(payType==4){//避免清除了支付密码状态
                SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
            }
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 查询微信支付结果
     */
    private void queryWxOrder() {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<Object>>> call = userBiz.queryWXOrder(token, trade_id);
        call.enqueue(new HttpCallBack<BaseResponse<List<Object>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Object>>> arg0,
                                   Response<BaseResponse<List<Object>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<Object>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        payScucess();
                    } else {
                        payScucess();
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Object>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                payScucess();
            }
        });
    }

    /**
     * 支付成功
     */
    private void payScucess() {
        if (type == 1) {//商品支付成功
            Intent intent = new Intent(mContext, PaySuccessActivity.class);
            intent.putExtra("trade_id", trade_id);
            intent.putExtra("pay_money", pay_money);
            intent.putExtra("goods_type", goods_type);
            intent.putExtra("rank_id", rank_id);
            startActivity(intent);
            MyApplication.finishSingleActivityByClass(SubmitCarOrderActivity.class);
            MyApplication.finishSingleActivityByClass(GoodsDetalActivity.class);
        } else {//提货支付成功
            EventBus.getDefault().post(new MsgEvent7());//发送事件：
        }
        finish();
    }


    private void getPayMethod() {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<PayMethod>>> call = userBiz.getPayMethod();
        call.enqueue(new HttpCallBack<BaseResponse<List<PayMethod>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<PayMethod>>> arg0,
                                   Response<BaseResponse<List<PayMethod>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<PayMethod>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<PayMethod> payMethodList = baseResponse.getData();
                        if (null == payMethodList || payMethodList.isEmpty()) {
                            return;
                        }
                        PayMethod payMethod = payMethodList.get(0);
                        int alipay = payMethod.getAlipay();
                        int pointpay = payMethod.getPointpay();
                        int unionpay = payMethod.getUnionpay();
                        int wxpay = payMethod.getWxpay();
                        if (alipay == 1) {//支付宝
                            rl_zhifubao.setVisibility(View.VISIBLE);
                        } else {
                            rl_zhifubao.setVisibility(View.GONE);
                        }
                        if (pointpay == 1) {//鼓励积分
                            rl_point.setVisibility(View.VISIBLE);
                            if (type == 2) {
                                rl_point.setVisibility(View.GONE);
                            }
                        } else {
                            rl_point.setVisibility(View.GONE);
                        }
                        if (unionpay == 1) {//银联
                            rl_yinlian.setVisibility(View.VISIBLE);
                        } else {
                            rl_yinlian.setVisibility(View.GONE);
                        }
                        if (wxpay == 1) {//微信
                            rl_weixin.setVisibility(View.VISIBLE);
                        } else {
                            rl_weixin.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<PayMethod>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


}
