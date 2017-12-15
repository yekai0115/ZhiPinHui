package com.zph.commerce.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.adapter.BankAdapter;
import com.zph.commerce.aliutil.GetObjectSamples;
import com.zph.commerce.aliutil.PutObjectSamples;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.AreaBean;
import com.zph.commerce.bean.AuthError;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.BindCard;
import com.zph.commerce.bean.CityBean;
import com.zph.commerce.bean.PersonAuth;
import com.zph.commerce.bean.ProvinceBean;
import com.zph.commerce.common.CreateFile;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.constant.MyOSSConfig;
import com.zph.commerce.dialog.DialogSelPhoto;
import com.zph.commerce.dialog.DialogSelectAddr;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent15;
import com.zph.commerce.eventbus.MsgEvent3;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.PermissionListener;
import com.zph.commerce.model.Addrs;
import com.zph.commerce.model.BandCardBase;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.FileUtils;
import com.zph.commerce.utils.ImgSetUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.utils.luban.Luban;
import com.zph.commerce.utils.luban.OnCompressListener;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/6/24 0024.
 */

public class BindBankActivity extends BaseActivity implements View.OnClickListener, PermissionListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private Context mContext;
    private Button confirmButton;
    private String localTempImageFileName;
    private EditText bankNumber, subBankName;
    private TextView tvProvince, tvCity, tvArea, bankName, perName;
    private ImageView bankImg;
    private String imgKey;
    private List<ProvinceBean> proivinces = new ArrayList<>();
    private int proivinceSelect, citySelection, areaSelection;
    private ProvinceBean addrProvince = null;
    private CityBean addrCity = null;
    private AreaBean addrArea = null;
    /**
     * 银行卡数据
     */
    private List<BindCard> bankItems;
    private BandCardBase bandCode;
    private String bankCode;
    private DialogSelPhoto dialogSelPhoto;
    private OSS oss;
    /**
     * 手机号
     */
    private TextView tv_phone;
    private EditText ed_code;
    private TextView tv_yzm;

    private String frontImgKey;
    private String backImgKey;
    private String handImgKey;
    private String name;
    private String number;
    private String phone;
    private String token;

    private TextView tv_err1;
    private TextView tv_err2;
    private AuthError authError;
    private PersonAuth personAuth;
    /**
     * 1未提交实名认证
     * 2身份信息有误,银行卡信息无误
     * 3身份信息有误,银行卡信息有误
     */
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_bank);
        mContext = this;
        EventBus.getDefault().register(this);
        dialogSelPhoto = new DialogSelPhoto();
        oss = new OSSClient(getApplicationContext(), MyConstant.ALI_ENDPOINT, MyOSSConfig.getProvider(), MyOSSConfig.getOSSConfig());
        initViews();
        initDialog();
        initEvents();
        dialogSelPhoto.setListener(new DialogSelPhoto.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isCameraSel, int imgIndex) {
                if (isCameraSel) {
                    //拍照
                    takePhotoForCamera(imgIndex);
                } else {
                    //相册
                    takePhotoForAlbum(imgIndex);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initEvents() {
        getProivinceList(1);
        Intent intent = getIntent();
        state = intent.getIntExtra("state", 0);
        personAuth = (PersonAuth) getIntent().getSerializableExtra("personAuth");
        frontImgKey = personAuth.getFront_card();
        backImgKey = personAuth.getRear_card();
        handImgKey = personAuth.getHand_logo();
        name = personAuth.getName();
        number = personAuth.getCard_number();

//        frontImgKey = intent.getStringExtra("frontImgKey");
//        backImgKey = intent.getStringExtra("backImgKey");
//        handImgKey = intent.getStringExtra("handImgKey");
//        name = intent.getStringExtra("name");
//        number = intent.getStringExtra("number");
        if (state == 3) {
//            String bank_card = intent.getStringExtra("bank_card");//银行卡号
//            String  bank_id = intent.getStringExtra("bank_id");
//            String bank_name = intent.getStringExtra("bank_name");
//            String bank_logo = intent.getStringExtra("bank_logo");
//            String province = intent.getStringExtra("province");
//            String city = intent.getStringExtra("city");
//            String county = intent.getStringExtra("county");
//            String branch = intent.getStringExtra("branch");

            String bank_card = personAuth.getBank_card();//银行卡号
            String bank_id = personAuth.getBank_id() + "";
            String bank_name = personAuth.getBankname();
            String bank_logo = personAuth.getBank_logo();
            String province = personAuth.getProvince();
            String city = personAuth.getCity();
            String county = personAuth.getCounty();
            String branch = personAuth.getBranch();
            bankName.setText(bank_name);
            bankNumber.setText(bank_card);
            subBankName.setText(branch);
            imgKey = bank_logo;
            bankCode = bank_id;
            tvProvince.setText(province);
            tvCity.setText(city);
            tvArea.setText(county);
            authError = personAuth.getRemarks();
            String error_bank_card = authError.getBank_card();
            String error_bank_logo = authError.getBank_logo();
            if (!StringUtils.isBlank(error_bank_card)) {
                tv_err1.setText(error_bank_card);
            }
            if (!StringUtils.isBlank(error_bank_logo)) {
                tv_err2.setText(error_bank_logo);
            }
            if(!StringUtils.isBlank(imgKey)){
                downLoadPic();
            }


        }
        phone = (String) SPUtils.get(mContext, "phone", "");
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        perName.setText(name);
        tv_phone.setText(phone);
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

        confirmButton = (Button) findViewById(R.id.confirm_button);
        perName = (TextView) findViewById(R.id.abc_name);
        bankName = (TextView) findViewById(R.id.abc_bank_name);
        bankNumber = (EditText) findViewById(R.id.abc_bank_number);
        subBankName = (EditText) findViewById(R.id.abc_sub_bank_name);
        tvProvince = (TextView) findViewById(R.id.abc_select_province);
        tvCity = (TextView) findViewById(R.id.abc_select_city);
        tvArea = (TextView) findViewById(R.id.abc_select_area);
        bankImg = (ImageView) findViewById(R.id.abc_bank_img);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        ed_code = (EditText) findViewById(R.id.ed_code);
        tv_yzm = (TextView) findViewById(R.id.tv_yzm);

        tv_err1 = (TextView) findViewById(R.id.tv_err1);
        tv_err2 = (TextView) findViewById(R.id.tv_err2);

        confirmButton.setOnClickListener(this);
        tvProvince.setOnClickListener(this);
        tvCity.setOnClickListener(this);
        tvArea.setOnClickListener(this);
        bankName.setOnClickListener(this);
        bankImg.setOnClickListener(this);
        tv_yzm.setOnClickListener(this);
        bankNumber.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_button:
                if (state == 1) {
                    sumbitBindBankInfo();
                } else {
                    updateAuthentication();
                }
                break;
            case R.id.abc_select_province:
                if (null == proivinces || proivinces.isEmpty()) {
                    getProivinceList(2);
                } else {
                    showProvinceDialog();
                }

                break;
            case R.id.abc_select_city:
                showCityDialog();
                break;
            case R.id.abc_select_area:
                showAreaDialog();
                break;
            case R.id.abc_bank_name:
                if (null == bankItems || bankItems.isEmpty()) {//银行数据为空
                    getBankLst();
                } else {//银行数据已经取到
                    alertBankDialog(BindBankActivity.this, bankItems);
                }
                break;
            case R.id.abc_bank_img:
                tv_err2.setVisibility(View.INVISIBLE);
                dialogSelPhoto.showDialog(BindBankActivity.this, 0);
                break;
            case R.id.tv_yzm://获取验证码
                getCheckNum();
                break;
            case R.id.abc_bank_number://
                tv_err1.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 获取验证码
     */
    private void getCheckNum() {
        tv_yzm.setClickable(false);
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.getBindCode(token, phone);
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
                        // 60秒内不能重复发送两次
                        CountDownTimer countDownTimer = new CountDownTimer(60 * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                tv_yzm.setText(millisUntilFinished / 1000 + "s" + "可重发");

                            }

                            @Override
                            public void onFinish() {
                                tv_yzm.setText("验证码");
                                tv_yzm.setClickable(true);

                            }
                        }.start();
                    } else {
                        ToastUtil.showToast(mContext, desc);
                        tv_yzm.setText("验证码");
                        tv_yzm.setClickable(true);
                    }
                } else {
                    String error = response.errorBody().toString();
                    error = response.raw().toString();
                    int code = response.code();
                    error = response.toString();
                    tv_yzm.setText("验证码");
                    tv_yzm.setClickable(true);

                    response.raw().toString();
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0, Throwable arg1) {
                dialog.dismiss();
                tv_yzm.setClickable(true);
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    private void getProivinceList(final int state) {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<ProvinceBean>>> call = userBiz.getProivinceList();
        call.enqueue(new Callback<BaseResponse<List<ProvinceBean>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ProvinceBean>>> arg0,
                                   Response<BaseResponse<List<ProvinceBean>>> response) {
                BaseResponse<List<ProvinceBean>> baseResponse = response.body();
                if (null != baseResponse && baseResponse.getCode() == MyConstant.SUCCESS) {
                    int code = baseResponse.getCode();
                    List<ProvinceBean> data = baseResponse.getData();
                    if (code == MyConstant.SUCCESS) {
                        proivinces.addAll(data);
                        Addrs.setProvinceObjects(data);
                        tvCity.setVisibility(View.VISIBLE);
                        tvArea.setVisibility(View.VISIBLE);
                        if (state == 2) {
                            showProvinceDialog();
                        }
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ProvinceBean>>> arg0,
                                  Throwable arg1) {
            }
        });
    }

    private void getCityList() {
        tvCity.setClickable(false);
        tvArea.setClickable(false);
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<CityBean>>> call = userBiz.getCityList(addrProvince.getProvince_id());
        call.enqueue(new Callback<BaseResponse<List<CityBean>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CityBean>>> arg0,
                                   Response<BaseResponse<List<CityBean>>> response) {
                BaseResponse<List<CityBean>> baseResponse = response.body();
                if (null != baseResponse && baseResponse.getCode() == MyConstant.SUCCESS) {
                    int code = baseResponse.getCode();
                    List<CityBean> data = baseResponse.getData();
                    if (code == MyConstant.SUCCESS) {
                        if (null == data || data.isEmpty()) {
                            tvCity.setVisibility(View.GONE);
                            tvArea.setVisibility(View.GONE);
                        } else {
                            tvCity.setClickable(true);
                            tvArea.setClickable(false);
                            tvCity.setVisibility(View.VISIBLE);
                            tvArea.setVisibility(View.VISIBLE);
                            Addrs.setCityObjects(data);
                        }
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CityBean>>> arg0,
                                  Throwable arg1) {
            }
        });
    }

    private void getAreaList() {
        tvArea.setClickable(false);
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<AreaBean>>> call = userBiz.getAreaList(addrCity.getCity_id());
        call.enqueue(new Callback<BaseResponse<List<AreaBean>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<AreaBean>>> arg0,
                                   Response<BaseResponse<List<AreaBean>>> response) {
                BaseResponse<List<AreaBean>> baseResponse = response.body();
                if (null != baseResponse && baseResponse.getCode() == MyConstant.SUCCESS) {
                    int code = baseResponse.getCode();
                    List<AreaBean> data = baseResponse.getData();
                    if (code == MyConstant.SUCCESS) {
                        if (null == data || data.isEmpty()) {
                            tvArea.setVisibility(View.GONE);
                        } else {
                            tvArea.setClickable(true);
                            tvArea.setVisibility(View.VISIBLE);
                            Addrs.setAreaObjects(data);
                        }
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<AreaBean>>> arg0,
                                  Throwable arg1) {
            }
        });
    }

    private void getBankLst() {
        tvArea.setClickable(false);
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<BindCard>>> call = userBiz.getBankLst();
        call.enqueue(new Callback<BaseResponse<List<BindCard>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<BindCard>>> arg0,
                                   Response<BaseResponse<List<BindCard>>> response) {
                BaseResponse<List<BindCard>> baseResponse = response.body();
                if (null != baseResponse && baseResponse.getCode() == MyConstant.SUCCESS) {
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        bankItems = baseResponse.getData();
                        alertBankDialog(BindBankActivity.this, bankItems);
                    } else {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<BindCard>>> arg0,
                                  Throwable arg1) {
            }
        });
    }


    private void showProvinceDialog() {
        new DialogSelectAddr().showProvincesDialog(BindBankActivity.this, Addrs.getProvinceNameList(), proivinceSelect, new DialogSelectAddr.OnItemClickedListener() {
            @Override
            public void onClick(int dialogIndex, int selectedIndex) {
                proivinceSelect = selectedIndex;
                ProvinceBean addrObject = Addrs.getProvinceObjects().get(selectedIndex);
                addrProvince = addrObject;
                tvProvince.setText(addrProvince.getName());
                tvCity.setText(getResources().getString(R.string.abc_select_city));
                tvArea.setText(getResources().getString(R.string.abc_select_area));
                citySelection = 0;
                areaSelection = 0;
                addrCity = null;
                addrArea = null;
                getCityList();
            }

        }, new DialogSelectAddr.OnConfirmClickedListener() {
            @Override
            public void onConfirmClick(int dialogIndex, int selectedIndex) {
                proivinceSelect = selectedIndex;
                ProvinceBean addrObject = Addrs.getProvinceObjects().get(selectedIndex);
                addrProvince = addrObject;
                tvProvince.setText(addrProvince.getName());
                tvCity.setText(getResources().getString(R.string.abc_select_city));
                tvArea.setText(getResources().getString(R.string.abc_select_area));
                citySelection = 0;
                areaSelection = 0;
                addrCity = null;
                addrArea = null;
                getCityList();
            }
        }, new DialogSelectAddr.OnCancelClickedListener() {
            @Override
            public void OnCancelClick() {

            }
        });

    }


    private void showCityDialog() {
        if (addrProvince == null) {
            Toast.makeText(BindBankActivity.this, getResources().getString(R.string.common_sel_province), Toast.LENGTH_SHORT).show();
            return;
        }
        new DialogSelectAddr().showProvincesDialog(BindBankActivity.this, Addrs.getCityNameList(), citySelection, new DialogSelectAddr.OnItemClickedListener() {
            @Override
            public void onClick(int dialogIndex, int selectedIndex) {
                citySelection = selectedIndex;
                CityBean addrObject = Addrs.getCityObjects().get(selectedIndex);
                addrCity = addrObject;
                tvCity.setText(addrCity.getName());
                tvArea.setText(getResources().getString(R.string.abc_select_area));
                areaSelection = 0;
                addrArea = null;
                getAreaList();
            }

        }, new DialogSelectAddr.OnConfirmClickedListener() {
            @Override
            public void onConfirmClick(int dialogIndex, int selectedIndex) {
                citySelection = selectedIndex;
                CityBean addrObject = Addrs.getCityObjects().get(selectedIndex);
                addrCity = addrObject;
                tvCity.setText(addrCity.getName());
                tvArea.setText(getResources().getString(R.string.abc_select_area));
                areaSelection = 0;
                addrArea = null;
                getAreaList();
            }
        }, new DialogSelectAddr.OnCancelClickedListener() {
            @Override
            public void OnCancelClick() {

            }
        });

    }


    private void showAreaDialog() {
        if (addrCity == null) {
            Toast.makeText(BindBankActivity.this, getResources().getString(R.string.common_sel_city), Toast.LENGTH_SHORT).show();
            return;
        }
        new DialogSelectAddr().showProvincesDialog(BindBankActivity.this, Addrs.getAreaNameList(), areaSelection, new DialogSelectAddr.OnItemClickedListener() {
            @Override
            public void onClick(int dialogIndex, int selectedIndex) {
                areaSelection = selectedIndex;
                AreaBean addrObject = Addrs.getAreaObjects().get(selectedIndex);
                addrArea = addrObject;
                tvArea.setText(addrArea.getName());
            }

        }, new DialogSelectAddr.OnConfirmClickedListener() {
            @Override
            public void onConfirmClick(int dialogIndex, int selectedIndex) {
                areaSelection = selectedIndex;
                AreaBean addrObject = Addrs.getAreaObjects().get(selectedIndex);
                addrArea = addrObject;
                tvArea.setText(addrArea.getName());
            }
        }, new DialogSelectAddr.OnCancelClickedListener() {
            @Override
            public void OnCancelClick() {

            }
        });

    }


    private void sumbitBindBankInfo() {
        if (!checkParmer()) {
            return;
        }
        dialog.show();
        sumbitInfo();

    }

    /**
     * 提交认证
     */
    private void sumbitInfo() {
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<Object>> call = userBiz.bindBankSubmit(token,
                name,//用户姓名
                number,//身份证号
                handImgKey,//手持照片
                frontImgKey,//正面身份证
                backImgKey,//反面身份证
                bankCode,//银行id
                bankNumber.getText().toString(),//银行卡号
                tvProvince.getText().toString(),
                tvCity.getText().toString(),
                tvArea.getText().toString(),
                subBankName.getText().toString(),//支行
                imgKey,//银行卡照片
                ed_code.getText().toString());//短信验证码
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        showShortToast("认证信息提交成功,请等待审核");
                        defaultFinish();
                        MyApplication.finishSingleActivityByClass(CertificationBindUserActivity.class);
                        EventBus.getDefault().post(new MsgEvent3());
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }

                } else {
                    String error = response.errorBody().toString();
                    error = response.raw().toString();
                    int code = response.code();
                    error = response.toString();
                    tv_yzm.setText("验证码");
                    tv_yzm.setClickable(true);
                    String reRsponse = response.raw().toString();
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0,
                                  Throwable arg1) {
            }
        });
    }


    /**
     * 修改认证信息
     */
    private void updateAuthentication() {
        dialog.show();
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);//
        params.put("card_number", number);//
        params.put("hand_logo", handImgKey);//
        params.put("front_card", frontImgKey);//
        params.put("rear_card", backImgKey);//
        params.put("bank_id", bankCode);//
        params.put("bank_card", bankNumber.getText().toString());
        params.put("province", tvProvince.getText().toString());
        params.put("city", tvCity.getText().toString());
        params.put("county", tvArea.getText().toString());
        params.put("branch", subBankName.getText().toString());
        params.put("bank_logo", imgKey);
        params.put("code", ed_code.getText().toString());
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.updateAuthentication(token, params);
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
                        ToastUtil.showToast(mContext, "修改信息提交成功,请等待审核");
                        MyApplication.finishSingleActivityByClass(CertificationBindUserActivity.class);
                        finish();
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


    private boolean checkParmer() {
        if (StringUtils.isBlank(perName.getText().toString())) {
            showShortToast("持卡人不能为空");
            return false;
        }
        if (StringUtils.isBlank(bankNumber.getText().toString())) {
            showShortToast("银行卡号不能为空");
            return false;
        }
        if (StringUtils.isBlank(bankName.getText().toString())) {
            showShortToast("您还没有选择开卡银行");
            return false;
        }
        if (StringUtils.isBlank(tvProvince.getText().toString())) {
            showShortToast("您还没有选择省");
            return false;
        }
        if (StringUtils.isBlank(tvCity.getText().toString())) {
            showShortToast("您还没有选择市");
            return false;
        }
        if (StringUtils.isBlank(tvArea.getText().toString())) {
            showShortToast("您还没有选择区");
            return false;
        }
        if (StringUtils.isBlank(subBankName.getText().toString())) {
            showShortToast("您还没有输入开户支行");
            return false;
        }
        if (StringUtils.isBlank(imgKey)) {
            showShortToast("您还没有上传银行卡正面照");
            return false;
        }
        return true;
    }

    /**
     * @param ctx
     */
    private void alertBankDialog(final Context ctx, final List<BindCard> items) {
//        final Dialog lDialog = new Dialog(ctx, R.style.huodongstyle);
        final Dialog lDialog = new Dialog(ctx);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.dialog_provider1);
        final TextView title = (TextView) lDialog.findViewById(R.id.title_dialog);
        title.setText("选择银行卡");
        ListView listView = (ListView) lDialog.findViewById(R.id.lv_hangye);
        LinearLayout ll_close = (LinearLayout) lDialog.findViewById(R.id.ll_close);
        ll_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lDialog.dismiss();
            }
        });
        BankAdapter adapter = new BankAdapter(ctx, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                for (int i = 0; i < items.size(); i++) {
                    if (i == position) {
                        items.get(i).setChecked(true);
                    } else {
                        items.get(i).setChecked(false);
                    }
                }
                bankName.setText(items.get(position).getBankname());
                bankName.setTextColor(mContext.getResources().getColor(R.color.gray_33));
                bankCode = items.get(position).getId();
                lDialog.dismiss();
            }
        });
        lDialog.setCancelable(true);
        lDialog.setCanceledOnTouchOutside(true);
        lDialog.show();
    }

    private void takePhotoForAlbum(int value) {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, this, 2);
    }

    private void takePhotoForCamera(int value) {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestRuntimePermission(permissions, this, 1);
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
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), type);
        } else {
            onGranted(type);
        }
    }

    //获得权限
    @Override
    public void onGranted(int value) {
        if (value == 1) {
            captureImage(CreateFile.FEEDBACK_PATH, 1);
        } else if (value == 2) {
            selectImage(2);
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
        localTempImageFileName = String.valueOf((new Date()).getTime()) + ".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(CreateFile.FEEDBACK_PATH, localTempImageFileName);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            // "net.csdn.blog.ruancoder.fileprovider"即是在清单文件中配置的authorities
//            data = FileProvider.getUriForFile(this, "com.ddz.mearchant.fileprovider", f);
//            // 给目标应用一个临时授权
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        } else {
        data = Uri.fromFile(f);
//        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
            String fileName;
            switch (requestCode) {
                case 1: {
                    handleCameraRet(data, 0);
                    break;
                }
                case 2: {
                    handleAlbumRet(data, 0);
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
        compressWithLs(new File(path), imgKey);
    }


    private void handleAlbumRet(Intent data, int value) {
        String fileName;
        String imgKey = ImgSetUtil.getImgKeyString();
        ContentResolver resolver = getContentResolver();
        // 照片的原始资源地址
        Uri originalUri = data.getData();
        String path = getAblumPicPath(data, this);
        compressWithLs(new File(path), imgKey);
//        try {
//            Cursor cursor = resolver.query(originalUri, new String[]{MediaStore.Images.Media.DATA}, null,
//                    null, null);
//            // 使用ContentProvider通过URI获取原始图片
//            Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
//            if (photo != null) {
//                cursor.moveToFirst();
//                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                compressWithLs(new File(path), imgKey);
////                switch (value) {
////                    case 0:
////                        imgPath = path2;
////                        imgKey = imgKey1;
////                        frameImage1.setVisibility(View.GONE);
////                        frameImage2.setVisibility(View.GONE);
//////                        upHeadImages.setVisibility(View.VISIBLE);
////                        bankImg.setImageBitmap(newBitmap);
////                        break;
////                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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


    private void compressWithLs(final File f, final String imgKey) {
        Luban.get(this)
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
                        upLoadAli(imgKey, f.getAbsolutePath());
                        Glide.with(mContext).load(f).fitCenter()
                                // .placeholder(R.drawable.load_fail).error(R.drawable.load_fail)
                                .into(bankImg);
                    }

                    @Override
                    public void onError(Throwable e) {//压缩失败
                        upLoadAli(imgKey, f.getAbsolutePath());
                        Glide.with(mContext).load(f).fitCenter()
                                // .placeholder(R.drawable.load_fail).error(R.drawable.load_fail)
                                .into(bankImg);
                    }
                }).launch();
    }


    private void upLoadAli(final String key, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = new PutObjectSamples(oss, MyConstant.ALI_BUCKET_RECOMMEND, key, path).putObjectFromLocalFile();
                if (flag) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            imgKey = key;
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            showShortToast("上传失败,请重新上传");
                        }
                    });
                }
            }
        }).start();
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


    private void downLoadPic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File cacheFile = FileUtils.getCacheFile(new File(FileUtils.getDiskCacheDir(getApplicationContext())), imgKey);
                GetObjectSamples getObject = new GetObjectSamples(oss, MyConstant.ALI_BUCKET_RECOMMEND, imgKey);
                String url = getObject.getObject(cacheFile);
                EventBus.getDefault().post(new MsgEvent15(url));
            }
        }).start();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent15 messageEvent) {

        Glide.with(mContext).load(messageEvent.getUrl()).centerCrop()
                .override(187, 100)
                .placeholder(R.drawable.pic_nomal_loading_style).error(R.drawable.pic_nomal_loading_style)
                .into(bankImg);
    }

}