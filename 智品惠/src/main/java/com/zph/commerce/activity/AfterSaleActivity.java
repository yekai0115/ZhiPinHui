package com.zph.commerce.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.bumptech.glide.Glide;
import com.zph.commerce.R;
import com.zph.commerce.adapter.CertificateAdapter;
import com.zph.commerce.aliutil.PutObjectSamples;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.AfterSaleGoodsInfo;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.common.CreateFile;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.constant.MyOSSConfig;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.DialogSelPhoto;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent17;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.PermissionListener;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.ImgSetUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.utils.luban.Luban;
import com.zph.commerce.utils.luban.OnCompressListener;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.widget.MyGridView;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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

/**
 * 售后页面
 */

public class AfterSaleActivity extends BaseActivity implements AdapterView.OnItemClickListener,PermissionListener, ActivityCompat.OnRequestPermissionsResultCallback {


    @ViewInject(R.id.iv_goods_pic)
    private ImageView iv_goods_pic;

    @ViewInject(R.id.tv_goodsTitle)
    private TextView tv_goodsTitle;

    @ViewInject(R.id.tv_guige)
    private TextView tv_guige;

    @ViewInject(R.id.ed_after_sale)
    private EditText ed_after_sale;

    @ViewInject(R.id.gv_pic)
    private MyGridView gv_pic;

    private ArrayList<String> paths = new ArrayList<>();

    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    private ArrayList<String> bitmapUrls = new ArrayList<>();
    private String localTempImageFileName;

    private String imagekey;
    private ArrayList<String> imagekeyList = new ArrayList<>();
    private Context mContext;
    private int type = 0;
    private String name;
    private String  pic;
    private String  value;
    private String order_sn;
    private String attr_id;
    private CertificateAdapter certificateAdapter;
    private DialogSelPhoto dialogSelPhoto;
    private OSS oss;
    private String token;
    /*
     *1普通商品售后
     * 2小哈售后
     */
    private int order_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_sale);
        x.view().inject(this);
        mContext = this;
        name=getIntent().getStringExtra("name");
        pic=getIntent().getStringExtra("pic");
        value=getIntent().getStringExtra("value");
        order_sn=getIntent().getStringExtra("order_sn");
        attr_id=getIntent().getStringExtra("attr_id");
        order_type=getIntent().getIntExtra("order_type",1);
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        initDialog();
        initViews();
        initEvents();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initViews() {
        tv_goodsTitle.setText(name);
        tv_guige.setText(value);
        Glide.with(mContext).load(pic)
                .fitCenter()
                .placeholder(R.drawable.pic_nomal_loading_style)
                .error(R.drawable.pic_nomal_loading_style)
                .into(iv_goods_pic);
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
        certificateAdapter = new CertificateAdapter(this,bitmapUrls,8);
        gv_pic.setAdapter(certificateAdapter);
        gv_pic.setOnItemClickListener(this);
        certificateAdapter.setOnDeleteImageListener(new CertificateAdapter.OnDeleteImageListener() {
            @Override
            public void click(int position) {
                bitmapUrls.remove(position);
                imagekeyList.remove(position);
                certificateAdapter.setList(bitmapUrls);
            }
        });
        dialogSelPhoto = new DialogSelPhoto();
        oss = new OSSClient(getApplicationContext(), MyConstant.ALI_ENDPOINT, MyOSSConfig.getProvider(), MyOSSConfig.getOSSConfig());
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
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (position == getDataSize()){

            dialogSelPhoto.showDialog(AfterSaleActivity.this,1);

        }
    }

    @Override
    protected void initEvents() {

    }

    @Event({R.id.btn_after_sale})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_after_sale://换货申请
          String reason= ed_after_sale.getText().toString();
            if(StringUtils.isBlank(reason)){
                ToastUtil.showToast(mContext,"请输入换货原因");
                return;
            }
            String pic="";
            if(null==imagekeyList||imagekeyList.isEmpty()){
                pic="";
            }else{
                StringBuilder sb = new StringBuilder();
                for(String key:imagekeyList){
                    sb.append(key).append(",");
                }
                pic= sb.deleteCharAt(sb.length() - 1).toString();
            }
                submitExchange(reason,pic);
                break;
            default:
                break;
        }
    }
    private int getDataSize(){

        return  bitmapUrls == null ? 0 : bitmapUrls.size();

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
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), type);
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
            data = FileProvider.getUriForFile(this, "com.zph.commerce.fileprovider", f);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        ContentResolver resolver = getContentResolver();
        // 照片的原始资源地址
        Uri originalUri = data.getData();
        String path = getAblumPicPath(data, this);
        compressWithLs(new File(path), value, imgKey);
//        try {
//            Cursor cursor = resolver.query(originalUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
//            // 使用ContentProvider通过URI获取原始图片
//            Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
//            if (photo != null) {
//                cursor.moveToFirst();
//                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                compressWithLs(new File(path), value, imgKey);
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


    /**
     * 压缩单张图片 Listener 方式
     */
    private void compressWithLs(final File f, final int value, final String imgKey) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            switch (value) {
                                case 1:
                                    //  imgPath = path;
                                    imagekey = key;
                                    imagekeyList.add(imagekey);
                                    bitmapUrls.add(path);
                                    certificateAdapter.setList(bitmapUrls);
                                    break;
                            }
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



    private void submitExchange(String  reason,String pic) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.submitExchange(token,reason,attr_id,order_sn,pic,order_type);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {
            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0,response);
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        EventBus.getDefault().post(new MsgEvent17());
                        ToastUtil.showToast(mContext, "换货申请提交成功");
                        finish();
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                super.onFailure(arg0,arg1);
                ToastUtil.showToast(mContext, "网络状态不佳,请检查您的网络设置");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
