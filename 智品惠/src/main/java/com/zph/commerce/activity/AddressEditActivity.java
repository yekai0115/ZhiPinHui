package com.zph.commerce.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent13;
import com.zph.commerce.eventbus.MsgEvent6;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.KeyBoardUtils;
import com.zph.commerce.utils.PhoneUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.picker.CityPicker;
import com.zph.commerce.view.poptips.ToolTip;
import com.zph.commerce.view.poptips.ToolTipView;
import com.zph.commerce.view.slide.Slidr;
import com.zph.commerce.view.slide.SlidrConfig;
import com.zph.commerce.view.slide.SlidrPosition;
import com.zph.commerce.view.watcher.ChineseEditText;
import com.zph.commerce.widget.TopNvgBar5;
import com.zph.commerce.widget.swichbutton.ToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


/**
 * 描述 ：编辑收货人信息
 */
public class AddressEditActivity extends BaseActivity {


    /**
     * 收货人姓名
     */
    @ViewInject(R.id.ed_userName)
    private EditText ed_userName;
    /**
     * 手机号码
     */
    @ViewInject(R.id.ed_phone)
    private EditText ed_phone;
    /**
     * 选择省市区
     */
    @ViewInject(R.id.tv_chose_ssq)
    private TextView ed_destion;
    /**
     * 填写详细地址
     */
    @ViewInject(R.id.ed_detalDes)
    private EditText ed_detalDes;

    /**
     * 没填姓名提示
     */
    @ViewInject(R.id.ll_name_tishi)
    private LinearLayout ll_name_tishi;
    /**
     * 没填电话提示
     */
    @ViewInject(R.id.ll_phone_tishi)
    private LinearLayout ll_phone_tishi;
    /**
     * 没填地区提示
     */
    @ViewInject(R.id.ll_destion_tishi)
    private LinearLayout ll_destion_tishi;

    /**
     * 没填详细地址提示
     */
    @ViewInject(R.id.ll_dizhi_tishi)
    private LinearLayout ll_dizhi_tishi;


    /**
     * 删除名字图标
     */
    @ViewInject(R.id.img_name_close)
    private ImageView img_name_close;

    /**
     * 删除电话图标
     */
    @ViewInject(R.id.img_phone_close)
    private ImageView img_phone_close;

    /**
     * 删除详细地址图标
     */
    @ViewInject(R.id.img_detaldes_close)
    private ImageView img_detaldes_close;

    /**
     * 删除地址
     */
    @ViewInject(R.id.tv_delete)
    private TextView tv_delete;

    /**
     * 是否设为默认
     */
    @ViewInject(R.id.switch_moren)
    private ToggleButton switch_moren;


    private Intent intent;

    /**
     * 上下文
     **/
    private Context mContext;
    /**
     * 地址id：点击编辑传进来
     */
    private String fdeliveryAddrId;
    /**
     * 收货人名字
     */
    private String accept_name;
    /**
     * 收货人电话
     */
    private String phone;
    /**
     * 收货人详细地址
     */
    private String address;
    /**
     * 省名字
     */
    private String province = "";
    /**
     * 市名字
     */
    private String city = "";
    /**
     * 区名字
     */
    private String area = "";
    /**
     * 省市区名字
     */
    private String provinceCityArea;

    private String proId;
    private String cityId;
    private String areaId;
    private Address destination;

    private View anChorView;


    /**
     * 1：从地址管理页面的编辑；0：新增
     */
    private int type;
    /**
     * (fromType,type);
     * (1,0:个人中心页面-地址管理页面(点击新增)-进来                                                                      ：新增;
     * (1,1:个人中心页面-地址管理页面(列表项点击编辑)-进来                                                           ：编辑;
     * (2,0:确认订单页面-选择地址页面(点击地址管理)-地址管理页面(点击新增)-进来                  ：新增;
     * (2,1:确认订单页面-选择地址页面(点击地址管理)-地址管理页面(列表项点击编辑)-进来      ：编辑; 	 ：新增;
     */
    private int fromType;

