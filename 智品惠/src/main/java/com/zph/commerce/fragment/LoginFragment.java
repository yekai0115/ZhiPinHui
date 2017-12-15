package com.zph.commerce.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.zph.commerce.R;
import com.zph.commerce.activity.ForgetPwdActivity;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.watcher.PasswordTextWatcher;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@ContentView(R.layout.fragment_login)
public class LoginFragment extends Fragment {


    /***/
    @ViewInject(R.id.img_phone)
    private ImageView img_phone;
    /***/
    @ViewInject(R.id.ed_phone)
    private EditText ed_phone;
    /***/
    @ViewInject(R.id.img_pwd)
    private ImageView img_pwd;
    /***/
    @ViewInject(R.id.ed_pwd)
    private EditText ed_pwd;
    @ViewInject(R.id.btn_login)
    private Button btn_login;


    private View mRootView;

    /**
     * 上下文
     **/
    private Context mContext;
    /**
     * 用户名
     */
    private String phone;
    /**
     * 密码
     */
    private String pwd;
    private LoadingDialog dialog;

    public LoginFragment() {
        super();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = x.view().inject(this, inflater, container);

        }
        ViewGroup mViewGroup = (ViewGroup) mRootView.getParent();
        if (mViewGroup != null) {
            mViewGroup.removeView(mRootView);
        }
        setWidget();
        return mRootView;
    }


    private void setWidget() {
        dialog = new LoadingDialog(mContext, R.style.dialog, "登录中...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        ed_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phone = ed_phone.getText().toString().trim();
                changeBg();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        ed_pwd.addTextChangedListener(new PasswordTextWatcher(ed_pwd) {

            @Override
            public void afterTextChanged(Editable s) {
                pwd = ed_pwd.getText().toString().trim();
                changeBg();
                super.afterTextChanged(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

                super.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                super.onTextChanged(s, start, before, count);
            }
        });
    }


    private void changeBg() {
        if (StringUtils.isBlank(phone) || phone.length() < 11 || StringUtils.isBlank(pwd) || pwd.length() < 6) {
            btn_login.setBackgroundResource(R.drawable.bg_login_defaultstyle);
        } else {
            btn_login.setBackgroundResource(R.drawable.bg_login_style);
        }
    }


    @Event({R.id.tv_forget_pwd, R.id.btn_login})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_forget_pwd://
                intent = new Intent(mContext, ForgetPwdActivity.class);
                intent.putExtra("title", "找回密码");
                startActivity(intent);
                break;
            case R.id.btn_login://登录
                checkParms();
                break;
        }
    }

    private void checkParms(){
        if (StringUtils.isBlank(phone) || phone.length() < 11) {
           ToastUtil.showToast(mContext,"请输入正确的手机号");
            return;
        }

        if (StringUtils.isBlank(pwd) || pwd.length() < 6) {
            ToastUtil.showToast(mContext,"密码位数不正确");
            return;
        }
        login();
    }





    private void login() {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<String>> call = userBiz.loginRepo(phone + ":" + pwd);
        call.enqueue(new Callback<BaseResponse<String>>() {

            @Override
            public void onResponse(Call<BaseResponse<String>> arg0,
                                   Response<BaseResponse<String>> response) {
                dialog.dismiss();
                BaseResponse<String> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        String token = baseResponse.getData();
                        String encodeToken = EncodeUtils.base64Encode2String(token.getBytes());
                        SPUtils.put(mContext, "token", encodeToken);
                        SPUtils.put(mContext, "phone", phone);
                        MyConstant.HASLOGIN = true;
                        SPUtils.put(mContext, "login", MyConstant.SUC_RESULT);
                        if(null!=getActivity()){
                            getActivity().finish();
                        }
                    } else {
                        ToastUtil.showToast(mContext, desc);

                    }
                } else {
                    String error = response.errorBody().toString();
                    error = response.raw().toString();
                    int code = response.code();
                    error = response.toString();
                    ToastUtil.showToast(mContext, "服务器连接失败,请稍后再试");
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<String>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }


}
