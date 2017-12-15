package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by 24448 on 2017/10/20.
 */

public class SettingActivity extends BaseActivity {


    @ViewInject(R.id.tv_version)
    private TextView tvVersion;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shezhi);
        x.view().inject(this);
        mContext = this;
        initViews();
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
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("1.0.0");
            e.printStackTrace();
        }
    }

    @Override
    protected void initEvents() {

    }

    @Event({R.id.rl_set_pw, R.id.rl_set_pay_pw, R.id.rl_contact, R.id.ll_about, R.id.btn_tuichu,R.id.tv_myaddress})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.rl_set_pw://修改登陆密码
                intent = new Intent(mContext, ForgetPwdActivity.class);
                intent.putExtra("title", "修改密码");
                startActivity(intent);
                break;
            case R.id.rl_set_pay_pw://修改、设置支付密码
                intent = new Intent(mContext, SetPayPwdActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_myaddress://收货地址
                intent = new Intent(mContext, AddressMangeActivity.class);
                intent.putExtra("fromType", 1);
                startActivity(intent);
                break;
            case R.id.rl_contact://联系客服
                showDialDialog();
                break;
            case R.id.ll_about://关于
                intent = new Intent();
                intent.putExtra("site_url",MyConstant.COM_ABOUT);
                intent.putExtra("name","关于");
                intent.setClass(mContext, MyWebViewActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_tuichu://退出
                showExitDialog();
                break;
            default:
                break;
        }
    }

    private void showExitDialog() {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked){
                    MyConstant.HASLOGIN = false;
                    SPUtils.clear(mContext);
                    SPUtils.put(mContext, "isFirstRun", false);
                    finish();//关闭设置页面
                    startActivity(LoginActivity.class);
                }

            }
        });
        alert.showDialog(SettingActivity.this, getResources().getString(R.string.dialog_personal_exit), "退出", "取消");
    }

    private void showDialDialog() {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked)
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", MyConstant.COM_PHONE_NUM, null)));
            }
        });
        alert.showDialog(SettingActivity.this, getResources().getString(R.string.dialog_personal_phone), "呼叫", "取消");
    }
}
