package com.zph.commerce.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.activity.ActivateFriendsActivity;
import com.zph.commerce.activity.BindBankActivity;
import com.zph.commerce.activity.CertificationBindUserActivity;
import com.zph.commerce.activity.ExchangeActivity;
import com.zph.commerce.activity.LoginActivity;
import com.zph.commerce.activity.MyMessageActivity;
import com.zph.commerce.activity.MyRecommendActivity;
import com.zph.commerce.activity.OrderCenterActivity;
import com.zph.commerce.activity.PlatformInventoryActivity;
import com.zph.commerce.activity.PraiseListActivity;
import com.zph.commerce.activity.SetPayPwdActivity;
import com.zph.commerce.activity.SettingActivity;
import com.zph.commerce.activity.UpdateNickActivity;
import com.zph.commerce.activity.WisdomIntegralActivity;
import com.zph.commerce.aliutil.PutObjectSamples;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.AuthError;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.PersonAuth;
import com.zph.commerce.bean.UserBean;
import com.zph.commerce.common.CreateFile;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.constant.MyOSSConfig;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.DialogSelPhoto;
import com.zph.commerce.dialog.DialogSelectExchange;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent2;
import com.zph.commerce.eventbus.MsgEvent5;
import com.zph.commerce.eventbus.ToGeRenMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.PermissionListener;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.ImgSetUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.utils.luban.Luban;
import com.zph.commerce.utils.luban.OnCompressListener;
import com.zph.commerce.view.pullableview.PullableRefreshScrollView;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.widget.GlideCircleTransform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@ContentView(R.layout.main_fragment_wode)
public class GeRenFragment extends Fragment implements PullToRefreshLayout.OnRefreshListener, PermissionListener, ActivityCompat.OnRequestPermissionsResultCallback {


    /**
     * 头像
     */
    @ViewInject(R.id.img_login)
    private ImageView img_login;

    @ViewInject(R.id.ll_login)
    private LinearLayout ll_login;

    /**
     * 未登录
     */
    @ViewInject(R.id.tv_login)
    private TextView tv_login;
    /**
     * 手机号
     */
    @ViewInject(R.id.tv_phone)
    private TextView tv_phone;
    /**
     * 会员等级
     */
    @ViewInject(R.id.tv_dengji)
    private TextView tv_dengji;

    /**
     * 累计销量
     */
    @ViewInject(R.id.tv_sails)
    private TextView tv_sails;

    /**
     * 智品积分
     */
    @ViewInject(R.id.tv_zp_point)
    private TextView tv_zp_point;
    /**
     * 鼓励积分
     */
    @ViewInject(R.id.tv_praise_point)
    private TextView tv_praise_point;

    /**
     * 货款
     */
    @ViewInject(R.id.tv_huohuan)
    private TextView tv_huohuan;

    /**
     * 兑换
     */
    @ViewInject(R.id.tv_verify_state)
    private TextView tv_verify_state;


    /**
     * 合伙人区域
     */
    @ViewInject(R.id.tv_area)
    private TextView tv_area;


    /**
     * 兑换
     */
    @ViewInject(R.id.wisdom_integral)
    private LinearLayout wisdom_integral;

    /**
     * 兑换
     */
    @ViewInject(R.id.commodity_integral)
    private LinearLayout commodity_integral;

    @ViewInject(R.id.category_refresh_view)
    private PullToRefreshLayout category_refresh_view;

    @ViewInject(R.id.scroll_view)
    private PullableRefreshScrollView scroll_view;

    private View mRootView;
    private int state;
    private OSS oss;
    private String localTempImageFileName;
    private int type = 0;
    private String token;
    /**
     * 上下文
     **/
    private Context mContext;
    private DialogSelPhoto dialogSelPhoto;

