package com.zph.commerce.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.CatergoryAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.ParentCatergory;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.utils.ACache;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 一级分类
 */

@ContentView(R.layout.fragment_catergory)
public class CatergoryFragment extends Fragment implements PullToRefreshLayout.OnRefreshListener {
    @ViewInject(R.id.top_nvg_bar)
    private TopNvgBar5 topNvgBar;
    @ViewInject(R.id.catergory_first)
    private ListView lv_category;
    @ViewInject(R.id.category_refresh_view)
    private PullToRefreshLayout ptrl;
    @ViewInject(R.id.scrollView)
    private ScrollView scrollView;
    private Context mContext;
    private View mRootView;
    private CatergoryAdapter adapter;

    /**
     * 分类数据第一级
     */
    private List<ParentCatergory> arrayList = new ArrayList<ParentCatergory>();



    private ACache mCache;


    public CatergoryFragment() {
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
        setWidget();
        return mRootView;
    }

    private void setWidget() {
        topNvgBar.setLeftVisibility(View.GONE);
        mCache = ACache.get(mContext);

        ptrl.setOnRefreshListener(this);
        adapter = new CatergoryAdapter(mContext, arrayList);
        lv_category.setAdapter(adapter);
        lv_category.setFocusable(false);
        // ptrl.autoRefresh();//自动刷新
        scrollView.smoothScrollTo(0, 0);
        getCategoryListFromCache(1);



    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @SuppressWarnings("unchecked")
    private void getCategoryListFromCache(int state) {
        mCache.clear();
        BaseResponse<List<ParentCatergory>> baseResponse = (BaseResponse<List<ParentCatergory>>) mCache.getAsObject("first_catergory");
        if (null == baseResponse) {

            getCategoryChildlist(0, state);
        } else {
            arrayList = baseResponse.getData();
            adapter.updateListView(arrayList);
            setListviewHeight(lv_category);
            if (state == 2) {
                ptrl.refreshFinish(PullToRefreshLayout.SUCCEED);
            }
        }

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
     * 描述 ： 取得商品分类
     *
     * @param
     * @param
     */
    public void getCategoryChildlist(final int id, final int state) {

        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<ParentCatergory>>> call = userBiz.getCategoryChildlistRepo();
        call.enqueue(new Callback<BaseResponse<List<ParentCatergory>>>() {

            @Override
            public void onResponse(
                    Call<BaseResponse<List<ParentCatergory>>> arg0,
                    Response<BaseResponse<List<ParentCatergory>>> response) {
                if (state == 1) {
                    if (arrayList != null) {
                        arrayList.clear();
                    }
                }
                if (state == 2) {
                    if (arrayList != null) {
                        arrayList.clear();
                    }
                    ptrl.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
                BaseResponse<List<ParentCatergory>> baseResponse = response.body();
                if (null != baseResponse) {
                    int status = baseResponse.getCode();
                    if (status==(MyConstant.SUCCESS)) {
                        arrayList = baseResponse.getData();
                        mCache.put("first_catergory", baseResponse, 60 * 60 * 24);
                        adapter.updateListView(arrayList);
                        setListviewHeight(lv_category);
                    }  else {
                        String msg=baseResponse.getMsg();
                        ToastUtil.showToast(mContext, msg);
                        adapter.notifyDataSetChanged();
                    }

                }

            }

            @Override
            public void onFailure(Call<BaseResponse<List<ParentCatergory>>> arg0, Throwable arg1) {
                if (state == 2) {
                    // 刷新完成调用
                    ptrl.refreshFinish(PullToRefreshLayout.FAIL);
                }
            }
        });
    }

    /**自动匹配listview的高度
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) + 30;
        ((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        getCategoryListFromCache(2);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

    }

}
