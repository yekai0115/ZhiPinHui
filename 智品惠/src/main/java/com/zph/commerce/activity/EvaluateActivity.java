package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.MsgEvent16;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by 24448 on 2017/10/20.
 */

public class EvaluateActivity extends BaseActivity {


    @ViewInject(R.id.tv_good)
    private TextView tv_good;

    @ViewInject(R.id.tv_medium)
    private TextView tv_medium;

    @ViewInject(R.id.tv_negative)
    private TextView tv_negative;
    @ViewInject(R.id.ed_evaluate)
    private EditText ed_evaluate;

    private String token;
    private Context mContext;
    private String good_id;
    private String attr_id;
    private String order_sn;
    private int level = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate);
        x.view().inject(this);
        mContext = this;
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        good_id = getIntent().getStringExtra("good_id");
        attr_id = getIntent().getStringExtra("attr_id");
        order_sn = getIntent().getStringExtra("order_sn");
        initViews();
        initEvents();
        initDialog();
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
        tv_good.setSelected(true);
        tv_medium.setSelected(false);
        tv_negative.setSelected(false);
    }

    @Override
    protected void initEvents() {

    }

    @Event({R.id.btn_evaluate, R.id.tv_good, R.id.tv_medium, R.id.tv_negative})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.btn_evaluate://评价
                submitComment();
                break;
            case R.id.tv_good://好评
                level = 1;
                tv_good.setSelected(true);
                tv_medium.setSelected(false);
                tv_negative.setSelected(false);
                break;
            case R.id.tv_medium://中评
                level = 2;
                tv_medium.setSelected(true);
                tv_good.setSelected(false);
                tv_negative.setSelected(false);
                break;
            case R.id.tv_negative://差评
                level = 3;
                tv_negative.setSelected(true);
                tv_medium.setSelected(false);
                tv_good.setSelected(false);
                break;
            default:
                break;
        }
    }


    private void submitComment() {
        dialog.show();
        String content = ed_evaluate.getText().toString();
        if (StringUtils.isBlank(content)) {
            content = "";
        }
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.submitComment(token, good_id, attr_id, order_sn, content, level);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {

                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        EventBus.getDefault().post(new MsgEvent16());
                        ToastUtil.showToast(getApplicationContext(), "评价成功");
                        finish();
                    } else {
                        ToastUtil.showToast(getApplicationContext(), desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(getApplicationContext(), "网络状态不佳,请检查您的网络设置");
            }
        });
    }


}
