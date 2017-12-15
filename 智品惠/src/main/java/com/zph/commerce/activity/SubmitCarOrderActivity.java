package com.zph.commerce.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.FreightAdapter;
import com.zph.commerce.adapter.SubmitCarOrderAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.Address;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.GoodsInfo;
import com.zph.commerce.bean.OrderCartBase;
import com.zph.commerce.bean.OrderGoods;
import com.zph.commerce.bean.OrderPay;
import com.zph.commerce.bean.RankInfo;
import com.zph.commerce.bean.Shipping;
import com.zph.commerce.bean.UserInfo;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.ToCarMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.ListItemClickHelp;
import com.zph.commerce.utils.CompuUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.widget.MyListView;
import com.zph.commerce.widget.TopNvgBar5;
import com.zph.commerce.widget.swichbutton.ToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


/**
 * 描述 ：购物车商品确认订单页面
 */
public class SubmitCarOrderActivity extends BaseActivity implements
        ListItemClickHelp {


    @ViewInject(R.id.ll_chose_dizhi)
    private LinearLayout ll_chose_dizhi;
    /**
     * 无收货人
     */
    @ViewInject(R.id.ll_no_address)
    private LinearLayout ll_no_address;
    /**
     * 收货人地址
     */
    @ViewInject(R.id.ll_address)
    private LinearLayout ll_address;
    /**
     * 收货人名字
     */
    @ViewInject(R.id.tv_sh_name)
    private TextView tv_sh_name;
    /**
     * 收货人手机号
     */
    @ViewInject(R.id.tv_phone)
    private TextView tv_sh_phone;

    /**
     * 收货人地址
     */
    @ViewInject(R.id.tv_address)
    private TextView tv_sh_des;

    /**
     * 会员等级
     */
    @ViewInject(R.id.rl_rank)
    private RelativeLayout rl_rank;

    /**
     * 会员等级
     */
    @ViewInject(R.id.tv_dengji)
    private TextView tv_dengji;


    /**
     * 商品总数
     */
    @ViewInject(R.id.tv_all_num)
    private TextView tv_all_num;

    /**
     * 商品小计金额
     */
    @ViewInject(R.id.tv_xiaoji)
    private TextView tv_xiaoji;

    /**
     * 实付款：商品小计+运费
     */
    @ViewInject(R.id.tv_shifukuan)
    private TextView tv_shifukuan;


    /**
     * 提交订单
     */
    @ViewInject(R.id.btn_pay)
    private Button btn_pay;


    @ViewInject(R.id.rl_point)
    private RelativeLayout rl_point;
    /**
     * 可用积分
     */
    @ViewInject(R.id.tv_goods_point_need)
    private TextView tv_goods_point_need;
    /**
     * 需要的积分
     */
    @ViewInject(R.id.tv_point_use)
    private TextView tv_point_use;

    @ViewInject(R.id.rl_ziti)
    private RelativeLayout rl_ziti;

    @ViewInject(R.id.switch_ziti)
    private ToggleButton switch_ziti;


    /**
     * 购买数量+销量
     */
    private int totlaNum;

    /**
     * 商品总数量
     */
    private int totlaGoodsNum;

    /**
     * 地址id
     */
    private String fdeliveryAddrId;
    /**
     * 邮费模板id
     */
    private String shipping_tpl_id;
    private String chose_shipping_tpl_id;
    private String shipping_str;
    /**
     * 收货人
     */
    private String accept_name;
    /**
     * 收货人详细地址
     */
    private String address;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 省市区
     */
    private String cr_id;
    private String mProinceName;


    /**
     * 会员价
     */
    private String goods_member_price;

    /**
     * 商品总额
     */
    private String all_goods_price;
    /**
     * 总运费
     */
    private String all_feight;
    /**
     * 实付款金额：all_goods_price+all_feight
     */
    private String total_fee;
    /**
     * 商品需要的总积分
     */
    private String all_gooods_point;

    private Address destination;
    private Intent intent;

    /**
     * 上下文
     **/
    private Context mContext;
    private String token;
    private String phone;
    /**
     * 会员等级
     */
    private String rank_name;

    /**
     * 会员等级
     */
    private int rank_id=1;

    /**
     * 会员销量
     */
    private int number;

    private int from_type;



    @ViewInject(R.id.lv_carOrder)
    private ListView lv_carOrder;
    private List<GoodsInfo> groupList = new ArrayList<GoodsInfo>();
    private SubmitCarOrderAdapter adapter;
    private OrderCartBase orderCartBase;
    private String goods_attr_id;
    private String queryId;
    private int goods_type;
    private int since_order;
    private List<Shipping> shippings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_queren);
        x.view().inject(this);
        mContext = this;
        EventBus.getDefault().register(this);//订阅事件：
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        phone = (String) SPUtils.get(mContext, "phone", "");
        setWidget();
        initViews();
        initDialog();
        intent = getIntent();
        String data = intent.getStringExtra("data");
        orderCartBase = GsonUtil.GsonToBean(data, OrderCartBase.class);
        goods_attr_id = intent.getStringExtra("goods_attr_id");
        queryId = intent.getStringExtra("queryId");
        goods_type = intent.getIntExtra("goods_type", 1);
        from_type= intent.getIntExtra("from_type", 0);
        adapter = new SubmitCarOrderAdapter(mContext, groupList, this, goods_type,rank_id,since_order,shipping_str);
        lv_carOrder.setAdapter(adapter);
        updateWidget(1);

    }

    @Override
    protected void initViews() {
        switch_ziti.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    since_order = 1;
                 //   updateAddress();
                } else {
                    since_order = 0;
                  //  numberChangeResult();
                }
                updateWidget(2);
            }
        });
    }

    @Override
    protected void initEvents() {

    }

    /**
     * @param type 1:第一次进来 2：重新选择了地址，更新商品列表信息和
     */
    private void updateWidget(int type) {
        try {
            goods_type = orderCartBase.getGoods_type();
            groupList = orderCartBase.getGoods_info();
            destination = orderCartBase.getAddr_info();
            shippings=orderCartBase.getShippings();
            if (null == groupList || groupList.isEmpty()) {
                ToastUtil.showToast(mContext, "商品不存在，无法购买");
                finish();
                return;
            }
            totlaGoodsNum = 0;
            all_feight = "0";
            all_goods_price = "0";
            total_fee = "0";
            all_gooods_point = "0";
            for (GoodsInfo goodsInfo : groupList) {
                String postprice = goodsInfo.getPostprice();//邮费
                if(goods_type==2){
                    if(StringUtils.isBlank(shipping_tpl_id)){
                        shipping_tpl_id= goodsInfo.getShipping_tpl_id();
                    }
                    if(null!=shippings&&!shippings.isEmpty()){
                        for (Shipping shipping:shippings){
                            String shipping_id=shipping.getShipping_tpl_id();
                            if(shipping_tpl_id.equals(shipping_id)){
                                shipping.setCheck(true);
                                postprice=shipping.getPrice();
                                shipping_str=shipping.getShipping_str();
                                postprice=CompuUtils.multiply(postprice,goodsInfo.getNumber()+"").toString();
                                goodsInfo.setPostprice(postprice);
                            }else{
                                shipping.setCheck(false);
                            }
                            shipping.setNum(goodsInfo.getNumber());
                        }
                    }
                }
                all_feight = CompuUtils.add(all_feight, postprice).toString();//总邮费
                String attr_price;
                if (goods_type == 1) {
                     attr_price = goodsInfo.getPrice_cost();//商品原价
                }else{
                     attr_price = goodsInfo.getAttr_price();//
                }
                int number = goodsInfo.getNumber();
                attr_price = CompuUtils.multiply(attr_price, number + "").toString();
                all_goods_price = CompuUtils.add(all_goods_price, attr_price).toString();//商品总额
                totlaGoodsNum = CompuUtils.add(totlaGoodsNum, number);//商品总数量
                String point = goodsInfo.getAttr_point();
                point = CompuUtils.multiply(point, number + "").toString();
                all_gooods_point = CompuUtils.add(all_gooods_point, point).toString();//商品需要的总积分
            }
            if(since_order==1){//自提:购买小哈、普通会员才能自提
                all_feight="0";
            }
            total_fee = CompuUtils.add(all_feight, all_goods_price).toString();//实付款
            tv_xiaoji.setText(all_goods_price);
            tv_all_num.setText(totlaGoodsNum + "");
            if (goods_type == 1) {
                rl_rank.setVisibility(View.GONE);
                rl_point.setVisibility(View.VISIBLE);
                rl_ziti.setVisibility(View.GONE);//自提隐藏
                String point = orderCartBase.getPoint();//用户可用积分
                tv_goods_point_need.setText("可用积分:"+point);
                int compare = CompuUtils.compareTo(point, all_gooods_point);
                if (compare >= 0) {//用户积分大于等于商品需要的总积分
                    tv_point_use.setText("-" + all_gooods_point);
                    total_fee = CompuUtils.subtract(total_fee, all_gooods_point).toString();//实付款
                } else {
                    tv_point_use.setText("-" + point);
                    total_fee = CompuUtils.subtract(total_fee, point).toString();//实付款
                }

                updateAddress();
            } else {//2小哈
                rl_rank.setVisibility(View.VISIBLE);
                rl_point.setVisibility(View.GONE);
                rl_ziti.setVisibility(View.VISIBLE);
                numberChangeResult();
            }
            tv_shifukuan.setText(total_fee);
            adapter.updateListview(groupList,rank_id,since_order,shipping_str);
            setListviewHeight(lv_carOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    private void numberChangeResult() {
        UserInfo userInfo = orderCartBase.getUser_info();
        number = userInfo.getNumber();//累计销量
        totlaNum = totlaGoodsNum + number;//本次累计销量
        int rank = userInfo.getRank_id();
        List<RankInfo> rank_info = orderCartBase.getRank_info();
        Collections.sort(rank_info);
        String rank_name1 = "";
        String rank_name2 = "";
        String rank_name3 = "";
        int rank_id1 = 0;
        int rank_id2 = 0;
        int rank_id3 = 0;
        int firstMax = rank_info.get(0).getMax_number();
        int firstNumber = rank_info.get(1).getNumber();//第二期间最小购买数量
        for (int i = 0; i < rank_info.size(); i++) {//取满足一次性购买数量的价格
            RankInfo rankInfo = rank_info.get(i);
            int number = rankInfo.getNumber();
            if (totlaGoodsNum >= number) {//商品购买数量
                rank_name1 = rankInfo.getRank_name();
                rank_id1 = rankInfo.getRank_id();
            }
        }

        for (int j = 0; j < rank_info.size(); j++) {//取满足累计区间的价格
            RankInfo rankInfo = rank_info.get(j);
            int min_number = rankInfo.getMin_number();
            int max_number = rankInfo.getMax_number();
            if (totlaNum >= min_number && totlaNum <= max_number) {
                rank_name2 = rankInfo.getRank_name();
                rank_id2 = rankInfo.getRank_id();
                break;
            }
        }

        for (int j = 0; j < rank_info.size(); j++) {//取满足会员等级的价格
            RankInfo rankInfo = rank_info.get(j);
            int rank_id = rankInfo.getRank_id();
            if (rank == rank_id) {
                rank_name3 = rankInfo.getRank_name();
                rank_id3 = rank_id;
                break;
            }
        }
        int compare1 = CompuUtils.compareTo(rank_id1, rank_id2);
        int compare2 = CompuUtils.compareTo(rank_id2, rank_id3);
        int compare3 = CompuUtils.compareTo(rank_id1, rank_id3);
        if (compare1 > 0) {//rank_id1>rank_id2
            if (compare2 > 0) {//rank_id1>rank_id2      rank_id2>rank_id3    rank_id1等级最高
                rank_name = rank_name1;
                rank_id = rank_id1;
            } else {//rank_id1>rank_id2  rank_id2<rank_id3
                if (compare3 > 0) {//rank_id1>rank_id2   rank_id2<rank_id3       rank_id1>rank_id3   rank_id1等级最高
                    rank_name = rank_name1;
                    rank_id = rank_id1;
                } else {//rank_id1>rank_id2  rank_id2<rank_id3    rank_id1<rank_id3     rank_id3等级最高
                    rank_name = rank_name3;
                    rank_id = rank_id3;
                }
            }
        } else {//rank_id1<rank_id2
            if (compare2 > 0) {// rank_id1<rank_id2    rank_id2>rank_id3   rank_id2等级最高
                rank_name = rank_name2;
                rank_id = rank_id2;
            } else {// rank_id1<rank_id2      rank_id2<rank_id3     rank_id3等级最高
                rank_name = rank_name3;
                rank_id = rank_id3;
            }

        }
        if (totlaGoodsNum >= firstNumber || totlaNum > firstMax || rank_id != 1) {//一次性购买超过10台或者累计超过第一区间最大值,即可成为创客及以上
            ll_chose_dizhi.setVisibility(View.GONE);//不显示地址
            ll_chose_dizhi.setVisibility(View.GONE);
            all_feight = "0";
            total_fee = CompuUtils.add(all_feight, all_goods_price).toString();//实付款
            tv_shifukuan.setText(total_fee);
            btn_pay.setBackgroundColor(mContext.getResources().getColor(R.color.bg_main_bottom));
            rl_ziti.setVisibility(View.GONE);
        } else {//普通会员
            ll_chose_dizhi.setVisibility(View.VISIBLE);//显示地址
            rl_ziti.setVisibility(View.VISIBLE);
            updateAddress();
        }
        tv_dengji.setText(rank_name);
    }

    private void updateAddress() {
        if (null != destination && !StringUtils.isBlank(destination.getAddr_id())) {
            fdeliveryAddrId = destination.getAddr_id();
            tv_sh_name.setText(destination.getAddr_name());
            tv_sh_phone.setText(destination.getAddr_mobile());
            mProinceName = destination.getAddr_province();
            tv_sh_des.setText("收货地址:" + destination.getAddr_province() + " " + destination.getAddr_city() + " " + destination.getAddr_county() + " " + destination.getAddr_detail());
            ll_no_address.setVisibility(View.GONE);
            ll_address.setVisibility(View.VISIBLE);
            btn_pay.setBackgroundColor(mContext.getResources().getColor(R.color.bg_main_bottom));
        } else {
            ll_no_address.setVisibility(View.VISIBLE);
            ll_address.setVisibility(View.GONE);
            btn_pay.setBackgroundColor(mContext.getResources().getColor(R.color.bg_main_bottom50));
        }
    }


    /**
     * 初始化
     */
    private void setWidget() {
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

    @Event({R.id.ll_chose_dizhi, R.id.btn_pay})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.ll_chose_dizhi:// 选择收货人地址
                gotoDestinaons();
                break;
            case R.id.btn_pay:// 生成订单
                if (goods_type == 1) {
                    if (StringUtils.isBlank(fdeliveryAddrId)) {
                        ToastUtil.showToast(mContext, "请先选择收货地址");
                        return;
                    }
                } else {
                    if (rank_id == 1) {
                        if (StringUtils.isBlank(fdeliveryAddrId)) {
                            ToastUtil.showToast(mContext, "请先选择收货地址");
                            return;
                        }
                    }

                }
                dialog.show();
                //生成确认订单
                submitOrder();
                break;

            default:
                break;
        }
    }


    /**
     * 提交订单
     */
    private void submitOrder() {
        Map<String, Object> params = new HashMap<String, Object>();
        if (!StringUtils.isBlank(fdeliveryAddrId)) {
            params.put("addr_id", fdeliveryAddrId);//
        }
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<OrderPay>>> call=null;
        if(goods_type==1){//普通商品
            params.put("goods_attr_id", goods_attr_id);//
            params.put("queryId", queryId);//
            call = userBiz.submitOrderGoods(token, params);
        }else{//小哈
            params.put("goods_attr_id", goods_attr_id);//
            params.put("since_order", since_order);//
            params.put("number", totlaGoodsNum);//
            params.put("shipping_tpl_id", shipping_tpl_id);//
            params.put("order_type", orderCartBase.getGoods_type());//
            call = userBiz.submitOrder(token, params);
        }
        call.enqueue(new HttpCallBack<BaseResponse<List<OrderPay>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<OrderPay>>> arg0,
                                   Response<BaseResponse<List<OrderPay>>> response) {
                dialog.dismiss();
                super.onResponse(arg0, response);
                BaseResponse<List<OrderPay>> baseResponse = response.body();
                if (null != baseResponse) {
                    String msg = baseResponse.getMsg();
                    int code = baseResponse.getCode();
                    if (code == MyConstant.SUCCESS) {
                        List<OrderPay> orderSubmit = baseResponse.getData();
                        if (null == orderSubmit || orderSubmit.isEmpty()) {
                            return;
                        }
                        if(from_type==1){//从购物车购买，购买后刷新购物车页面
                            EventBus.getDefault().post(new ToCarMsgEvent());
                        }
                        OrderPay orderPay = orderSubmit.get(0);
                        String trade_id = orderPay.getTrade_id();
                        String pay_money = orderPay.getPay_money();
                        intent = new Intent(mContext, PayOrderActivity.class);
                        intent.putExtra("trade_id", trade_id);
                        intent.putExtra("pay_money", pay_money);
                        intent.putExtra("goods_type", goods_type);
                        intent.putExtra("rank_id", rank_id);
                        intent.putExtra("type", 1);
                        startActivity(intent);
                    }  else {
                        ToastUtil.showToast(mContext, msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderPay>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    private void gotoDestinaons() {
        intent = new Intent(mContext, AddressChooseActivity.class);
        startActivityForResult(intent, 1);

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
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (null != destination) {
            destination = null;
        }
        EventBus.getDefault().unregister(this);//取消订阅
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            String   mPhone = (String) SPUtils.get(mContext, "phone", "");
            if(!phone.equals(mPhone)){//切换了登陆用户
                finish();
            }
        } else {// 未登录
            finish();
            MyConstant.HASLOGIN = false;
        }
    }


    /**
     * 1:地址选择回调
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Bundle bundle = data.getExtras();
        switch (requestCode) {
            case 1:// 地址回调
                try {
                    int resultType = bundle.getInt("result");// 从收货地址列表重新选择一个地址地址回调(2)、没有地址新增一个地址回调(1)
                    if (resultType == 0) {
                        btn_pay.setBackgroundColor(mContext.getResources().getColor(R.color.bg_main_bottom50));
                        ll_address.setVisibility(View.GONE);
                        ll_no_address.setVisibility(View.VISIBLE);
                    } else {
                        btn_pay.setBackgroundColor(mContext.getResources().getColor(R.color.bg_main_bottom));
                        ll_no_address.setVisibility(View.GONE);
                        ll_address.setVisibility(View.VISIBLE);
                        destination = bundle.getParcelable("place");
                        accept_name = destination.getAddr_name();
                        tv_sh_name.setText(accept_name);
                        fdeliveryAddrId = destination.getAddr_id();
                        mobile = destination.getAddr_mobile();
                        tv_sh_phone.setText(mobile);
                        address = destination.getAddr_detail();
                        String area = destination.getAddr_county();
                        String prvinceName = destination.getAddr_province_name();
                        if (StringUtils.isBlank(area)) {
                            cr_id = prvinceName + " " + destination.getAddr_city_name();
                        } else {
                            cr_id = prvinceName + " " + destination.getAddr_city_name() + " " + destination.getAddr_county_name();
                        }
                        tv_sh_des.setText("收货地址:" + cr_id + " " + address);
                        if (StringUtils.isBlank(mProinceName) || (!mProinceName.equalsIgnoreCase(prvinceName))) {
                            generateOrder();//重新生成确认订单
                        }
                        mProinceName = prvinceName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void generateOrder() {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<OrderCartBase>>> call = null;
        if (goods_type == 1) {
            call = userBiz.generateOrder(token, goods_type, goods_attr_id, queryId, fdeliveryAddrId);
        } else if (goods_type == 2) {
            call = userBiz.xhGenerateOrder(token, goods_type, goods_attr_id, totlaGoodsNum + "", fdeliveryAddrId);
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
                        orderCartBase = baseResponse.getData().get(0);
                        updateWidget(2);
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderCartBase>>> arg0, Throwable arg1) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, "服务器错误");
            }
        });
    }


    @Override
    public void onClick(View item, View widget, int position, int which) {
        GoodsInfo goodsInfo = groupList.get(position);
        int number = goodsInfo.getNumber();
        switch (which) {
            case R.id.tv_num_jian://减
                if (number == 1) {
                    number = 1;
                    ToastUtil.showToast(mContext, "不能再减了");
                    return;
                } else {
                    number--;
                    goodsInfo.setNumber(number);
                    getParms();
                }
                break;
            case R.id.tv_num_jia://加
                number++;
                goodsInfo.setNumber(number);
                getParms();
                break;
            case R.id.tv_yunfei://
               if(goods_type==2&&since_order==0){
                   choseFreightDialog();
               }
                break;
            default:
                break;
        }
    }

    private void getParms() {
        if (goods_type == 1) {
            ArrayList<OrderGoods> orderList = new ArrayList<OrderGoods>();
            OrderGoods orderGoods;
            StringBuilder sb = new StringBuilder();
            for (GoodsInfo goodsInfo1 : groupList) {
                sb.append(goodsInfo1.getGoods_attr_id()).append(",");
                orderGoods = new OrderGoods(goodsInfo1.getGoods_attr_id(), goodsInfo1.getNumber() + "");
                orderList.add(orderGoods);
            }
            goods_attr_id = sb.deleteCharAt(sb.length() - 1).toString();
            queryId = GsonUtil.toJsonString(orderList);
        } else if (goods_type == 2) {
            totlaGoodsNum = groupList.get(0).getNumber();

        }
        generateOrder();
    }


    /**
     */
    private void setListviewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listitemView = listAdapter.getView(i, null, listView);
            listitemView.measure(0, 0);
            totalHeight += listitemView.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + 30;
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }



    private void choseFreightDialog() {
        if(null==shippings||shippings.isEmpty()){
            return;
        }
        final Dialog lDialog = new Dialog(mContext, R.style.shareDialogStyle);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.dialog_yunfei_layout);
        lDialog.setCancelable(true);
        lDialog.setCanceledOnTouchOutside(true);
        MyListView listView = (MyListView) lDialog.findViewById(R.id.lv);
        TextView tv_sure = (TextView) lDialog.findViewById(R.id.tv_sure);
        View close_view = (View) lDialog.findViewById(R.id.close_view);
        // 从底部弹出
        Window dialogWindow = lDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        RelativeLayout lLayout_bg = (RelativeLayout) lDialog.findViewById(R.id.rl_share);
        // 调整dialog背景大小
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int)
                (display.getWidth()), WindowManager.LayoutParams.MATCH_PARENT));
        final FreightAdapter adapter=new FreightAdapter(mContext,shippings);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                for (int i=0;i<shippings.size();i++){
                    Shipping shipping=shippings.get(position);
                    if(i==position){
                        shippings.get(i).setCheck(true);
                        chose_shipping_tpl_id=shipping.getShipping_tpl_id();
                    }else{
                        shippings.get(i).setCheck(false);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shipping_tpl_id=chose_shipping_tpl_id;
                updateWidget(2);
                lDialog.dismiss();

            }
        });
        close_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<shippings.size();i++){
                    Shipping shipping=shippings.get(i);
                    String shipping_id=shipping.getShipping_tpl_id();
                    if(shipping_tpl_id.equals(shipping_id)){
                        shippings.get(i).setCheck(true);
                    }else{
                        shippings.get(i).setCheck(false);
                    }
                }
                lDialog.dismiss();

            }
        });



        lDialog.show();
    }



}