    public GeRenFragment() {
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
        // 注册事件
        EventBus.getDefault().register(this);
        initDialog();
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
        category_refresh_view.setOnRefreshListener(this);
        dialogSelPhoto = new DialogSelPhoto();
        oss = new OSSClient(mContext, MyConstant.ALI_ENDPOINT, MyOSSConfig.getProvider(), MyOSSConfig.getOSSConfig());

        setWidget(1);
        dialogSelPhoto.setListener(new DialogSelPhoto.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isCameraSel, int imgIndex) {
                if (isCameraSel) {
                    type = 1;
                    //拍照
                    takePhotoForCamera(imgIndex);
                } else {
                    //相册
                    type = 2;
                    takePhotoForAlbum(imgIndex);
                }
            }
        });
        return mRootView;
    }

    private void setWidget(int state) {
        if (MyConstant.HASLOGIN) {
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            ll_login.setVisibility(View.VISIBLE);
            tv_login.setVisibility(View.GONE);
            getUserinfo(state);
        } else {
            if (state == 2) {
                category_refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
            }
            ll_login.setVisibility(View.GONE);
            tv_login.setVisibility(View.VISIBLE);
            tv_zp_point.setText("0");
            tv_praise_point.setText("0");
            tv_huohuan.setText("0");
            Glide.with(mContext).load(R.drawable.img_default_head).override(80, 80).transform(new GlideCircleTransform(mContext)).into(img_login);
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        // 下拉刷新操作
        setWidget(2);

    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        // 加载操作
        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }


    @Event({R.id.img_login, R.id.tv_login, R.id.tv_change, R.id.tv_verify,
            R.id.tv_oder, R.id.tv_kucun, R.id.tv_activate,
            R.id.tv_news, R.id.tv_shezhi, R.id.tv_recommend, R.id.wisdom_integral, R.id.commodity_integral, R.id.ll_huokuan, R.id.tv_phone})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.wisdom_integral://智品积分
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, WisdomIntegralActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);

                }
                startActivity(intent);
                break;
            case R.id.commodity_integral://鼓励积分:查看鼓励积分奖励记录（进的记录）
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, PraiseListActivity.class);
                    intent.putExtra("state", 2);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);

                }
                startActivity(intent);
//                if (MyConstant.HASLOGIN) {
//                    int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
//                    if (payState == MyConstant.SUCCESS) {//已设置支付密码
//                        int status = (int) SPUtils.get(mContext, "verified" + token, 0);
//                        if (status == 3) {//已认证
//                            intent = new Intent(mContext, ExchangeActivity.class);
//                            intent.putExtra("state", 2);
//                            startActivity(intent);
//                        } else {//查询认证状态
//                            getPersonAuth(4);
//                        }
//                    } else {//未设置支付密码
//                        showdDialog(1, getResources().getString(R.string.tv_40), null);
//                    }
//                } else {
//                    intent = new Intent(mContext, LoginActivity.class);
//                    startActivity(intent);
//                }
                break;
            case R.id.ll_huokuan://货款：  查看货款奖励记录（进的记录）
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, PraiseListActivity.class);
                    intent.putExtra("state", 1);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);

                }
                startActivity(intent);