    /**
     * 是否已经编辑了
     */
    private Boolean hasEdit = false;
    private String token;
    private LoadingDialog dialog;
    /**
     * 是否默认，1是默认，0不设置
     */
    private int addr_primary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_edit);
        x.view().inject(this);
        mContext = this;
        // 注册事件
        EventBus.getDefault().register(this);
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        intent = getIntent();
        //(fromType,type);
        //(1,0:个人中心页面-地址管理页面(点击新增)-进来                                                                      ：新增;
        //(1,1:个人中心页面-地址管理页面(列表项点击编辑)-进来                                                           ：编辑;
        //(2,0:确认订单页面-选择地址页面(点击地址管理)-地址管理页面(点击新增)-进来                  ：新增;
        //(2,1:确认订单页面-选择地址页面(点击地址管理)-地址管理页面(列表项点击编辑)-进来      ：编辑;
        fromType = intent.getIntExtra("fromType",1);
        type = intent.getIntExtra("type", 0);
        if (type == 1) {//编辑
            destination = intent.getParcelableExtra("place");
            fdeliveryAddrId = destination.getAddr_id();
            accept_name = destination.getAddr_name();
            phone = destination.getAddr_mobile();
            province = destination.getAddr_province_name();
            city = destination.getAddr_city_name();
            area = destination.getAddr_county_name();
            address = destination.getAddr_detail();
            proId = destination.getAddr_province();
            cityId = destination.getAddr_city();
            areaId = destination.getAddr_county();
            addr_primary = destination.getAddr_primary();
            ed_userName.setText(accept_name);
            ed_phone.setText(phone);
            ed_destion.setText(province + city + area);
            ed_detalDes.setText(address);
            ed_userName.setTextColor(mContext.getResources().getColor(R.color.tv_color2));
            ed_phone.setTextColor(mContext.getResources().getColor(R.color.tv_color2));
            ed_destion.setTextColor(mContext.getResources().getColor(R.color.tv_color2));
            ed_detalDes.setTextColor(mContext.getResources().getColor(R.color.tv_color2));

        }
        setWidget();
        setSlidr();
    }


    private void setSlidr() {
        int primary = getResources().getColor(R.color.toming);
        int secondary = getResources().getColor(R.color.accent);
        SlidrConfig config = new SlidrConfig.Builder().primaryColor(primary)
                .secondaryColor(secondary).position(SlidrPosition.LEFT)
                .touchSize(DimenUtils.dip2px(mContext, 60)).build();
        Slidr.attach(this, config);
    }


    private void setWidget() {
        dialog = new LoadingDialog(mContext, R.style.dialog, "加载中...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        switch_moren.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    addr_primary = 1;
                } else {
                    addr_primary = 0;
                }
            }
        });
        TopNvgBar5 topNvgBar = (TopNvgBar5) findViewById(R.id.top_nvg_bar);
        topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
            @Override
            public void onLeftClick() {
                if (hasEdit) {//已经编辑了：点击返回
                    showNotSaveDialog();
                } else {//未编辑,点击返回
                    finish();
                }
            }

            @Override
            public void onRightClick() {
                if (checkNoNull()) {
                    KeyBoardUtils.closeKeybord(ed_userName, mContext);
                    dialog.show();
                    if (type == 1) {//编辑保存
                        uddateAdd();
                    } else {//从地址列表新增地址进来
                        addAddress();
                    }

                }
            }
        });
        try {
            if (type == 1) {
                topNvgBar.setTitle("地址编辑");
                tv_delete.setVisibility(View.VISIBLE);
            } else {
                topNvgBar.setTitle("添加新地址");
                tv_delete.setVisibility(View.GONE);
            }
            ed_userName.requestFocus();
            //收货人姓名输入监听
            ed_userName.addTextChangedListener(new ChineseEditText(ed_userName) {

                @Override
                public void afterTextChanged(Editable s) {
                    accept_name = ed_userName.getText().toString().trim();
                    changeVisiable(accept_name, true, img_name_close);
                    ll_name_tishi.setVisibility(View.GONE);

                    if (null != anChorView) {
                        if (anChorView.getTag() != null) {
                            ((ToolTipView) anChorView.getTag()).remove();
                            anChorView.setTag(null);
                            return;
                        }
                    }

                    if (type == 1) {//代表编辑
                        if (!accept_name.equals(destination.getAddr_name())) {
                            hasEdit = true;
                        }
                    } else {//表示新增
                        if (!StringUtils.isBlank(accept_name)) {
                            hasEdit = true;
                        }
                    }
                    super.afterTextChanged(s);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {

                    super.beforeTextChanged(s, start, count, after);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {

                    super.onTextChanged(s, start, before, count);
                }
            });

            ed_userName.setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    accept_name = ed_userName.getText().toString().trim();
                    changeVisiable(accept_name, hasFocus, img_name_close);
                }
            });


            //收货人手机号输入监听
            ed_phone.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    phone = ed_phone.getText().toString().trim();
                    changeVisiable(phone, true, img_phone_close);

                    ll_phone_tishi.setVisibility(View.GONE);
                    if (null != anChorView) {


                        if (anChorView.getTag() != null) {
                            ((ToolTipView) anChorView.getTag()).remove();
                            anChorView.setTag(null);
                            return;
                        }
                    }

                    if (type == 1) {//代表编辑
                        if (!phone.equals(destination.getAddr_mobile())) {//不等于传进来的值代表修改了
                            hasEdit = true;
                        }
                    } else {
                        if (!StringUtils.isBlank(phone)) {
                            hasEdit = true;
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {


                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });

            ed_phone.setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    phone = ed_phone.getText().toString().trim();
                    changeVisiable(phone, hasFocus, img_phone_close);
                }
            });

            //收货人详细地址输入监听
            ed_detalDes.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    address = ed_detalDes.getText().toString().trim();
                    changeVisiable(address, true, img_detaldes_close);

                    ll_dizhi_tishi.setVisibility(View.GONE);
                    if (null != anChorView) {
                        if (anChorView.getTag() != null) {
                            ((ToolTipView) anChorView.getTag()).remove();
                            anChorView.setTag(null);
                            return;
                        }
                    }
                    if (type == 1) {//代表编辑
                        if (!address.equals(destination.getAddr_detail())) {
                            hasEdit = true;
                        }
                    } else {
                        if (!StringUtils.isBlank(address)) {
                            hasEdit = true;
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {


                }

                @Override
                public void afterTextChanged(Editable s) {


                }
            });


            ed_detalDes.setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    address = ed_detalDes.getText().toString().trim();
                    changeVisiable(address, hasFocus, img_detaldes_close);

                }
            });
        } catch (Exception e) {

        }
    }


    @Override
    protected void initEvents() {

    }

    @Override
    protected void initViews() {

    }

    @Event({R.id.tv_chose_ssq, R.id.ll_name_tishi,
            R.id.ll_phone_tishi, R.id.ll_dizhi_tishi, R.id.img_name_close, R.id.img_phone_close, R.id.img_detaldes_close, R.id.tv_delete})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.tv_chose_ssq://设置省市区
                KeyBoardUtils.closeKeybord(ed_userName, mContext);
                showDestinationDialog();
                break;
            case R.id.ll_name_tishi:
                showToolTipView(ll_name_tishi, Gravity.START, "请填写收货人名字!", mContext.getResources().getColor(R.color.bg_main_bottom));
                break;
            case R.id.ll_phone_tishi:
                //     showToolTipView(ll_phone_tishi, Gravity.START, "请输入收货人手机号码!", mContext.getResources().getColor(R.color.bg_main_bottom));
                ed_phone.setText("");
                break;
            case R.id.ll_dizhi_tishi:
                showToolTipView(ll_dizhi_tishi, Gravity.START, "请填写详细地址!", mContext.getResources().getColor(R.color.bg_main_bottom));
                break;
            case R.id.img_name_close:
                ed_userName.setText("");
                break;
            case R.id.img_phone_close:
                ed_phone.setText("");
                break;
            case R.id.img_detaldes_close:
                ed_detalDes.setText("");
                break;
            case R.id.tv_delete://删除收货地址
                showDeleteMessage("确认要删除吗");
                break;
            default:
                break;
        }
    }

    private boolean checkNoNull() {
        accept_name = ed_userName.getText().toString().trim();
        phone = ed_phone.getText().toString().trim();
        address = ed_detalDes.getText().toString().trim();
        provinceCityArea = ed_destion.getText().toString().trim();

        if (StringUtils.isBlank(accept_name)) {
            ll_name_tishi.setVisibility(View.VISIBLE);
            showToolTipView(ll_name_tishi, Gravity.START, "请填写收货人名字!", mContext.getResources().getColor(R.color.bg_main_bottom));
            return false;
        }


        if (StringUtils.isBlank(phone)) {
            ll_phone_tishi.setVisibility(View.VISIBLE);
            showToolTipView(ll_phone_tishi, Gravity.START, "请输入收货人手机号码!", mContext.getResources().getColor(R.color.bg_main_bottom));
            return false;
        }
        if (phone.length() < 11 || !PhoneUtils.isMobileNO(phone)) {
            ll_phone_tishi.setVisibility(View.VISIBLE);
            showToolTipView(ll_phone_tishi, Gravity.START, "手机号码格式不正确!", mContext.getResources().getColor(R.color.bg_main_bottom));
            return false;
        }

        if (StringUtils.isBlank(provinceCityArea)) {
            ll_destion_tishi.setVisibility(View.VISIBLE);
            showToolTipView(ll_destion_tishi, Gravity.START, "请选择收货人所在地区!", mContext.getResources().getColor(R.color.bg_main_bottom));
            return false;
        }
        if (StringUtils.isBlank(address)) {
            ll_dizhi_tishi.setVisibility(View.VISIBLE);
            showToolTipView(ll_dizhi_tishi, Gravity.START, "请填写详细地址!", mContext.getResources().getColor(R.color.bg_main_bottom));
            return false;
        }

        return true;
    }

    private void showToolTipView(View anchorView, int gravity, CharSequence text, int backgroundColor) {
        showToolTipView(anchorView, gravity, text, backgroundColor, 1L);
    }

    private void showToolTipView(final View anchorView, int gravity, CharSequence text, int backgroundColor, long delay) {
        showToolTipView(anchorView, null, gravity, text, backgroundColor, delay);
    }

    private void showToolTipView(final View anchorView, ViewGroup parentView, int gravity,
                                 CharSequence text, int backgroundColor, long delay) {
        if (anchorView.getTag() != null) {
            ((ToolTipView) anchorView.getTag()).remove();
            anchorView.setTag(null);
            return;
        }

        ToolTip toolTip = createToolTip(text, backgroundColor);
        ToolTipView toolTipView = createToolTipView(toolTip, anchorView, parentView, gravity);
        if (delay > 0L) {
            toolTipView.showDelayed(delay);
        } else {
            toolTipView.show();
        }
        anchorView.setTag(toolTipView);
        anChorView = anchorView;

        toolTipView.setOnToolTipClickedListener(new ToolTipView.OnToolTipClickedListener() {
            @Override
            public void onToolTipClicked(ToolTipView toolTipView) {
                anchorView.setTag(null);
            }
        });
    }

    private ToolTip createToolTip(CharSequence text, int backgroundColor) {
        Resources resources = getResources();
        int padding = resources.getDimensionPixelSize(R.dimen.padding);
        int textSize = resources.getDimensionPixelSize(R.dimen.text_size);
        int radius = resources.getDimensionPixelSize(R.dimen.radius);
        return new ToolTip.Builder()
                .withText(text)
                .withTextColor(Color.WHITE)
                .withTextSize(textSize)
                .withBackgroundColor(backgroundColor)
                .withPadding(padding, padding, padding, padding)
                .withCornerRadius(radius)
                .build();
    }

    private ToolTipView createToolTipView(ToolTip toolTip, View anchorView, ViewGroup parentView, int gravity) {
        return new ToolTipView.Builder(this)
                .withAnchor(anchorView)
                .withParent(parentView)
                .withToolTip(toolTip)
                .withGravity(gravity)
                .build();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监听返回键
            if (hasEdit) {//已经编辑了
                showNotSaveDialog();
            } else {//未编辑直接返回
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != destination) {
            destination = null;
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 省市区选择器
     *
     * @param
     */
    private void showDestinationDialog() {

        final Dialog lDialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.dizhi_alertdialog);
        // 从底部弹出
        Window dialogWindow = lDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        final CityPicker citypicker = (CityPicker) lDialog.findViewById(R.id.citypicker);
        //	citypicker.provincePicker.setDefault(2);

        proId = citypicker.getProId();
        cityId = citypicker.getCityId();
        areaId = citypicker.getAreaId();


        ((TextView) lDialog.findViewById(R.id.txt_close))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        lDialog.dismiss();

                    }
                });
        ((TextView) lDialog.findViewById(R.id.tv_ok))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        province = citypicker.getProvince();
                        city = citypicker.getCtiy();
                        area = citypicker.getArea();
                        provinceCityArea = province + city + area;

                        proId = citypicker.getProId();
                        cityId = citypicker.getCityId();
                        areaId = citypicker.getAreaId();
                        ed_destion.setText(province + "  " + city + "  " + area);
                        ed_destion.setTextColor(mContext.getResources().getColor(R.color.gray_33));
                        ll_destion_tishi.setVisibility(View.GONE);
                        if (null != anChorView) {
                            if (anChorView.getTag() != null) {
                                ((ToolTipView) anChorView.getTag()).remove();
                                anChorView.setTag(null);
                                return;
                            }
                        }
                        hasEdit = true;//点击了确定即代表已编辑
                        lDialog.dismiss();
                    }
                });
        lDialog.show();

    }

    private void changeVisiable(String content, boolean hasFocus, ImageView img_close) {
        if (hasFocus) {
            if (StringUtils.isBlank(content)) {
                img_close.setVisibility(View.GONE);
            } else {
                img_close.setVisibility(View.VISIBLE);
            }
        } else {
            img_close.setVisibility(View.GONE);
        }
    }


    /**
     * 未保存对话框
     */
    private void showNotSaveDialog() {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//
                    finish();
                }

            }
        });
        alert.showDialog(AddressEditActivity.this, getResources().getString(R.string.tv_53), "确定", "取消");
    }


    /**
     * 删除地址对话框
     *
     * @param string
     */
    private void showDeleteMessage(String string) {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//
                    dialog.show();
                    deleteAdd(fdeliveryAddrId);
                }

            }
        });
        alert.showDialog(AddressEditActivity.this, getResources().getString(R.string.tv_54), "确定", "取消");


    }


    /**
     * 修改收货地址
     */
    private void uddateAdd() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("addr_id", fdeliveryAddrId + "");//地址主键id
        params.put("addr_name", accept_name);//收件人姓名
        params.put("addr_province", proId);//省
        params.put("addr_city", cityId);//市
        params.put("addr_county", areaId);//区
        params.put("addr_detail", address);//详细地址
        params.put("addr_mobile", phone);//电话
        params.put("addr_primary", addr_primary + "");//是否默认

        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.updateAddrRepo(token, params);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        ToastUtil.showToast(mContext, "收货地址修改成功");
                        finish();
                        if(fromType==2){
                            MyApplication.finishSingleActivityByClass(AddressMangeActivity.class);
                            EventBus.getDefault().post(new MsgEvent13());//地址选择页面数据更新
                        }else{
                            EventBus.getDefault().post(new MsgEvent6());//地址管理页面数据更新
                        }
                    } else {
                        ToastUtil.showToast(mContext, baseResponse.getMsg());
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


    /**
     * 新增收货地址
     */
    private void addAddress() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("addr_name", accept_name);//收货人姓名
        params.put("addr_province", proId);//省
        params.put("addr_city", cityId);//市
        params.put("addr_county", areaId);//区id
        params.put("addr_detail", address);//详细地址
        params.put("addr_mobile", phone);//手机号
        params.put("addr_primary", addr_primary + "");//是否默认
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.addAddrRepo(token, params);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        ToastUtil.showToast(mContext, "地址添加成功");
                        finish();
                        if(fromType==2){
                            MyApplication.finishSingleActivityByClass(AddressMangeActivity.class);
                            EventBus.getDefault().post(new MsgEvent13());//地址选择页面数据更新
                        }else{
                            EventBus.getDefault().post(new MsgEvent6());//地址管理页面数据更新
                        }
                    } else {
                        	ToastUtil.showToast(mContext, baseResponse.getMsg());

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

    /**
     * 删除地址
     *
     * @param id
     */
    private void deleteAdd(String id) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.deliveryAdrDel(token, id);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        ToastUtil.showToast(mContext, "删除成功");
                        finish();
                        if(fromType==2){
                            MyApplication.finishSingleActivityByClass(AddressMangeActivity.class);
                            EventBus.getDefault().post(new MsgEvent13());//地址选择页面数据更新
                        }else{
                            EventBus.getDefault().post(new MsgEvent6());//地址管理页面数据更新
                        }
                    }else {
                        	ToastUtil.showToast(mContext, baseResponse.getMsg());

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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {

        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }
}
