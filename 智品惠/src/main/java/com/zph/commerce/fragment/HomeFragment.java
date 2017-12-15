package com.zph.commerce.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.squareup.picasso.Picasso;
import com.zph.commerce.R;
import com.zph.commerce.activity.CategoryGoodsListActivity;
import com.zph.commerce.activity.GoodsDetalActivity;
import com.zph.commerce.activity.LoginActivity;
import com.zph.commerce.activity.MyMessageActivity;
import com.zph.commerce.activity.MyWebViewActivity;
import com.zph.commerce.activity.SearchActivity;
import com.zph.commerce.adapter.BannerAdapter;
import com.zph.commerce.adapter.GridViewAdapter;
import com.zph.commerce.adapter.ProductsListAdapter;
import com.zph.commerce.adapter.ViewPagerAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BannerBase;
import com.zph.commerce.bean.BannerBean;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.Campaign;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.ToActivityMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.GoodsBean;
import com.zph.commerce.utils.DimenUtils;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableRefreshScrollView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.widget.MyListView;
import com.zph.commerce.widget.viewpager.LoopViewPager;
import com.zph.commerce.widget.viewpager.MaterialIndicator;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@ContentView(R.layout.main_fragment_home)
public class HomeFragment extends Fragment implements PullLayout.OnRefreshListener,PullableRefreshScrollView.OnScrollChangeListener{


    /**
     * 轮播
     */
    @ViewInject(R.id.ve_pager)
    private LoopViewPager pager;

    @ViewInject(R.id.bannerIndicator)
    private MaterialIndicator bannerIndicator;

    /**
     * 活动banner
     */
    @ViewInject(R.id.img_banner)
    private ImageView img_banner;
    /**
     *
     */
    @ViewInject(R.id.lv_recommend)
    private MyListView lv_recommend;


    @ViewInject(R.id.refresh_view)
    private PullLayout refresh_view;

    @ViewInject(R.id.my_message)
    private ImageView my_message;

    @ViewInject(R.id.searcher_linear)
    private LinearLayout searcher_linear;

    @ViewInject(R.id.searcher_title_linear)
    private LinearLayout searcher_title_linear;

    @ViewInject(R.id.mScrollView)
    private PullableRefreshScrollView mScrollView;

    @ViewInject(R.id.introduce_banner1)
    private ImageView introduce_banner1;

    @ViewInject(R.id.introduce_banner2)
    private ImageView introduce_banner2;

    @ViewInject(R.id.introduce_banner3)
    private ImageView introduce_banner3;


    @ViewInject(R.id.viewpager)
    private ViewPager mPager;

    @ViewInject(R.id.ll_dot)
    private LinearLayout mLlDot;



    private View mRootView;


    private String token;


    /**
     * 上下文
     **/
    private Context mContext;
    private BannerAdapter bannerAdapter;
    private ProductsListAdapter adapter;
    private int fadingHeight = 500; // 当ScrollView滑动到什么位置时渐变消失（根据需要进行调整）
    private Drawable drawable; // 顶部渐变布局需设置的Drawable
    private static final int START_ALPHA = 0;//scrollview滑动开始位置
    private static final int END_ALPHA = 255;//scrollview滑动结束位置
    private List<GoodsBean> recommendBeanList = new ArrayList<>();


    private List<View> mPagerList;
    private List<BannerBean> mDatas;
    private LayoutInflater inflater;
    /**
     * 总的页数
     */
    private int pageCount;
    /**
     * 每一页显示的个数
     */
    private int pageSize = 4;
    /**
     * 当前显示的是第几页
     */
    private int curIndex = 0;

    public HomeFragment() {
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
        //      EventBus.getDefault().register(this);
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
        refresh_view.setOnRefreshListener(this);
        setWidget();
//        searcher_linear.getBackground().setAlpha(150);
        return mRootView;
    }

