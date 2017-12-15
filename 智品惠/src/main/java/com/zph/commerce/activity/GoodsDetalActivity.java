package com.zph.commerce.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zph.commerce.R;
import com.zph.commerce.adapter.ProductImageListrAdapter;
import com.zph.commerce.adapter.ScaleImgAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.CommentContent;
import com.zph.commerce.bean.OrderCartBase;
import com.zph.commerce.bean.OrderGoods;
import com.zph.commerce.bean.Specifications;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.constant.QQConstants;
import com.zph.commerce.constant.WxConstants;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.ShareDialog;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.MsgEvent4;
import com.zph.commerce.eventbus.ToCarMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.FileUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.popwindow.ActionItem;
import com.zph.commerce.view.popwindow.BuyPopup;
import com.zph.commerce.view.popwindow.TitlePopAdapter;
import com.zph.commerce.view.popwindow.TitlePopWindow;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.GlideCircleTransform;
import com.zph.commerce.widget.MyScrollView;
import com.zph.commerce.widget.viewpager.LoopViewPager;
import com.zph.commerce.widget.viewpager.MaterialIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by 24448 on 2017/10/20.
 */

public class GoodsDetalActivity extends BaseActivity implements MyScrollView.OnScrollChangeListener, IUiListener {


    @ViewInject(R.id.tv_goods)
    private TextView tv_goods;

    @ViewInject(R.id.view_goods)
    private View view_goods;

    @ViewInject(R.id.tv_detals)
    private TextView tv_detals;

    @ViewInject(R.id.view_detals)
    private View view_detals;

    @ViewInject(R.id.view_evaluation)
    private View view_evaluation;

    @ViewInject(R.id.img_pop)
    private ImageView img_pop;

    @ViewInject(R.id.tv_guige)
    private TextView tv_guige;

    @ViewInject(R.id.tv_goods_name)
    private TextView tv_goods_name;


    @ViewInject(R.id.tv_price)
    private TextView tv_price;
    @ViewInject(R.id.tv_1)
    private TextView tv_1;
    @ViewInject(R.id.tv_point)
    private TextView tv_point;
    @ViewInject(R.id.tv_2)
    private TextView tv_2;
    @ViewInject(R.id.tv_member_price)
    private TextView tv_member_price;
    @ViewInject(R.id.ll_point)
    private LinearLayout ll_point;


    @ViewInject(R.id.lv_img)
    private ListView listImg;
    @ViewInject(R.id.pager)
    private LoopViewPager pager;
    @ViewInject(R.id.goodsIndicator)
    private MaterialIndicator goodsIndicator;


    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;
    @ViewInject(R.id.sv_goods)
    private MyScrollView sv_goods;

    @ViewInject(R.id.ll_detal)
    private LinearLayout ll_detal;

    @ViewInject(R.id.ll_evaluation)
    private LinearLayout ll_evaluation;


    @ViewInject(R.id.tv_add_car)
    private TextView tv_add_car;
    /**
     * 头像
     */
    @ViewInject(R.id.img_logo)
    private ImageView img_logo;
    /**
     * 评论数
     */
    @ViewInject(R.id.tv_evaluate_num)
    private TextView tv_evaluate_num;
    /**
     * 昵称
     */
    @ViewInject(R.id.tv_name)
    private TextView tv_name;
    /**
     * 等级
     */
    @ViewInject(R.id.tv_rank)
    private TextView tv_rank;
    /**
     * 规格
     */
    @ViewInject(R.id.tv_prameger)
    private TextView tv_prameger;
    /**
     * 评论
     */
    @ViewInject(R.id.tv_evaluation_text)
    private TextView tv_evaluation_text;
    /**
     * 全部
     */
    @ViewInject(R.id.all_radio)
    private RadioButton all_radio;
    /**
     * 好评
     */
    @ViewInject(R.id.good_radio)
    private RadioButton good_radio;
    /**
     * 中评
     */
    @ViewInject(R.id.general_radio)
    private RadioButton general_radio;
    /**
     * 差评
     */
    @ViewInject(R.id.bad_radio)
    private RadioButton bad_radio;
    @ViewInject(R.id.ll_person)
    private LinearLayout ll_person;
    @ViewInject(R.id.ll_guige)
    private LinearLayout ll_guige;


