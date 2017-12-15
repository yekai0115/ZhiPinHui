package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.ExchangeInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.DialogExchangeTips;
import com.zph.commerce.dialog.DialogWidget;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent2;
import com.zph.commerce.eventbus.MsgEvent3;
import com.zph.commerce.eventbus.MsgEvent5;
import com.zph.commerce.eventbus.MsgEvent8;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.CommonUtils;
import com.zph.commerce.utils.CompuUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
import com.zph.commerce.view.PayPasswordView;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class ExchangeActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    /**总积分*/
    private HandyTextView canCash;
    private HandyTextView aeBankName;
    private Button confirmButton;
    private EditText exchangeEdit;
    private TextView tv_name;
    private HandyTextView tv_able_money;
    private TextView tv_tips;
    private DialogConfirm dialogConfirm;
    private DialogWidget mDialogWidget;
    private String token;
    private String bankCard;
    private  String freeze;//被冻结部分
    /**
     * 1货款提现
     * 2积分提现
     */
    private int type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        mContext = this;
        // 注册事件
        EventBus.getDefault().register(this);
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        dialogConfirm = new DialogConfirm();
        type=getIntent().getIntExtra("state",1);
        initViews();
        initDialog();
        initEvents();
        getExchangeInfo(1);
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
                Intent intent = new Intent(mContext, ExchangeListActivity.class);
                intent.putExtra("state",type);
                startActivity(intent);
            }
        });
        aeBankName = (HandyTextView) findViewById(R.id.ae_bank_name);
        confirmButton = (Button) findViewById(R.id.confirm_button);
        exchangeEdit = (EditText) findViewById(R.id.act_exchange_edit);
        canCash = (HandyTextView) findViewById(R.id.ae_can_cash);
        tv_name= (TextView) findViewById(R.id.tv_name);
        tv_able_money= (HandyTextView) findViewById(R.id.tv_able_money);
        tv_tips= (TextView) findViewById(R.id.tv_tips);
        aeBankName.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        tv_able_money.setOnClickListener(this);
        if(type==1){//货款
            topNvgBar.setTitle("货款提现");
            tv_name.setText("可提现货款");
            tv_tips.setText(mContext.getResources().getString(R.string.tishi_84));
        }else{//积分
            topNvgBar.setTitle("鼓励积分");
            tv_tips.setText(mContext.getResources().getString(R.string.tishi_85));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ae_bank_name:
                if (StringUtils.isBlank(bankCard)) {//绑卡：需要设置支付密码、实名认证
                    checkPayPwdState(1);
                } else {//改卡：已经设置了支付密码、已提交实名认证
                    checkVeriState(3);//查询认证状态
                }
                break;
            case R.id.confirm_button://兑换
                confirmExchange();
                break;
            case R.id.tv_able_money:
                showExchangeTipsDialog();
                break;
        }
    }

    /**
     * 检查参数
     */
    private void confirmExchange() {
        if (StringUtils.isBlank(bankCard)) {
            showShortToast("您还没有绑定银行卡");
            return;
        }
        String value = exchangeEdit.getText().toString();//输入的金额
        if (!StringUtils.isBlank(value)) {
            long amount = Long.parseLong(value);
            BigDecimal amountMoney = new BigDecimal(value);
            BigDecimal money = new BigDecimal(tv_able_money.getText().toString());//可兑换金额
            if (amountMoney.compareTo(money) == 1) {
                ToastUtil.showToast(mContext,"超过最大可用提现金额");
                return;
            }
            if (amount <= 0) {
                ToastUtil.showToast(mContext,"提现金额不合法，请重新输入");
                return;
            }
            if (amount % 100 != 0) {
                ToastUtil.showToast(mContext,"提现金额必须是100的整数倍");
                return;
            }
            int compare = CommonUtils.compareTo(value, "10000");
            if (compare == 1) {
                ToastUtil.showToast(mContext,"一次最多只能兑换10000");
                return;
            }
            checkPayPwdState(2);//查询是否设置了支付密码
        } else {
            ToastUtil.showToast(mContext,"请输入提现金额");
            return;
        }
    }


    /**
     * 查询支付密码状态
     * 1：绑卡操作
     * 2：兑换操作
     */
    private void checkPayPwdState(int state) {
        int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
        if (payState == MyConstant.SUCCESS) {//已设置支付密码
            if (state == 1) {//绑卡操作：已设置支付密码,未实名认证
                showVerifyDialog(1, getResources().getString(R.string.tv_41));
            } else if (state == 2) {//兑换操作：已设置支付密码,查询认证状态
                checkVeriState(2);
            }
        } else {//未设置支付密码
            showSetPayPwdDialog();
        }
    }

    /**
     * 查询认证状态
     * 1：默认查询
     * 2：兑换操作
     * 3：修改银行卡操作
     */
    private void checkVeriState(int state) {
        int status = (int) SPUtils.get(mContext, "verified" + token, 0);
        if (status == 3) {//已认证
            if (state == 2) {//兑换
                showExchangeDialog();
            } else if (state == 3) {//修改银行卡
                showResetBankDialog();
            }
        } else {//查询认证状态
            getExchangeInfo(state);
        }
    }


    /**
     * 弹出兑换密码弹窗
     */
    private void showExchangeDialog() {
        String value = exchangeEdit.getText().toString();
        mDialogWidget = new DialogWidget(ExchangeActivity.this, getDecorViewDialog(value));
        mDialogWidget.show();
    }


    /**
     * 修改银行卡提示（已设置支付密码，已实名认证）
     */
    private void showResetBankDialog() {
        dialogConfirm.showDialog(ExchangeActivity.this, getResources().getString(R.string.ae_alter_bank), "确定", "取消");
        dialogConfirm.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {
                    startActivity(ConfirmPayPwdActivity.class);
                }
            }
        });
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
        alert.showDialog(ExchangeActivity.this, getResources().getString(R.string.tv_40), "确定", "取消");
    }


    private void showExchangeTipsDialog() {
        DialogExchangeTips alert = new DialogExchangeTips();
        alert.showDialog(ExchangeActivity.this, freeze,type);
    }



    /**
     * 实名认证弹窗
     * @param state 1未认证；2认证未通过
     * @param title
     */
    private void showVerifyDialog(final int state, String title) {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//进入实名认证流程页面
                    Intent intent = new Intent(mContext, CertificationBindUserActivity.class);
                    intent.putExtra("state",state);
                    startActivity(intent);
                }

            }
        });
        alert.showDialog(ExchangeActivity.this, title, "确定", "取消");
    }


    protected View getDecorViewDialog(final String amount) {
        return PayPasswordView.getInstance(amount, "兑换", this, new PayPasswordView.OnPayListener() {

            @Override
            public void onSurePay(String password) {
                mDialogWidget.dismiss();
                mDialogWidget = null;
                confirmPwd(password);
            }

            @Override
            public void onCancelPay() {
                mDialogWidget.dismiss();
                mDialogWidget = null;
            }
        }).getView();
    }

    @Override
    protected void initEvents() {

    }

    /**
     * 查询认证状态
     * 1：默认查询
     * 2：兑换操作
     * 3：修改银行卡操作
     */
    private void getExchangeInfo(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<ExchangeInfo>>> call;
        if(type==1){//货款
            call = userBiz.getExchangeInfoSurplus(token);
        }else{//积分
            call = userBiz.exchangeInfo(token);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<ExchangeInfo>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<ExchangeInfo>>> arg0,
                                   Response<BaseResponse<List<ExchangeInfo>>> response) {
                dialog.dismiss();
                super.onResponse(arg0,response);
                BaseResponse<List<ExchangeInfo>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        List<ExchangeInfo> data = baseResponse.getData();
                        ExchangeInfo exchangeInfo = data.get(0);
                        bankCard = exchangeInfo.getBankname();
                        if (!StringUtils.isBlank(bankCard)) {
                            aeBankName.setText(bankCard);
                        }
                        String can_cash = exchangeInfo.getPoints();;//可兑换部分
                         freeze = exchangeInfo.getCan_cash1();//不可兑换部分
                        String total = CompuUtils.add(can_cash, freeze).toString();
                        canCash.setText(total);
                        tv_able_money.setText(can_cash);
                        int status = exchangeInfo.getStatus();//认证状态0未提交,1认证失败,2审核中,3认证通过
                        SPUtils.put(mContext, "verified" + token, status);
                        if (status == 3) {//认证通过
                            if (state == 2) {//兑换操作
                                showExchangeDialog();
                            } else if (state == 3) {//修改银行卡操作
                                showResetBankDialog();
                            }
                        } else if (status == 0) {//未认证
                            if (state == 2) {//兑换操作:已设置支付密码，未实名认证
                                showVerifyDialog(1, getResources().getString(R.string.tv_41));
                            }
                        } else if (status == 2) {//审核中
                            if (state != 1) {//
                                ToastUtil.showToast(mContext, "您的实名认证正在审核中");
                            }
                        } else {
                            if (state != 1) {//认证失败，修改认证信息
                                showVerifyDialog(2, getResources().getString(R.string.tishi_12));
                            }

                        }
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ExchangeInfo>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }



    /**
     * 兑换
     * @param password
     */
    private void confirmPwd(final String password) {
        //验证支付密码
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<ExchangeInfo>>> call;
        if(type==1){//货款
            call = userBiz.exchangeSurplus(token, password, exchangeEdit.getText().toString());
        }else{//积分
            call = userBiz.canCashExchange(token, password, exchangeEdit.getText().toString());
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<ExchangeInfo>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ExchangeInfo>>> arg0,
                                   Response<BaseResponse<List<ExchangeInfo>>> response) {
                if (dialog.isShowing()){dialog.dismiss();}
                super.onResponse(arg0, response);
                BaseResponse<List<ExchangeInfo>> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    String msg=baseResponse.getMsg();
                    if (retCode == MyConstant.SUCCESS) {//兑换成功
                        ExchangeInfo exchangeInfo =baseResponse.getData().get(0);
                        String can_cash = exchangeInfo.getCan_cash();;//可兑换鼓励积分
                        String total = CompuUtils.add(can_cash, freeze).toString();
                        canCash.setText(total);
                        tv_able_money.setText(can_cash);
                        //提现成功
                        exchangeEdit.setText("");
                        ToastUtil.showToast(mContext,msg);
                        EventBus.getDefault().post(new MsgEvent5());//个人页面刷新数据
                    }else if(retCode == MyConstant.PAY_PWD_ERROE){//密码错误
                        DialogConfirm alert = new DialogConfirm();
                        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
                            @Override
                            public void onClick(boolean isOkClicked) {
                                if (isOkClicked) {
                                } else {
                                    startActivity(SetPayPwdActivity.class);
                                }
                            }
                        });
                        alert.showDialog(ExchangeActivity.this, "支付密码错误,请重试", "确定", "忘记密码");
                    } else {
                        ToastUtil.showToast(mContext,msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ExchangeInfo>>> arg0,
                                  Throwable arg1) {
                if (dialog.isShowing()){dialog.dismiss();}
            }
        });

    }


    //支付密码设置成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent2 msgEvent2) {
        SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
    }

    //实名认证提交成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent3 msgEvent3) {
        getExchangeInfo(1);
    }


    //银行卡更换成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent8 msgEvent8) {
        getExchangeInfo(1);
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            getExchangeInfo(1);
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消事件注册
        EventBus.getDefault().unregister(this);
    }
}
