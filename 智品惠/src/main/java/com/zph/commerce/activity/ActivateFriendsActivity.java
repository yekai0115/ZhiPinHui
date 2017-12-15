package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.UserBean;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
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

public class ActivateFriendsActivity extends BaseActivity{


    @ViewInject(R.id.activate_num)
    private HandyTextView activateNum;

    @ViewInject(R.id.btn_activate)
    private EditText btnActivate;
    private String token;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_friends);
        x.view().inject(this);
        mContext = this;
        token = (String) SPUtils.get(this, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        // 注册事件
        EventBus.getDefault().register(this);
        initViews();
        initDialog();
        initEvents();
    }

    @Override
    protected void initViews() {
        TopNvgBar5 topNvgBar = (TopNvgBar5) findViewById(R.id.top_nvg_bar);
        topNvgBar.setRightVisibility(View.VISIBLE);
        topNvgBar.setRight(getResources().getString(R.string.the_quota_of_subsidiary));
        topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }


            @Override
            public void onRightClick() {
                startActivity(AmountDetailActivity.class);
            }
        });
    }
    @Event({R.id.btn_activate})
    private void click(View view) {
        switch (view.getId()){
            case R.id.btn_activate:
                /*if (activateNum.getText().toString().equals("0")){
                    showShortToast("您的名额不足，无法帮助好友激活");
                    return;
                }*/
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("activateNum",activateNum.getText().toString());
                intent.putExtra("activateNum",bundle);
                intent.setClass(ActivateFriendsActivity.this,ActivateVipActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    @Override
    protected void initEvents() {
        gettActiviteNum();
    }
    private void gettActiviteNum() {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<UserBean>>> call = userBiz.getUserInformation(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<UserBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<UserBean>>> arg0,
                                   Response<BaseResponse<List<UserBean>>> response) {
                if (dialog.isShowing()){dialog.dismiss();}
                super.onResponse(arg0,response);
                BaseResponse<List<UserBean>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        List<UserBean> data = baseResponse.getData();
                        UserBean userBean=data.get(0);
                        activateNum.setText(userBean.getPlaces());
                    }
                }else {
                    ToastUtil.showReLogin(mContext);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<UserBean>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
//        int tage = messageEvent.getTage();
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            gettActiviteNum();
        }
    }
}