    private void setWidget() {
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        getBanner(1);
        getProductList(1);
        adapter = new ProductsListAdapter(mContext, recommendBeanList);
        lv_recommend.setAdapter(adapter);
        lv_recommend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GoodsBean goodsBean = recommendBeanList.get(i);
                String id = goodsBean.getGid();
                Intent intent = new Intent();
                intent.putExtra("productId", id);
                intent.setClass(mContext, GoodsDetalActivity.class);
                startActivity(intent);
            }
        });
//        mScrollView.setOnScrollChangeListener(this);
    }

    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        getBanner(2);
        getProductList(2);
    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {
        // 加载操作
        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消事件注册
        //    EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }


    /**
     * 查询
     */
    private void getBanner(final int state) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<BannerBase>> call = userBiz.getBanner();
        call.enqueue(new Callback<BaseResponse<BannerBase>>() {

            @Override
            public void onResponse(Call<BaseResponse<BannerBase>> arg0,
                                   Response<BaseResponse<BannerBase>> response) {
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.SUCCEED);
                }
                BaseResponse<BannerBase> baseResponse = response.body();
                if (baseResponse != null) {
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        if (state == 1) {
                            BannerBase data = baseResponse.getData();
                            if (data == null) return;
                            List<BannerBean> banner1 = data.getBanner1();
                            if (null == banner1 || banner1.size() == 0) return;
                            MyConstant.BANNER_PUBLIC_URL=data.getFronturl();
                            bannerAdapter = new BannerAdapter(mContext, banner1);
                            pager.setAdapter(bannerAdapter);
                            if (banner1.size() > 1) {
                                pager.setLooperPic(true);
                                pager.addOnPageChangeListener(bannerIndicator);
                                bannerIndicator.setAdapter(pager.getAdapter());
                            } else {
                                pager.setLooperPic(false);
                            }
                            List<BannerBean> banner2 = data.getBanner2();
                            if (null == banner2 || banner2.isEmpty()) return;
                            BannerBean bannerBean = banner2.get(0);
                            String banner = (data.getFronturl() + bannerBean.getLogo() + MyConstant.PIC_DPI2).trim();
//                            Glide.with(mContext).load(banner)
//                                    .fitCenter()
//                                    //  .override(width,DimenUtils.dip2px(context,130))
//                                    .placeholder(R.drawable.pic_nomal_loading_style)
//                                    .error(R.drawable.pic_nomal_loading_style)
//                                    .into(img_banner);
                            Picasso.with(mContext).load(banner)
                                    .resize(DimenUtils.getWidth(mContext), DimenUtils.dip2px(mContext,150))
                                       .centerInside()
                                    .config(Bitmap.Config.RGB_565)
                                    .placeholder(R.drawable.pic_nomal_loading_style)
                                    .error(R.drawable.pic_nomal_loading_style)
                                    .into(img_banner);
                            img_banner.setTag(bannerBean);
                            showIntroduce(data);
                            showclassification(data);
                            Campaign campaign=data.getActivity();
                            EventBus.getDefault().post(new ToActivityMsgEvent(campaign));
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<BaseResponse<BannerBase>> arg0, Throwable arg1) {
                ToastUtil.showToast(mContext, "服务器连接失败,请检查您的网络设置");
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.FAIL);
                }
            }
        });
    }

    private void showclassification(BannerBase data) {
        mDatas = data.getBanner3();
        if (null == mDatas || mDatas.isEmpty()) return;
        inflater = LayoutInflater.from(mContext);
        //总的页数=总数/每页数量，并取整
        pageCount = (int) Math.ceil(mDatas.size() * 1.0 / pageSize);
        mPagerList = new ArrayList<View>();
        for (int i = 0; i < pageCount; i++) {
            //每个页面都是inflate出一个新实例
            GridView gridView = (GridView) inflater.inflate(R.layout.gridview, mPager, false);
            gridView.setAdapter(new GridViewAdapter(mContext, mDatas, i, pageSize));
            mPagerList.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position + curIndex * pageSize;

                    BannerBean bannerBean = mDatas.get(position);
                    view.setTag(bannerBean);
                    imgClickAction(view);
                }
            });
        }
        //设置适配器
        mPager.setAdapter(new ViewPagerAdapter(mPagerList));
        //设置圆点
        if (pageCount > 1) {
            setOvalLayout();
        }
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, 0);
            }
        });

    }

    private void showIntroduce(BannerBase data) {
        List<BannerBean> banner = data.getBanner4();
        if (null == banner || banner.isEmpty()) return;
        for (int i = 0; i < banner.size(); i++) {
            BannerBean bannerBean = banner.get(i);
            String bannerImg = (data.getFronturl() + bannerBean.getLogo() + MyConstant.PIC_DPI2).trim();
            switch (i) {
                case 0:
//                    Glide.with(mContext).load(bannerImg)
//                            .fitCenter()
//                            //  .override(width,DimenUtils.dip2px(context,130))
//                            .placeholder(R.drawable.pic_nomal_loading_style)
//                            .error(R.drawable.pic_nomal_loading_style)
//                            .into(introduce_banner1);
                    Picasso.with(mContext).load(bannerImg)
                            //    .centerInside()
                            .config(Bitmap.Config.RGB_565)
                            .placeholder(R.drawable.pic_nomal_loading_style)
                            .error(R.drawable.pic_nomal_loading_style)
                            .into(introduce_banner1);

                    introduce_banner1.setTag(bannerBean);
                    break;
                case 1:
//                    Glide.with(mContext).load(bannerImg)
//                            .fitCenter()
//                            //  .override(width,DimenUtils.dip2px(context,130))
//                            .placeholder(R.drawable.pic_nomal_loading_style)
//                            .error(R.drawable.pic_nomal_loading_style)
//                            .into(introduce_banner2);

                    Picasso.with(mContext).load(bannerImg)
                            //    .centerInside()
                            .config(Bitmap.Config.RGB_565)
                            .placeholder(R.drawable.pic_nomal_loading_style)
                            .error(R.drawable.pic_nomal_loading_style)
                            .into(introduce_banner2);


                    introduce_banner2.setTag(bannerBean);
                    break;
                case 2:
//                    Glide.with(mContext).load(bannerImg)
//                            .fitCenter()
//                            //  .override(width,DimenUtils.dip2px(context,130))
//                            .placeholder(R.drawable.pic_nomal_loading_style)
//                            .error(R.drawable.pic_nomal_loading_style)
//                            .into(introduce_banner3);

                    Picasso.with(mContext).load(bannerImg)
                            //    .centerInside()
                            .config(Bitmap.Config.RGB_565)
                            .placeholder(R.drawable.pic_nomal_loading_style)
                            .error(R.drawable.pic_nomal_loading_style)
                            .into(introduce_banner3);

                    introduce_banner3.setTag(bannerBean);
                    break;
            }

        }
    }

    /**
     * 查询
     */
    private void getProductList(final int state) {
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<GoodsBean>>> call = userBiz.getMall();
        call.enqueue(new HttpCallBack<BaseResponse<List<GoodsBean>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<GoodsBean>>> arg0,
                                   Response<BaseResponse<List<GoodsBean>>> response) {
                super.onResponse(arg0, response);
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.SUCCEED);
                }
                BaseResponse<List<GoodsBean>> baseResponse = response.body();
                if (baseResponse != null) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        recommendBeanList = baseResponse.getData();
                        if (recommendBeanList == null) return;
                        adapter.updateListview(recommendBeanList);
                        setListviewHeight(lv_recommend);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<GoodsBean>>> arg0, Throwable arg1) {
                ToastUtil.showToast(mContext, "服务器连接失败,请检查您的网络设置");
                if (state == 2) {
                    refresh_view.refreshFinish(PullLayout.FAIL);
                }
            }
        });
    }


        @Override
        public void onScrollChange(PullableRefreshScrollView view, int x, int y, int oldx, int oldy) {
            if (y > fadingHeight) {
                y = fadingHeight; // 当滑动到指定位置之后设置颜色为纯色，之前的话要渐变---实现下面的公式即可
                drawable = getResources().getDrawable(R.color.white);
                drawable.setAlpha(END_ALPHA);
                searcher_title_linear.setBackgroundDrawable(drawable);
                my_message.setBackground(getResources().getDrawable(R.drawable.home_news_black));
//                my_message.setBackgroundDrawable(getResources().getDrawable(R.drawable.home_news1));
            } else if (y < 0) {
                y = 0;
            } else {
                drawable = getResources().getDrawable(R.color.toming);
                drawable.setAlpha(y * (END_ALPHA - START_ALPHA) / fadingHeight
                        + START_ALPHA);
//                drawable.setAlpha(START_ALPHA);

                searcher_title_linear.setBackgroundDrawable(drawable);
                my_message.setBackground(getResources().getDrawable(R.drawable.home_news));

//                my_message.setBackgroundDrawable(getResources().getDrawable(R.drawable.home_news));

            }


        }

    @Event({R.id.my_message, R.id.searcher_linear, R.id.introduce_banner1, R.id.introduce_banner2, R.id.introduce_banner3, R.id.img_banner})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.my_message://我的消息
                if (MyConstant.HASLOGIN) {
                    intent = new Intent(getActivity(), MyMessageActivity.class);
                } else {
                    intent = new Intent(getActivity(), LoginActivity.class);

                }
                startActivity(intent);
                break;
            case R.id.searcher_linear://搜索
                intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.introduce_banner1://
                imgClickAction(introduce_banner1);
                break;
            case R.id.introduce_banner2://
                imgClickAction(introduce_banner2);
                break;
            case R.id.introduce_banner3://
                imgClickAction(introduce_banner3);
                break;
            case R.id.img_banner://
                imgClickAction(img_banner);
                break;
            default:
                break;
        }
    }

    private void imgClickAction(View view) {
        BannerBean bannerBean = (BannerBean) view.getTag();
        int fast_way = bannerBean.getFast_way();
        String site_url = bannerBean.getSite_url();
        String jump_id = bannerBean.getJump_id();
        String name = bannerBean.getName();
        if (fast_way == 1) {
            return;
        } else if (fast_way == 2) {//商品详情
            if (StringUtils.isBlank(jump_id)) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("productId", jump_id);
            intent.setClass(mContext, GoodsDetalActivity.class);
            startActivity(intent);
        } else if (fast_way == 3) {//跳转html
            Intent intent = new Intent();
            intent.putExtra("site_url", site_url);
            intent.putExtra("name", name);
            intent.setClass(mContext, MyWebViewActivity.class);
            startActivity(intent);
        } else if (fast_way == 4) {//商品列表
            Intent intent = new Intent();
            intent.putExtra("jump_id", jump_id);
            intent.putExtra("name", name);
            intent.putExtra("type", 1);
            intent.setClass(mContext, CategoryGoodsListActivity.class);
            startActivity(intent);
        } else if (fast_way == 5) {

        }


    }


    /**
     * 自动匹配listview的高度
     *
     * @param
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }

    /**
     * 设置圆点
     */
    public void setOvalLayout() {
        if (mLlDot.getChildCount()==pageCount){
            mLlDot.removeAllViews();
        }
        for (int i = 0; i < pageCount; i++) {
            mLlDot.addView(inflater.inflate(R.layout.dot, null));
        }
        // 默认显示第一页
        mLlDot.getChildAt(0).findViewById(R.id.v_dot)
                .setBackgroundResource(R.drawable.dot_selected);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int position) {
                // 取消圆点选中
                mLlDot.getChildAt(curIndex)
                        .findViewById(R.id.v_dot)
                        .setBackgroundResource(R.drawable.dot_normal);
                // 圆点选中
                mLlDot.getChildAt(position)
                        .findViewById(R.id.v_dot)
                        .setBackgroundResource(R.drawable.dot_selected);
                curIndex = position;
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }
}