    private Context mContext;
    private TitlePopWindow window;
    private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();
    private ArrayList<String> imageUrlLists = new ArrayList<>();
    /**
     * 商品介绍图片集合
     **/
    private ArrayList<String> imageDetalUrls = new ArrayList<String>();
    private ScaleImgAdapter adapter;
    private Intent intent;
    private int top2, top3;
    private String productId;
    private ProductImageListrAdapter productImageListrAdapter;
    private BuyPopup buyPopup;
    private String token;
    private LayoutInflater mLayoutInflater;
    private GoodsBean goodsBean;
    private String attr_id;
    private String num;
    private int goods_type;
    private Tencent mTencent;// 新建Tencent实例用于调用分享方法
    private IWXAPI api;
    private String shareUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods_detals);
        x.view().inject(this);
        EventBus.getDefault().register(this);
        mContext = this;
        productId = getIntent().getStringExtra("productId");
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        shareUrl = "https://www2.zphuivip.com/secured/swiper?goods_id=" + productId;
        initDialog();
        initData();
        initViews();
        initEvents();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 给标题栏弹窗添加子类
        mActionItems.add(new ActionItem(this, "消息", R.drawable.search_news));
        mActionItems.add(new ActionItem(this, "首页", R.drawable.search_home));
        mActionItems.add(new ActionItem(this, "分享", R.drawable.search_share));
    }


    @Override
    protected void initViews() {
        ViewTreeObserver vto = tv_goods.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                //	mImgOverlay.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                int height = mImgOverlay.getMeasuredHeight();
//                int width = mImgOverlay.getMeasuredWidth();
                top2 = ll_detal.getTop();
                top3 = ll_evaluation.getTop();

                return true;
            }
        });
        sv_goods.setOnScrollChangeListener(this);
        sv_goods.smoothScrollTo(0, 0);
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGoodDetail();
            }
        });
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGoodDetail();
            }
        });
        mTencent = Tencent.createInstance(QQConstants.mAppid, mContext);
        api = WXAPIFactory.createWXAPI(mContext, WxConstants.APP_ID, true);
        api.registerApp(WxConstants.APP_ID);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onScrollChange(MyScrollView view, int x, int y, int oldx, int oldy) {


//        if (oldy < y && ((y - oldy) > 15)) {// 向上
//         //   Log.e("wangly", "距离："+(oldy < y) +"---"+(y - oldy));
//         //   Log.d("TAG","向上滑动")
//       //     ToastUtil.showToast(mContext,"向上滑动");
//
//            if (!checkIsVisible(this, tv_guige)) {
//                view_goods.setVisibility(View.GONE);
//                view_detals.setVisibility(View.VISIBLE);
//            }
//
//        }  else if (oldy > y && (oldy - y) > 15) {// 向下
//          //  Log.e("wangly", "距离："+(oldy > y) +"---"+(oldy - y));
//        //    Log.d("TAG"," 向下滑动")
//        //    ToastUtil.showToast(mContext,"向下滑动");
//
//            if (checkIsVisible(this, tv_price)) {
//                view_goods.setVisibility(View.VISIBLE);
//                view_detals.setVisibility(View.GONE);
//            }
//        }
        if (isClickTitle) return;
        if (y >= ll_evaluation.getTop() && y < ll_detal.getTop()) {
            view_goods.setVisibility(View.GONE);
            view_evaluation.setVisibility(View.VISIBLE);
            view_detals.setVisibility(View.GONE);
        } else if (y >= ll_detal.getTop()) {
            view_goods.setVisibility(View.GONE);
            view_evaluation.setVisibility(View.GONE);
            view_detals.setVisibility(View.VISIBLE);
        } else {
            view_goods.setVisibility(View.VISIBLE);
            view_evaluation.setVisibility(View.GONE);
            view_detals.setVisibility(View.GONE);
        }
    }


    @Override
    public void onScrollBottomListener() {
        // Log.e("slantech","滑动到底部");
    }


    @Override
    public void onScrollTopListener() {
        //  Log.e("slantech","滑动到顶部");
    }

    public static Boolean checkIsVisible(Context context, View view) {
        // 如果已经加载了，判断广告view是否显示出来，然后曝光
        int screenWidth = getScreenMetrics(context).x;
        int screenHeight = getScreenMetrics(context).y;
        Rect rect = new Rect(0, 0, screenWidth, screenHeight);
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (view.getLocalVisibleRect(rect)) {
            return true;
        } else {
            //view已不在屏幕可见区域;
            return false;
        }
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     *
     * @param context
     * @return
     */
    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        return new Point(w_screen, h_screen);
    }

    @Override
    protected void initEvents() {
        adapter = new ScaleImgAdapter(imageDetalUrls, mContext);//初始化图文详情
        listImg.setAdapter(adapter);
        getGoodDetail();
    }

    private boolean isClickTitle = false;
    int currentPosition = 0;

    @Event({R.id.rl_back, R.id.tv_contact, R.id.tv_buy, R.id.img_pop,
            R.id.tv_goods, R.id.tv_detals, R.id.tv_myaddress, R.id.tv_guige, R.id.tv_add_car, R.id.tv_evaluation,
            R.id.tv_evaluate_num,R.id.all_radio,R.id.good_radio,R.id.general_radio,R.id.bad_radio})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.rl_back://
                finish();
                break;
            case R.id.tv_contact://联系客服
                showDialDialog();
                break;
            case R.id.tv_add_car://加入购物车
                if (MyConstant.HASLOGIN) {
                    if (null == goodsBean) {
                        return;
                    } else if (StringUtils.isBlank(attr_id)) {
                        buyPopup = new BuyPopup(mContext, mLayoutInflater.inflate(R.layout.view_pop, null), goodsBean, currentPosition, attr_id, 3);
                        buyPopup.setOnDismissListener(new OnPopupDismissListener());
                        buyPopup.showAtLocation(sv_goods, Gravity.CENTER, 0, 0);
                    } else {
                        if (buyPopup != null && buyPopup.isShowing()) {
                            buyPopup.dismiss();
                        }
                        addShoppingCart();
                    }
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tv_buy://立即购买
                if (MyConstant.HASLOGIN) {
                    if (null == goodsBean) {
                        return;
                    } else if (StringUtils.isBlank(attr_id)) {
                        buyPopup = new BuyPopup(mContext, mLayoutInflater.inflate(R.layout.view_pop, null), goodsBean, currentPosition, attr_id, 2);
                        buyPopup.setOnDismissListener(new OnPopupDismissListener());
                        buyPopup.showAtLocation(sv_goods, Gravity.CENTER, 0, 0);
                    } else {
                        if (buyPopup != null && buyPopup.isShowing()) {
                            buyPopup.dismiss();
                        }
                        generateOrder();
                    }
                } else {
                    intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.tv_guige://选择规格
                if (null == goodsBean) {
                    return;
                }
                buyPopup = new BuyPopup(mContext, mLayoutInflater.inflate(R.layout.view_pop, null), goodsBean, currentPosition, attr_id, 1);
                buyPopup.setOnDismissListener(new OnPopupDismissListener());
                buyPopup.showAtLocation(sv_goods, Gravity.CENTER, 0, 0);
                break;
            case R.id.img_pop://右上角
                showMorePopWindow();
                break;
            case R.id.tv_goods://商品
                view_goods.setVisibility(View.VISIBLE);
                view_evaluation.setVisibility(View.GONE);
                view_detals.setVisibility(View.GONE);
                isClickTitle = true;
                sv_goods.smoothScrollTo(0, 0);
                break;
            case R.id.tv_detals://详情
                view_goods.setVisibility(View.GONE);
                view_evaluation.setVisibility(View.GONE);
                view_detals.setVisibility(View.VISIBLE);
                isClickTitle = true;
                sv_goods.smoothScrollTo(0, top2);
                break;
            case R.id.tv_evaluation://评价
                view_goods.setVisibility(View.GONE);
                view_evaluation.setVisibility(View.VISIBLE);
                view_detals.setVisibility(View.GONE);
                isClickTitle = true;
                sv_goods.smoothScrollTo(0, top3);
                break;
            case R.id.tv_evaluate_num://查看评价
                intent=new Intent(mContext,EvaluateListActivity.class);
                intent.putExtra("goods_id",productId);
                intent.putExtra("leval",0);
                startActivity(intent);
                break;
            case R.id.all_radio://全部
                intent=new Intent(mContext,EvaluateListActivity.class);
                intent.putExtra("goods_id",productId);
                intent.putExtra("leval",0);
                startActivity(intent);
                break;
            case R.id.good_radio://好评
                intent=new Intent(mContext,EvaluateListActivity.class);
                intent.putExtra("goods_id",productId);
                intent.putExtra("leval",1);
                startActivity(intent);
                break;
            case R.id.general_radio://中评
                intent=new Intent(mContext,EvaluateListActivity.class);
                intent.putExtra("goods_id",productId);
                intent.putExtra("leval",2);
                startActivity(intent);
                break;
            case R.id.bad_radio://差评
                intent=new Intent(mContext,EvaluateListActivity.class);
                intent.putExtra("leval",3);
                intent.putExtra("goods_id",productId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }


    private void showMorePopWindow() {
        window = new TitlePopWindow(this, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, mActionItems);
        if (Build.VERSION.SDK_INT == 24) {
            // 适配 android 7.0
            int[] location = new int[2];
            img_pop.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            window.showAtLocation(img_pop, Gravity.NO_GRAVITY, -100, y + img_pop.getHeight());
        } else {
            window.showAsDropDown(img_pop, 30, 0);
        }
//            window.showAsDropDown(img_pop, 30, 0);
        // 实例化标题栏按钮并设置监听

        window.setOnItemClickListener(new TitlePopAdapter.onItemClickListener() {

            @Override
            public void click(int position, View view) {
                switch (position) {
                    case 0:// 消息
                        if (MyConstant.HASLOGIN) {
                            intent = new Intent(mContext, MyMessageActivity.class);
                        } else {
                            intent = new Intent(mContext, LoginActivity.class);
                        }
                        startActivity(intent);
                        break;
                    case 1:// 首页
                        finish();
                    case 2:// 分享
                        showShareDialog();
                        break;
                    default:
                        break;
                }

            }
        });
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
        alert.showDialog(GoodsDetalActivity.this, getResources().getString(R.string.dialog_personal_phone), "呼叫", "取消");
    }


    private void getGoodDetail() {
        dialog.show();
        stateLayout.showProgressView();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<GoodsBean>>> call = userBiz.getGoodDetail(productId);
        call.enqueue(new HttpCallBack<BaseResponse<List<GoodsBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<GoodsBean>>> arg0,
                                   Response<BaseResponse<List<GoodsBean>>> response) {
                super.onResponse(arg0, response);
                dialog.dismiss();
                BaseResponse<List<GoodsBean>> baseResponse = response.body();
                if (baseResponse != null) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        if (baseResponse.getData().size() == 0) {
                            stateLayout.showEmptyView("暂无数据");
                            return;
                        }
                        stateLayout.showContentView();
                        goodsBean = baseResponse.getData().get(0);
                        tv_goods_name.setText(goodsBean.getGname());
                        goods_type = goodsBean.getGoods_type();
                        if (goods_type == 1) {
                            tv_price.setText(goodsBean.getPrice_cost());
                            ll_point.setVisibility(View.VISIBLE);
                            tv_member_price.setText(goodsBean.getPrice());
                            String point = goodsBean.getPoint();//不显示积分
                            if (StringUtils.isBlank(point)) {
                                point = "0";
                            }
                            tv_point.setText(point);
                        } else {
                            tv_add_car.setVisibility(View.GONE);
                            tv_price.setText(goodsBean.getPrice());
                            ll_point.setVisibility(View.GONE);
                        }
                        String goods_logo = goodsBean.getGoods_logo();
                        String[] arr = goods_logo.split(",");
                        for (int i = 0; i < arr.length; i++) {
                            imageUrlLists.add(arr[i]);
                        }
                        productImageListrAdapter = new ProductImageListrAdapter(mContext, imageUrlLists);
                        pager.setAdapter(productImageListrAdapter);
                        if (imageUrlLists.size() > 1) {
                            pager.setLooperPic(true);
                            pager.addOnPageChangeListener(goodsIndicator);
                            goodsIndicator.setAdapter(pager.getAdapter());
                        }
                        updateProductImgUrl(goodsBean.getDetail());
                        updateEvaluate();
                    } else {
                        stateLayout.showEmptyView("暂无数据");
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<GoodsBean>>> arg0, Throwable arg1) {
                stateLayout.showEmptyView("网络连接错误");
                dialog.dismiss();
            }
        });
    }

    private void updateProductImgUrl(String detail) {
        imageDetalUrls.clear();
        String[] arr = detail.split(",");
        for (int i = 0; i < arr.length; i++) {
            if (i > 16) {
                break;
            }
            imageDetalUrls.add(arr[i]);
        }
        adapter.notifyDataSetChanged();
        //      adapter.updataListView(imageDetalUrls);
    }

    private void updateEvaluate() {

        int commentNum = goodsBean.getCommentNum();
        CommentContent commentContent = goodsBean.getCommentContent();
        String headurl = goodsBean.getHeadurl();
        tv_evaluate_num.setText("(" + commentNum + ")");
        if (commentNum==0) {
            tv_evaluation_text.setText("暂无评论");
            ll_person.setVisibility(View.GONE);
            ll_guige.setVisibility(View.GONE);
        } else {
            ll_person.setVisibility(View.VISIBLE);
            ll_guige.setVisibility(View.VISIBLE);
            tv_evaluation_text.setText(commentContent.getComment());
            tv_name.setText(commentContent.getNick());
            tv_rank.setText(commentContent.getRank_name());
            tv_prameger.setText(commentContent.getAttr_value());
            int type = commentContent.getDerail();
            String url;
            if (type == 1) {
                url = headurl + "/" + commentContent.getHead();
            } else {
                url = headurl + commentContent.getHead();
            }
            Glide.with(mContext).load(url).override(25, 25).transform(new GlideCircleTransform(mContext))
                    .placeholder(R.drawable.pic_nomal_loading_style)
                    .error(R.drawable.img_default_head)
                    .into(img_logo);
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(MsgEvent4 messageEvent) {
        currentPosition = messageEvent.getPosition();
        num = messageEvent.getNumber();
        attr_id = messageEvent.getId();
        int type = messageEvent.getType();
        Specifications specifications = messageEvent.getSpecifications();
        if (type == 1) {//关闭规格弹窗回调
            buyPopup.dismiss();
            if (StringUtils.isBlank(attr_id)) {
                tv_guige.setText("请选择规格");
                tv_goods_name.setText(goodsBean.getGname());
                if (goods_type == 1) {
                    tv_price.setText(goodsBean.getPrice_cost());
                } else {
                    tv_price.setText(goodsBean.getPrice());
                }
//                String point = goodsBean.getPoint();//不显示积分
//                if (!StringUtils.isBlank(point) && !point.equals("0")) {
//                    tv_point.setText("+" + point + "积分");
//                    tv_point.setVisibility(View.VISIBLE);
//                } else {
//                    tv_point.setVisibility(View.GONE);
//                }
            } else {//选了规格，点击确定
                tv_guige.setText("已选:" + specifications.getAttr_value());
                if (goods_type == 1) {
                    tv_price.setText(specifications.getPrice_cost());
                } else {
                    tv_price.setText(specifications.getAttr_price());
                }
//                String point = specifications.getAttr_point();//不显示积分
//                if (!StringUtils.isBlank(point) && !point.equals("0")) {
//                    tv_point.setText(point);
//                    tv_point.setVisibility(View.VISIBLE);
//                    tv_1.setVisibility(View.VISIBLE);
//                    tv_2.setVisibility(View.VISIBLE);
//                } else {
//                    tv_point.setVisibility(View.GONE);
//                    tv_1.setVisibility(View.GONE);
//                    tv_2.setVisibility(View.GONE);
//                }
            }
        } else {
            if (StringUtils.isBlank(attr_id)) {
                ToastUtil.showToast(mContext, "请选择商品规格");
                return;
            }
            tv_guige.setText("已选:" + specifications.getAttr_value());
            if (buyPopup != null && buyPopup.isShowing()) {
                buyPopup.dismiss();
            }
            if (type == 2) {//立即购买
                generateOrder();
            } else if (type == 3) {//加入购物车
                addShoppingCart();
            }
        }
    }

    /**
     * popupwindow消失的回调
     */
    private class OnPopupDismissListener implements
            android.widget.PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            // 标题和主页开始播放动画
            // mLlytTitle.startAnimation(mTranInAnimation);
            // mLlytTitle.setBackgroundResource(R.color.blueshit)
            //   currentPosition= messageEvent.getPosition();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消事件注册
        EventBus.getDefault().unregister(this);
        if (Util.isOnMainThread()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }
    }


    private void generateOrder() {
        dialog.show();

        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<OrderCartBase>>> call = null;
        int type = goodsBean.getGoods_type();
        OrderGoods orderGoods = new OrderGoods(attr_id, num);
        ArrayList<OrderGoods> orderList = new ArrayList<OrderGoods>();
        orderList.add(orderGoods);
        final String queryId = GsonUtil.toJsonString(orderList);
        if (type == 1) {
            call = userBiz.generateOrder(token, goodsBean.getGoods_type(), attr_id, queryId, "");
        } else if (type == 2) {
            call = userBiz.xhGenerateOrder(token, goodsBean.getGoods_type(), attr_id, num, "");
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<OrderCartBase>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<OrderCartBase>>> arg0,
                                   Response<BaseResponse<List<OrderCartBase>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<OrderCartBase>> baseResponse = response.body();
                if (baseResponse != null) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        if (baseResponse.getData().size() == 0)
                            return;
                        OrderCartBase orderCartBase = baseResponse.getData().get(0);
                        String data = GsonUtil.toJsonString(orderCartBase);
                        intent = new Intent(mContext, SubmitCarOrderActivity.class);
                        intent.putExtra("data", data);
                        intent.putExtra("goods_attr_id", attr_id);
                        intent.putExtra("queryId", queryId);
                        intent.putExtra("goods_type", goods_type);
                        startActivity(intent);
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderCartBase>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "连接服务器失败,请检查您的网络状态");
            }
        });
    }


    /**
     * 加入购物车
     */
    private void addShoppingCart() {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<Object>> call = userBiz.addShoppingCart(token, attr_id, num);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<Object> baseResponse = response.body();
                if (baseResponse != null) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        ToastUtil.showToast(mContext, "加入购物车成功");
                        EventBus.getDefault().post(new ToCarMsgEvent());
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "连接服务器失败,请检查您的网络状态");
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
            MyConstant.HASLOGIN = false;
        }
    }


    private void showShareDialog() {
        ShareDialog shareDialog = new ShareDialog();
        shareDialog.setListener(new ShareDialog.OnOkCancelClickedListener() {
            @Override
            public void onClick(int id) {
                switch (id) {
                    case R.id.ll_wx:
                        shareToWeChat(0);

                        break;
                    case R.id.ll_pyq:

                        shareToWeChat(1);
                        break;
                    case R.id.ll_qq:
                        shareToQQ();
                        break;
                    case R.id.ll_kj:

                        shareToQzone();
                        break;
                }
            }
        });
        shareDialog.showDialog(GoodsDetalActivity.this);
    }


    public void shareToQQ() {
        File appIconfile = createFile();
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, goodsBean.getGname());// 标题
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "智品惠是以智能产品为核心的一款结合线上上传、线下体验中心的自动化购买模式的APP");// 摘要
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareUrl);// 内容地址
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, appIconfile.getAbsolutePath());// 网络图片地址　　
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "智品惠");// 应用名称
        params.putString(QQShare.SHARE_TO_QQ_EXT_INT, "其它附加功能");
        mTencent.shareToQQ(this, params, this);
    }

    private void shareToQzone() {
        File appIconfile = createFile();
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, goodsBean.getGname());
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "智品惠是以智能产品为核心的一款结合线上上传、线下体验中心的自动化购买模式的APP");
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareUrl);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, appIconfile.getAbsolutePath());
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "智品惠");
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        mTencent.shareToQQ(this, params, this);
    }


    // 0-分享给朋友  1-分享到朋友圈
    private void shareToWeChat(int flag) {
        if (!api.isWXAppInstalled()) {
            ToastUtil.showToast(mContext, "请先安装微信!");
            return;
        }
        File appIconfile = createFile();
        //创建一个WXWebPageObject对象，用于封装要发送的Url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareUrl;
        //创建一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = goodsBean.getGname();
        msg.description = "智品惠是以智能产品为核心的一款结合线上上传、线下体验中心的自动化购买模式的APP";


        Bitmap bmp = BitmapFactory.decodeFile(appIconfile.getAbsolutePath());
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 120, 120, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);
        bmp.recycle();


        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());//transaction字段用于唯一标识一个请求，这个必须有，否则会出错
        req.message = msg;
        //表示发送给朋友圈  WXSceneTimeline  表示发送给朋友  WXSceneSession
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
        //  msg.thumbData =getThumbData();//封面图片byte数组
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    @Override
    public void onComplete(Object o) {
        // 操作成功
        ToastUtil.showToast(mContext, "分享成功");
    }

    @Override
    public void onError(UiError uiError) {
        // 分享异常
    }

    @Override
    public void onCancel() {
        // 取消分享
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, this);
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_QQ_SHARE || resultCode == Constants.REQUEST_QZONE_SHARE || resultCode == Constants.REQUEST_OLD_SHARE) {
                Tencent.handleResultData(data, this);
            }
        }
    }

    private File createFile() {
        File file = new File(MyConstant.APP_HOME_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        File appIconfile = new File(MyConstant.SHARE_IMAGE_PATH);
        try {
            if (!appIconfile.exists()) {
                appIconfile.createNewFile();
            }
            FileUtils.copyAssetsFile(mContext, "app_icon.png", appIconfile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appIconfile;
    }


}