//                if (MyConstant.HASLOGIN) {
//                    int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
//                    if (payState == MyConstant.SUCCESS) {//已设置支付密码
//                        int status = (int) SPUtils.get(mContext, "verified" + token, 0);
//                        if (status == 3) {//已认证
//                            intent = new Intent(mContext, ExchangeActivity.class);
//                            intent.putExtra("state", 1);
//                            startActivity(intent);
//                        } else {//查询认证状态
//                            getPersonAuth(3);
//                        }
//                    } else {//未设置支付密码
//                        showdDialog(1, getResources().getString(R.string.tv_40), null);
//                    }
//                } else {
//                    intent = new Intent(mContext, LoginActivity.class);
//                    startActivity(intent);
//                }
                break;
            case R.id.img_login://个人页面
                if (MyConstant.HASLOGIN) {
                    dialogSelPhoto.showDialog(getActivity(), 1);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tv_login://登录
                intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_change://兑换  鼓励积分、货款兑换记录（出的记录）
                if (MyConstant.HASLOGIN) {
                    int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
                    if (payState == MyConstant.SUCCESS) {//已设置支付密码
                        int status = (int) SPUtils.get(mContext, "verified" + token, 0);
                        if (status == 3) {//已认证
                            showdSelectExchangeDialog();
                        } else {//查询认证状态
                            getPersonAuth(3);
                        }
                    } else {//未设置支付密码
                        showdDialog(1, getResources().getString(R.string.tv_40), null);
                    }
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tv_verify://实名认证
                if (MyConstant.HASLOGIN) {
                    int payState = (int) SPUtils.get(mContext, "payPwd" + token, 0);
                    if (payState == MyConstant.SUCCESS) {//已设置支付密码
                        int status = (int) SPUtils.get(mContext, "verified" + token, 0);
                        if (status == 3) {//已认证
                            ToastUtil.showToast(mContext, "实名认证已通过");
                        } else {//查询认证状态
                            getPersonAuth(2);
                        }
                    } else {//未设置支付密码
                        showdDialog(1, getResources().getString(R.string.tv_40), null);
                    }
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tv_oder://订单中心
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, OrderCenterActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_kucun://库存
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, PlatformInventoryActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_activate://激活好友
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, ActivateFriendsActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_news://我的消息
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, MyMessageActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_shezhi://设置
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, SettingActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_recommend://推荐好友
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, MyRecommendActivity.class);
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.tv_phone://修改昵称
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(mContext, UpdateNickActivity.class);
                    intent.putExtra("nick", tv_phone.getText().toString());
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                }
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消事件注册
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {//已登录
            MyConstant.HASLOGIN = true;
        } else {//未登录
            MyConstant.HASLOGIN = false;
        }
        setWidget(1);
    }

    //主页点击个人、智品订单页面提货确认收货发送过来
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(ToGeRenMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {//已登录
            MyConstant.HASLOGIN = true;
        } else {//未登录
            MyConstant.HASLOGIN = false;
        }
        setWidget(1);
    }


    //支付密码设置成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent2 msgEvent2) {
        SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
    }


    //兑换成功
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent5 msgEvent5) {
        getUserinfo(1);
    }


    /**
     * 查询认证状态、支付密码状态、用户信息
     */
    private void getUserinfo(final int state) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<UserBean>>> call = userBiz.getUserInformation(token);
        call.enqueue(new Callback<BaseResponse<List<UserBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<UserBean>>> arg0,
                                   Response<BaseResponse<List<UserBean>>> response) {
                BaseResponse<List<UserBean>> baseResponse = response.body();
                if (state == 2) {
                    category_refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                if (baseResponse != null) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<UserBean> data = baseResponse.getData();
                        UserBean userBean = data.get(0);
                        String nick = userBean.getNick();
                        if (StringUtils.isBlank(nick)) {
                            tv_phone.setText(userBean.getMobile());
                        } else {
                            tv_phone.setText(nick);
                        }
                        tv_dengji.setText(userBean.getRank());
                        tv_zp_point.setText(userBean.getNocan_cash());
                        tv_praise_point.setText(userBean.getCan_cash());
                        tv_huohuan.setText(userBean.getSurplus());
                        tv_verify_state.setText(userBean.getRemarks());
                        tv_sails.setText(userBean.getSales_volume());
                        String agent = userBean.getAgent();
                        if (!StringUtils.isBlank(agent)) {
                            tv_area.setText(agent);
                            tv_area.setVisibility(View.VISIBLE);
                        } else {
                            tv_area.setVisibility(View.GONE);
                        }
                        int type = userBean.getDerail();
                        String url;
                        if (type == 1) {
                            url = userBean.getHeadurl() + "/" + userBean.getHead();
                        } else {
                            url = userBean.getHeadurl() + userBean.getHead();
                        }
                        Glide.with(mContext).load(url).override(80, 80).transform(new GlideCircleTransform(mContext))
                                .placeholder(R.drawable.pic_nomal_loading_style)
                                .error(R.drawable.img_default_head)
                                .into(img_login);
                        int verified = userBean.getVerified();
                        if (verified == 3) {//认证通过
                            SPUtils.put(mContext, "verified" + token, verified);
                        }
                        String payState = userBean.getPwd();
                        if (!StringUtils.isBlank(payState)) {//已设置支付密码
                            SPUtils.put(mContext, "payPwd" + token, MyConstant.SUCCESS);
                        }
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                } else {
                    try {
                        int code = response.code();
                        String error = response.errorBody().string();
                        BaseResponse errorResponse = GsonUtil.GsonToBean(error, BaseResponse.class);
                        if (errorResponse.getCode() == (MyConstant.T_ERR_AUTH) || errorResponse.getCode() == MyConstant.T_LOGIN_ERR) {//token过期
                            MyConstant.HASLOGIN = false;
                            SPUtils.remove(mContext, "login");
                            SPUtils.remove(mContext, "token");
                            setWidget(1);
                        } else {
                            MyApplication.getInstance().showShortToast("服务器连接失败");
                        }
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<UserBean>>> arg0, Throwable arg1) {
                ToastUtil.showToast(mContext, "服务器连接失败");
                if (state == 2) {
                    category_refresh_view.refreshFinish(PullToRefreshLayout.FAIL);
                }
            }
        });
    }


    /**
     * @param state 2:实名认证点击；3兑换点击
     */
    private void getPersonAuth(final int state) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<PersonAuth>>> call = userBiz.getPersonAuth(token);
        call.enqueue(new Callback<BaseResponse<List<PersonAuth>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<PersonAuth>>> arg0,
                                   Response<BaseResponse<List<PersonAuth>>> response) {

                BaseResponse<List<PersonAuth>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        List<PersonAuth> data = baseResponse.getData();
                        PersonAuth personAuth = data.get(0);
                        int status = personAuth.getStatus();
                        if (status == 0) {//未提交审核
                            showdDialog(2, getResources().getString(R.string.tv_41), null);
                        } else if (status == 1) {//1认证失败
                            showdDialog(3, getResources().getString(R.string.tishi_12), personAuth);
                        } else if (status == 2) {//2审核中
                            ToastUtil.showToast(mContext, "请等待实名认证审核通过后即可操作");
                        } else if (status == 3) {//3认证通过
                            SPUtils.put(mContext, "verified" + token, status);
                            if (state == 2) {//实名认证点击
                                ToastUtil.showToast(mContext, "您已通过实名认证");
                            }
                            if (state == 3) {//兑换点击
                                showdSelectExchangeDialog();
                            }
                        }
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<PersonAuth>>> arg0, Throwable arg1) {

                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    /**
     * 1:未设置支付密码
     * 2：未提交实名认证
     * 3：实名认证未通过
     */
    private void showdDialog(final int state, String title, final PersonAuth personAuth) {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//进入设置支付密码页面
                    Intent intent;
                    if (state == 1) {
                        intent = new Intent(mContext, SetPayPwdActivity.class);
                    } else {
                        if (state == 3) {
                            AuthError authError = personAuth.getRemarks();
                            String name = authError.getName();
                            String card_number = authError.getCard_number();
                            String hand_logo = authError.getHand_logo();
                            String front_card = authError.getFront_card();
                            String rear_card = authError.getRear_card();
                            String bank_card = authError.getBank_card();
                            String bank_logo = authError.getBank_logo();
                            if (StringUtils.isBlank(name) || StringUtils.isBlank(card_number) || StringUtils.isBlank(hand_logo) || StringUtils.isBlank(front_card) || StringUtils.isBlank(rear_card)) {//身份信息有误
                                intent = new Intent(mContext, CertificationBindUserActivity.class);
                                intent.putExtra("personAuth", personAuth);
                                if (StringUtils.isBlank(bank_card) || StringUtils.isBlank(bank_logo)) {//银行卡信息有误
                                    intent.putExtra("state", 3);
                                } else {//银行卡信息无误
                                    intent.putExtra("state", 2);
                                }
                            } else {//身份信息无误,银行卡信息有误
                                intent = new Intent(mContext, BindBankActivity.class);
                                intent.putExtra("state", 3);
                                intent.putExtra("personAuth", personAuth);
                            }

                        } else {//未提交实名认证
                            intent = new Intent(mContext, CertificationBindUserActivity.class);
                            intent.putExtra("state", 1);
                        }

                    }
                    startActivity(intent);
                }

            }
        });
        alert.showDialog(getActivity(), title, "确定", "取消");
    }

    /**
     * 选择兑换内容
     */
    private void showdSelectExchangeDialog() {
        DialogSelectExchange alert = new DialogSelectExchange();
        alert.setListener(new DialogSelectExchange.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                Intent intent = new Intent(mContext, ExchangeActivity.class);
                if (isOkClicked) {//货款兑换
                    intent.putExtra("state", 1);
                } else {//鼓励积分兑换
                    intent.putExtra("state", 2);

                }
                startActivity(intent);
            }
        });
        alert.showDialog(getActivity());
    }

    /**
     * 相册
     *
     * @param value
     */
    private void takePhotoForAlbum(int value) {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, this, value);
    }

    /**
     * 拍照
     *
     * @param value
     */
    private void takePhotoForCamera(int value) {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, this, value);
    }

    // andrpoid 6.0 及以上需要写运行时权限
    public void requestRuntimePermission(String[] permissions, PermissionListener listener, int type) {

        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), permissionList.toArray(new String[permissionList.size()]), type);
        } else {
            onGranted(type);
        }
    }

    //获得权限
    @Override
    public void onGranted(int value) {
        if (type == 1) {//拍照
            captureImage(CreateFile.FEEDBACK_PATH, value);
        } else if (type == 2) {//相册
            selectImage(value);
        }
    }


    @Override
    public void onDenied(List<String> deniedPermission) {


    }

    /**
     * 拍照
     *
     * @param path 照片存放的路径
     */
    public void captureImage(String path, int value) {
        Uri data;
        localTempImageFileName = String.valueOf((new Date()).getTime()) + ".jpg";//拍照后的图片路径
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(CreateFile.FEEDBACK_PATH, localTempImageFileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "net.csdn.blog.ruancoder.fileprovider"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(getActivity(), "com.zph.commerce.fileprovider", f);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(f);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, data);
        startActivityForResult(intent, value);
    }

    /**
     * 从图库中选取图片
     */
    public void selectImage(int value) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, value);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
            switch (type) {
                case 1: {
                    handleCameraRet(data, requestCode);
                    break;
                }
                case 2: {
                    handleAlbumRet(data, requestCode);
                    break;
                }

                default:
                    break;
            }
        }
    }

    private void handleCameraRet(Intent data, int value) {
        // 将保存在本地的图片取出并缩小后显示在界面上
        String imgKey = ImgSetUtil.getImgKeyString();
        String path = CreateFile.FEEDBACK_PATH + localTempImageFileName;
        compressWithLs(new File(path), value, imgKey);
    }


    private void handleAlbumRet(Intent data, int value) {
        String imgKey = ImgSetUtil.getImgKeyString();
        ContentResolver resolver = getActivity().getContentResolver();
        // 照片的原始资源地址
        Uri originalUri = data.getData();
        String path = getAblumPicPath(data, getActivity());
        compressWithLs(new File(path), value, imgKey);

    }


    /**
     * @param data
     * @param ac
     * @return
     * @方法说明:获得相册图片的路径
     * @方法名称:getAblumPicPath
     * @返回 String
     */
    public static String getAblumPicPath(Intent data, Activity ac) {
        Uri originalUri = data.getData();
        String path = "";
        String[] proj = {MediaStore.Images.Media.DATA};
        if (originalUri != null && proj != null) {
            Cursor cursor = ac.getContentResolver().query(originalUri, null, null, null, null);
            if (cursor == null) {
                path = originalUri.getPath();
                if (!StringUtils.isEmpty(path)) {
                    String type = ".jpg";
                    String type1 = ".png";
                    if (path.endsWith(type) || path.endsWith(type1)) {
                        return path;
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } else {
                /**将光标移至开，这个很重要，不小心很容易引起越**/
                cursor.moveToFirst();
                /**按我个人理解 这个是获得用户选择的图片的索引**/
                int column_index = cursor.getColumnIndex(proj[0]);
                /** 最后根据索引值获取图片路**/
                path = cursor.getString(column_index);
                cursor.close();
            }
        }
        return path;
    }


    /**
     * 压缩单张图片 Listener 方式
     */
    private void compressWithLs(final File f, final int value, final String imgKey) {
        Luban.get(mContext)
                .load(f)
                .putGear(Luban.THIRD_GEAR)
                .setFilename(System.currentTimeMillis() + "")
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File f) {
                        dialog.show();
                        upLoadAli(imgKey, f.getAbsolutePath(), value);
                    }

                    @Override
                    public void onError(Throwable e) {//压缩失败
                        upLoadAli(imgKey, f.getAbsolutePath(), value);
                    }
                }).launch();
    }


    private void upLoadAli(final String key, final String path, final int value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = new PutObjectSamples(oss, MyConstant.ALI_PUBLIC_BUCKET_RECOMMEND, key, path).putObjectFromLocalFile();
                if (flag) {//上传成功
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            switch (value) {
                                case 1:
                                    updateHeadPic(key);
                                    Glide.with(mContext).load(path).fitCenter().override(80, 80).transform(new GlideCircleTransform(mContext))
                                            // .placeholder(R.drawable.load_fail).error(R.drawable.load_fail)
                                            .into(img_login);
                                    break;
                            }
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            ToastUtil.showToast(getActivity(), "上传失败,请重新上传");
                        }
                    });
                }
            }
        }).start();
    }

    private LoadingDialog dialog;

    protected void initDialog() {
        dialog = new LoadingDialog(getActivity(), R.style.dialog, "请稍候...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void updateHeadPic(String key) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.updateHeadPic(token, key);
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
                    if (retCode == MyConstant.SUCCESS) {//
                        ToastUtil.showToast(mContext, "头像修改成功");
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }
}
