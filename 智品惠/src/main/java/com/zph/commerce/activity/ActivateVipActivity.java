package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HandyTextView;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by 24448 on 2017/10/20.
 */

public class ActivateVipActivity extends BaseActivity{


    @ViewInject(R.id.activate_num)
    private HandyTextView activateNum;

    @ViewInject(R.id.edit_number)
    private EditText editNumber;

    @ViewInject(R.id.btn_activate)
    private Button btnActivate;

    private String token;

    private Context mContext;

    private View anChorView;
    private String activateNumShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_vip);
        x.view().inject(this);
        mContext = this;
        token = (String) SPUtils.get(this, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        if (getIntent().getBundleExtra("activateNum") != null){
            activateNumShow = getIntent().getBundleExtra("activateNum").getString("activateNum");
            activateNum.setText(activateNumShow);
        }
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
    }

    @Override
    protected void initEvents() {

    }
    @Event({R.id.btn_activate})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_activate://激活提交
                    sumbitActiviteFriend();
                break;
            default:
                break;
        }
    }
    private void sumbitActiviteFriend() {
        if(StringUtils.isBlank(editNumber.getText().toString())){
            showShortToast("请填写好友的手机号码");
            return;
        }
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<String>> call = userBiz.activateFriend(token,editNumber.getText().toString());
        call.enqueue(new HttpCallBack<BaseResponse<String>>() {

            @Override
            public void onResponse(Call<BaseResponse<String>> arg0,
                                   Response<BaseResponse<String>> response) {
                if (dialog.isShowing()){dialog.dismiss();}
                super.onResponse(arg0,response);
                BaseResponse<String> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        String data = baseResponse.getData();
                        showShortToast("激活成功");
                    }
                }else {
                    ToastUtil.showReLogin(mContext);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }
}
