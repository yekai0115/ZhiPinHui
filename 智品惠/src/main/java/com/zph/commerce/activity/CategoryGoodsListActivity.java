package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.CatergoryGoodslistAdapter;
import com.zph.commerce.adapter.HorizonScrolAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.Catergory;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.HorizontalScrollMenu;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 * 描述 ：商品分类商品列表
 */
public class CategoryGoodsListActivity extends BaseActivity implements
        PullToRefreshLayout.OnRefreshListener {


    @ViewInject(R.id.top_nvg_bar)
    private  TopNvgBar5 topNvgBar;


    @ViewInject(R.id.refresh_view)
    private PullToRefreshLayout refresh_view;

    /**
     * 商品列表
     */
    @ViewInject(R.id.lv_goods)
    private PullableListView lv_goods;

    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;


    @ViewInject(R.id.hsm_container)
    private HorizontalScrollMenu hsm_container;

    /**
     * 上下文
     **/
    private Context mContext;
    /**
     * 分类数据
     **/
    private List<Catergory> catergoryList = new ArrayList<Catergory>();
    /**
     * 商品数据
     **/
    private List<GoodsBean> catergoryGoodsList = new ArrayList<GoodsBean>();

    /*商品列表适配器*/
    private CatergoryGoodslistAdapter adapter;
    /**
     * 小分类id
     */
    private String jump_id;
    /**
     * 大分类id
     */
    private String parent_id;
    private String name;
    private int type;
    private String jump_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_goods_list);
        mContext = this;
        x.view().inject(this);
        setWidget();
        initDialog();
        initViews();
        adapter = new CatergoryGoodslistAdapter(mContext, catergoryGoodsList);
        lv_goods.setAdapter(adapter);
        lv_goods.canPullUp = false;
        refresh_view.setOnRefreshListener(this);

    }


    private void setWidget() {
        topNvgBar.setMyOnClickListener(new TopNvgBar5.MyOnClickListener() {
            @Override
            public void onLeftClick() {

                finish();
            }

            @Override
            public void onRightClick() {

            }
        });
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  stateLayout.showProgressView();
                if(null==catergoryList||catergoryList.isEmpty()){
                    goodCategaryList(1);
                }else{
                    goodCategaryGoodsList(1, jump_id);
                }
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  stateLayout.showProgressView();
                if(null==catergoryList||catergoryList.isEmpty()){
                    goodCategaryList(1);
                }else{
                    goodCategaryGoodsList(1, jump_id);
                }
            }
        });

        lv_goods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                GoodsBean goodsBean=(GoodsBean)adapter.getItem(i);
                String gid=goodsBean.getGid();
                Intent intent = new Intent();
                intent.putExtra("productId", gid);
                intent.setClass(mContext, GoodsDetalActivity.class);
                startActivity(intent);
            }
        });


    }

    private ArrayList<String> scrolListName;

    @Override
    protected void initViews() {
        Intent intent = getIntent();
        type = intent.getIntExtra("type", 1);
        name = intent.getStringExtra("name");
        topNvgBar.setTitle(name);
        if (type == 1) {//先查询分类
            parent_id= intent.getStringExtra("jump_id");
            goodCategaryList(1);
        } else {//从分类进来，不用查询分类
            jump_id = intent.getStringExtra("jump_id");
            jump_name=intent.getStringExtra("jump_name");
            Bundle bundle = intent.getBundleExtra("catergory");
            catergoryList = (List<Catergory>) bundle.getSerializable("CatergoryList");
            setMenuAdapter();
            goodCategaryGoodsList(1, jump_id);
        }
    }

    private void setMenuAdapter(){

        hsm_container.setAdapter(new HorizonScrolAdapter() {
            @Override
            public List<String> getMenuItems() {
                scrolListName = new ArrayList<String>();
                for (int i = 0; i < catergoryList.size(); i++) {
                    scrolListName.add(catergoryList.get(i).getName());
                }
                return scrolListName;
            }

            @Override
            public List<View> getContentViews() {
                {
                    List<View> views = new ArrayList<View>();
                    for (String str : scrolListName) {
                        View v = LayoutInflater.from(CategoryGoodsListActivity.this).inflate(R.layout.content_view, null);
                        TextView tv = (TextView) v.findViewById(R.id.tv_content);
                        tv.setText(str);
                        views.add(v);
                    }
                    return views;
                }
            }

            @Override
            public void onPageChanged(int position, boolean visitStatus) {
                jump_id=catergoryList.get(position).getCid();
                goodCategaryGoodsList(1, jump_id);
            }
        });
        if(!StringUtils.isBlank(jump_name)){
            hsm_container.scrollTo(jump_name);
        }

    }



    @Override
    protected void initEvents() {

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
        super.onDestroy();


    }


    /**
     * 获取分类列表
     *
     * @param state
     */
    private void goodCategaryList(final int state) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<Catergory>>> call = userBiz.goodCategary(parent_id);
        call.enqueue(new HttpCallBack<BaseResponse<List<Catergory>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Catergory>>> arg0,
                                   Response<BaseResponse<List<Catergory>>> response) {
                dialog.dismiss();
                if (state == 2) {
                    refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                super.onResponse(arg0, response);
                BaseResponse<List<Catergory>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        catergoryList = baseResponse.getData();
                        if (null == catergoryList || catergoryList.isEmpty()) {
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        } else {
                            stateLayout.showContentView();
                            setMenuAdapter();
                        }
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Catergory>>> arg0, Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView();
                stateLayout.showErrorView("网络错误");
                if (state == 2) {
                    // 刷新完成调用
                    refresh_view.refreshFinish(PullToRefreshLayout.FAIL);
                }

                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    /**
     * 获取商品列表
     *
     * @param state
     */
    private void goodCategaryGoodsList(final int state, String category_id) {
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<GoodsBean>>> call = userBiz.goodCategaryList(category_id);
        call.enqueue(new HttpCallBack<BaseResponse<List<GoodsBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<GoodsBean>>> arg0,
                                   Response<BaseResponse<List<GoodsBean>>> response) {
                dialog.dismiss();
                if (state == 2) {
                    if (catergoryGoodsList != null) {
                        catergoryGoodsList.clear();
                    }
                    refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                if (state == 3) {
                    if (catergoryGoodsList != null) {
                        catergoryGoodsList.clear();
                    }
                }
                super.onResponse(arg0, response);
                BaseResponse<List<GoodsBean>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {//
                        catergoryGoodsList = baseResponse.getData();
                        if (null == catergoryGoodsList || catergoryGoodsList.isEmpty()) {
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        } else {
                            stateLayout.showContentView();
                        }
                        adapter.updateListView(catergoryGoodsList);
                    } else {
                        ToastUtil.showToast(mContext, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<GoodsBean>>> arg0, Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView();
                stateLayout.showErrorView("网络错误");
                if (state == 2) {
                    // 刷新完成调用
                    refresh_view.refreshFinish(PullToRefreshLayout.FAIL);
                }

                ToastUtil.showToast(mContext, "网络状态不佳,请稍后再试");
            }
        });
    }


    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        if(null==catergoryList||catergoryList.isEmpty()){
            goodCategaryList(2);
        }else{
            goodCategaryGoodsList(2, jump_id);
        }

    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

    }
}
