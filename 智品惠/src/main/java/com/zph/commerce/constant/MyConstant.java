package com.zph.commerce.constant;

import android.os.Environment;

/**
 * 常量
 */

public class MyConstant {
    public final static String WEB_SERVICE_BASE="https://app.zphuivip.com/";

    public final static int SUCCESS=1000;
    public final static int FAILED=1001;
    public final static int ERR_AUTH=1003;
    public final static int PAY_PWD_ERROE=1011;
    public final static int NO_PAY_PWD=1015;
    public static final int T_ERR_AUTH = 1006;//登录过期
    public static final int T_LOGIN_ERR = 1080;//登录异常
  //  public static final int SERVER_ERROE = 403;//登录异常
    public static final int SERVER_ERROE = 500;//服务器异常
    public static  String  PIC_DPI2="2.png";

    /**关于*/
    public static final String COM_ABOUT = "https://mp.weixin.qq.com/s/BNoavYMGWBcjcIKMB_QHXw";

    public static final String COM_SERVICE = "https://www2.zphuivip.com/public/service/agreement";


    public static final String IMG_PATH = Environment.getExternalStorageDirectory().getPath().toString();
    public static final int DEF_IMG_W = 800;
    public static final int DEF_IMG_H = 640;

    /**阿里云EndPoint*/
    public static final String ALI_ENDPOINT = "http://oss-cn-shenzhen.aliyuncs.com";
    public static final String ALI_KEYID = "LTAIsxUPdsSknfT0";
    public static final String ALI_KEYSECRET = "kUDMQin1Han5PrJIyvXnmifTjWCk6g";


    /**上传下载的bucketName*/
    public static final String ALI_BUCKET_RECOMMEND = "zph-private-presonal"; //

    public static final String ALI_PUBLIC_BUCKET_RECOMMEND = "zph-public-presonal"; //


    /**图片前缀*/
    public static final String ALI_PUBLIC_URL = "http://zph-public-presonal.oss-cn-shenzhen.aliyuncs.com/";

    /**banner图片前缀*/
    public static  String BANNER_PUBLIC_URL = " http://www2-2.ddzyigou.com/views/public/banner/";



    public static  Boolean HASLOGIN=false;
    public static final String SUC_RESULT="0";
    public final static String VISTER="ddz";

    public static final String AUTH_USER="authUser";
    public static final String CLOSE_ON_KEYDOWN="close_on_keydown";
    public static final String COM_PHONE_NUM = "0755-23071963";

    /**
     * 本应用的文件
     */
    public final static String APP_HOME_PATH = Environment.getExternalStorageDirectory() + "/zph/";

    public static final String SHARE_IMAGE_PATH = APP_HOME_PATH + "app_icon.jpg";



}
