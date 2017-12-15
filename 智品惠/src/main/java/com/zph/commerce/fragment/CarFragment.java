package com.zph.commerce.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.activity.LoginActivity;
import com.zph.commerce.activity.SubmitCarOrderActivity;
import com.zph.commerce.adapter.ShoppingCarAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.CartGoodsBase;
import com.zph.commerce.bean.OrderCartBase;
import com.zph.commerce.bean.OrderGoods;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.DialogConfirm;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.eventbus.ToCarMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.interfaces.ListItemClickHelp;
import com.zph.commerce.utils.CompuUtils;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.GsonUtil;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.swipe.SwipeMenu;
import com.zph.commerce.view.pullableview.swipe.SwipeMenuCreator;
import com.zph.commerce.view.pullableview.swipe.SwipeMenuItem;
import com.zph.commerce.view.pullableview.swipe.SwipeMenuListView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zph.commerce.R.style.dialog;

@ContentView(R.layout.main_fragment_car)
public class CarFragment extends Fragment implements PullLayout.OnRefreshListener, ListItemClickHelp {
    @ViewInject(R.id.ll_top)
    private TopNvgBar5 topNvgBar;
    @ViewInject(R.id.cb_car_all)
    private CheckBox cb_car_all;
    /**
     * 小计
     */
    @ViewInject(R.id.tv_car_Allprice)
    private TextView tv_car_Allprice;
    /**
     * 结算、删除
     */
    @ViewInject(R.id.tv_cart_buy_or_del)
    private TextView tv_cart_buy_or_del;
    /**
     * 小计数量
     */
    @ViewInject(R.id.tv_num)
    private TextView tv_num;
    @ViewInject(R.id.ll_money)
    private LinearLayout ll_money;

    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.car_refresh_view)
    private PullLayout ptrl;

    @ViewInject(R.id.listView_shopping)
    private SwipeMenuListView listView_shopping;

    private View mRootView;
    private String token;

    private boolean isEdit = true;
    /**
     * 商品被选中的总数
     */
    private int itemCheckNumber = 0;

    /**
     * 上下文
     **/
    private Context mContext;
    private ShoppingCarAdapter adapter;

    /**
     * 判断是否全选
     */
    private boolean isSelectAll;


    /**
     * 购物车数据
     */
    private List<CartGoodsBase> groupList = new ArrayList<CartGoodsBase>();
    /**
     * 选中的购物车数据:带分类头部
     */
    private ArrayList<CartGoodsBase> checkList = new ArrayList<CartGoodsBase>();
    private LoadingDialog dialogs;
    /**
     * 购物车选中商品总价
     */
    private String totalMoney = "0.00";

    public CarFragment() {
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
        ptrl.setOnRefreshListener(this);
        listView_shopping.canLoad = false;// 设置不能上拉
        adapter = new ShoppingCarAdapter(mContext, groupList, this);
        listView_shopping.setAdapter(adapter);
        setWidget();
        return mRootView;
    }

    private void setWidget() {
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        dialogs = new LoadingDialog(mContext, dialog, "加载中...");
        dialogs.setCancelable(true);
        dialogs.setCanceledOnTouchOutside(true);
        if (MyConstant.HASLOGIN) {// 第一次加载进货单页面，未登录不取数据
            getShoppingCart(1);
        }
        topNvgBar.setLeftVisibility(View.GONE);
        topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
            @Override
            public void onLeftClick() {

            }

            @Override
            public void onRightClick() {
                if (isEdit) {
                    topNvgBar.setRight("完成");
                    tv_cart_buy_or_del.setText("删除");
                    tv_num.setVisibility(View.GONE);
                    isEdit = false;
                    ll_money.setVisibility(View.INVISIBLE);
                    //     setEditStatus(1);
                } else if (!isEdit) {
                    isEdit = true;
                    topNvgBar.setRight("编辑");
                    tv_cart_buy_or_del.setText("结算");
                    ll_money.setVisibility(View.VISIBLE);
                    if (itemCheckNumber != 0) {
                        tv_num.setText("(" + itemCheckNumber + ")");
                        tv_num.setVisibility(View.VISIBLE);
                    } else {
                        tv_num.setVisibility(View.GONE);
                    }
//                    setEditStatus(2);
                }
                adapter.notifyDataSetChanged();
            }
        });


        // 侧滑
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(mContext);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(255, 63, 104)));
                // set item width
                openItem.setWidth(DimenUtils.px2dip(mContext, 360));
                // set item title
                openItem.setTitle("删除");
                // // set item title fontsize
                openItem.setTitleSize(16);
                // // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // openItem.setIcon(R.drawable.collect);
                // add to menu
                menu.addMenuItem(openItem);


            }
        };
        listView_shopping.setMenuCreator(creator);
        // 侧滑监听
        listView_shopping.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position,
                                           SwipeMenu menu, int index) {
                CartGoodsBase xyGoods = (CartGoodsBase) adapter.getItem(position);
                String carId = xyGoods.getId();
                showDeleteCartDialog(carId);
                return false;
            }
        });


        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getShoppingCart(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getShoppingCart(1);
            }
        });

    }


    @Event({R.id.cb_car_all, R.id.tv_cart_buy_or_del})
    private void click(View view) {
        switch (view.getId()) {
            case R.id.tv_cart_buy_or_del://结算、删除
                if (null == groupList || groupList.isEmpty()) {// 如果购物车为空的
                    ToastUtil.showToast(mContext, "购物车空空如也");
                } else {// 购物车不为空的
                    if (checkList.size() == 0) {// 未选择商品
                        ToastUtil.showToast(mContext, "请先选择商品");
                    } else {// 选择了商品
                        if (isEdit) {//结算
                            generateOrder();
                        } else {// 删除
                            String goodsIds = getGoodsIds();
                            showDeleteCartDialog(goodsIds);
                        }
                    }
                }
                break;
            case R.id.cb_car_all://全选
                checkAll();
                break;
        }
    }


    /**
     * 全选
     */
    private void checkAll() {
        try {
            checkList.clear();
            totalMoney = "0.00";
            itemCheckNumber = 0;
            // 全选未选中：isSelectAll=false→→→→!isSelectAll=true→→→→isSelectAll=true
            isSelectAll = !isSelectAll;
            // 全选未选中，遍历列表将全选和单选框都选中
            if (isSelectAll) {
                for (int i = 0; i < groupList.size(); i++) {
                    CartGoodsBase bean = groupList.get(i);
                    bean.setChecked(true);
                    checkList.add(bean);
                    totalMoney = CompuUtils.add(totalMoney, bean.getPrice_cost()).toString();
                }
                // 返回的是字符串
                tv_car_Allprice.setText(totalMoney);
                cb_car_all.setChecked(true);
            } else {// 全选已选中，遍历列表将全选和单选框改为未选中
                for (int i = 0; i < groupList.size(); i++) {
                    CartGoodsBase bean = groupList.get(i);
                    bean.setChecked(false);
                }
                cb_car_all.setChecked(false);
            }
            // 通知viewList刷新(执行下面所有item被全选)
            adapter.notifyDataSetChanged();
            if (!MyConstant.HASLOGIN) {
                totalMoney = "0.00";
                tv_car_Allprice.setText(totalMoney);
                cb_car_all.setChecked(false);
            } else {
                tv_car_Allprice.setText(totalMoney);
            }
            itemCheckNumber = checkList.size();
            if (itemCheckNumber != 0) {
                tv_num.setText("(" + itemCheckNumber + ")");
                tv_num.setVisibility(View.VISIBLE);
            } else {
                tv_num.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void getShoppingCart(final int state) {
        cb_car_all.setChecked(false);
        isSelectAll = false;
        checkList.clear();
        totalMoney = "0.00";
        itemCheckNumber = 0;
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<CartGoodsBase>>> call = userBiz.queryUserCart(token);
        call.enqueue(new Callback<BaseResponse<List<CartGoodsBase>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<CartGoodsBase>>> arg0,
                                   Response<BaseResponse<List<CartGoodsBase>>> response) {
                dialogs.dismiss();
                if (state == 1 || state == 3) {
                    if (groupList != null) {
                        groupList.clear();
                    }
                    ptrl.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                if (state == 2) {
                    if (groupList != null) {
                        groupList.clear();
                    }
                    ptrl.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                BaseResponse<List<CartGoodsBase>> baseResponse = response.body();
                if (null != baseResponse) {
                    int status = baseResponse.getCode();
                    if (status == (MyConstant.SUCCESS)) {
                        List<CartGoodsBase> cartGoodsBaseList = baseResponse.getData();
                        if (null == cartGoodsBaseList || cartGoodsBaseList.isEmpty()) {// 无数据
                            stateLayout.showEmptyView("暂无数据");
                        } else {// 有数据
                            stateLayout.showContentView();
                            for (int i = 0; i < cartGoodsBaseList.size(); i++) {
                                CartGoodsBase cartGoods = cartGoodsBaseList.get(i);
                                if (state == 3) {
                                    cartGoods.setChecked(true);
                                } else {
                                    cartGoods.setChecked(false);
                                }
                                cartGoods.setNumber("1");
                                groupList.add(cartGoods);

                            }
                        }
                        totalMoney = "0.00";
                        tv_car_Allprice.setText(totalMoney);
                        tv_num.setVisibility(View.GONE);
                    } else {
                        ToastUtil.showToast(mContext, baseResponse.getMsg());
                    }
                    adapter.notifyDataSetChanged();
                } else {//
                    adapter.notifyDataSetChanged();
                    try {
                        String error = response.errorBody().string();
                        BaseResponse errorResponse = GsonUtil.GsonToBean(error, BaseResponse.class);
                        startLogin(errorResponse);
                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CartGoodsBase>>> arg0,
                                  Throwable arg1) {
                dialogs.dismiss();

                if (state == 1) {
                    dialogs.dismiss();
                }
                if (state == 2) {
                    // 刷新完成调用
                    ptrl.refreshFinish(PullToRefreshLayout.FAIL);
                }
                ToastUtil.showToast(mContext, "网络状态不佳,请检查您的网络设置");
            }
        });
    }


    private String getGoodsIds() {
        StringBuilder sb = new StringBuilder();
        for (CartGoodsBase goods : checkList) {
            sb.append(goods.getId()).append(",");
        }
        String result = sb.deleteCharAt(sb.length() - 1).toString();
        return result;
    }

    /**
     * 删除提示
     *
     * @param goodsIds
     */
    private void showDeleteCartDialog(final String goodsIds) {
        DialogConfirm alert = new DialogConfirm();
        alert.setListener(new DialogConfirm.OnOkCancelClickedListener() {
            @Override
            public void onClick(boolean isOkClicked) {
                if (isOkClicked) {//
                    deleteCart(goodsIds);
                }

            }
        });
        alert.showDialog(getActivity(), getResources().getString(R.string.tishi_78), "确定", "取消");
    }


    /**
     * 删除购物车数据
     */
    private void deleteCart(String fgoodsShopcarIds) {
        dialogs.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<Object>> call = userBiz.delCartGoodsBat(token, fgoodsShopcarIds);
        call.enqueue(new HttpCallBack<BaseResponse<Object>>() {

            @Override
            public void onResponse(Call<BaseResponse<Object>> arg0,
                                   Response<BaseResponse<Object>> response) {
                dialogs.dismiss();
                BaseResponse<Object> baseResponse = response.body();
                if (null != baseResponse) {
                    int status = baseResponse.getCode();
                    if (status == (MyConstant.SUCCESS)) {// 删除成功
                        totalMoney = "0.00";
                        tv_car_Allprice.setText(totalMoney);
                        checkList.clear();
                        getShoppingCart(2);// 刷新
                    } else {
                        String msg = baseResponse.getMsg();
                        ToastUtil.showToast(mContext, msg);
                    }
                } else {
                    try {
                        String error = response.errorBody().string();
                        BaseResponse errorResponse = GsonUtil.GsonToBean(error, BaseResponse.class);
                        startLogin(errorResponse);
                    } catch (Exception e) {

                    }

                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Object>> arg0,
                                  Throwable arg1) {
                dialogs.dismiss();
                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    private void startLogin(BaseResponse errorResponse) {
        try {
            if (errorResponse.getCode() == (MyConstant.T_ERR_AUTH) || errorResponse.getCode() == MyConstant.T_LOGIN_ERR) {//token过期
                MyConstant.HASLOGIN = false;
                SPUtils.remove(mContext, "login");
                SPUtils.remove(mContext, "token");
                Intent intent = new Intent(mContext, LoginActivity.class);
                startActivity(intent);
            } else {
                MyApplication.getInstance().showShortToast("服务器连接失败");
            }
        } catch (Exception e) {

        }
    }


    private void generateOrder() {
        StringBuilder sb = new StringBuilder();
        ArrayList<OrderGoods> orderList = new ArrayList<OrderGoods>();
        OrderGoods orderGoods;
        for (CartGoodsBase cartGoodsBase : checkList) {
            sb.append(cartGoodsBase.getGoods_attr_id()).append(",");
            orderGoods = new OrderGoods(cartGoodsBase.getGoods_attr_id(), cartGoodsBase.getNumber());
            orderList.add(orderGoods);
        }
        final String goods_attr_id = sb.deleteCharAt(sb.length() - 1).toString();
        final String queryId = GsonUtil.toJsonString(orderList);
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        final int goods_type = groupList.get(0).getGoods_type();
        Call<BaseResponse<List<OrderCartBase>>> call = userBiz.generateOrder(token, goods_type, goods_attr_id, queryId, "");
        call.enqueue(new HttpCallBack<BaseResponse<List<OrderCartBase>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<OrderCartBase>>> arg0,
                                   Response<BaseResponse<List<OrderCartBase>>> response) {

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
                        Intent intent = new Intent(mContext, SubmitCarOrderActivity.class);
                        intent.putExtra("data", data);
                        intent.putExtra("goods_type", goods_type);
                        intent.putExtra("from_type", 1);
                        intent.putExtra("goods_attr_id", goods_attr_id);
                        intent.putExtra("queryId", queryId);
                        startActivity(intent);
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<OrderCartBase>>> arg0, Throwable arg1) {

                ToastUtil.showToast(mContext, "连接服务器失败,请检查您的网络状态");
            }
        });
    }


    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        getShoppingCart(2);

    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {

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
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            getShoppingCart(1);
        } else {//未登录
            MyConstant.HASLOGIN = false;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(ToCarMsgEvent messageEvent) {
        getShoppingCart(1);

    }


    @Override
    public void onClick(View item, View widget, int position, int which) {
        CartGoodsBase cartGoodsBase = groupList.get(position);
        TextView edit_num = (TextView) item.findViewById(R.id.tv_buy_num);
        String number = edit_num.getText().toString();
        int num;
        if (!StringUtils.isBlank(number)) {
            num = Integer.valueOf(number);
        } else {
            num = 1;
        }
        switch (which) {
            case R.id.tv_num_jian://减
                if (num == 1) {
                    num = 1;
                    ToastUtil.showToast(mContext, "不能再减了");
                } else {
                    num--;
                }
                cartGoodsBase.setNumber(num + "");
                adapter.notifyDataSetChanged();
                break;
            case R.id.tv_num_jia://加
                num++;
                cartGoodsBase.setNumber(num + "");
                adapter.notifyDataSetChanged();
                break;
            case R.id.cb_check://选择框
                boolean isChecked = cartGoodsBase.isChecked();
                cartGoodsBase.setChecked(!isChecked);
                adapter.notifyDataSetChanged();
                reFlashView();
                break;
            default:
                break;
        }
    }


    public void reFlashView() {
        try {
            itemCheckNumber = 0;
            checkList.clear();
            totalMoney = "0.00";
            for (CartGoodsBase bean : groupList) {
                boolean isChecked = bean.isChecked();
                if (isChecked) {
                    itemCheckNumber++;
                    checkList.add(bean);
                    totalMoney = CompuUtils.add(totalMoney, bean.getPrice_cost()).toString();
                }
            }
            if (itemCheckNumber == groupList.size()) {
                isSelectAll = true;
            } else {
                isSelectAll = false;
            }
            tv_car_Allprice.setText(totalMoney);
            cb_car_all.setChecked(isSelectAll);
            itemCheckNumber = checkList.size();
            if (itemCheckNumber != 0) {
                tv_num.setText("(" + itemCheckNumber + ")");
                tv_num.setVisibility(View.VISIBLE);
            } else {
                tv_num.setVisibility(View.GONE);
            }
        } catch (Exception e) {

        }

    }


}
