package com.zph.commerce.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.adapter.RecommendAdapter;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.bean.RecomFriend;
import com.zph.commerce.bean.RecomFriendBase;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.dialog.LoadingDialog;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.ToastUtil;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.statelayout.StateLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


@ContentView(R.layout.fragment_friends_list)
public class RecommendFrament extends BaseFragment implements PullLayout.OnRefreshListener {

    private View mainView;
    private Context context;
    private int position;


    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.tv_friends_num)
    private TextView tv_friends_num;

    @ViewInject(R.id.lv_friends)
    private PullableListView lv_friends;
    @ViewInject(R.id.refresh_view)
    private PullLayout ptrl;

    private LoadingDialog dialog;

    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;

    private RecommendAdapter adapter;
    private List<RecomFriend> friendList = new ArrayList<RecomFriend>();
    private String token;

    public RecommendFrament() {
        super();
    }

    public static RecommendFrament newInstance(int position) {
        RecommendFrament orderFrament = new RecommendFrament();
        Bundle b = new Bundle();
        b.putInt("position", position);
        orderFrament.setArguments(b);
        return orderFrament;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        token = (String) SPUtils.get(context, "token", "");
        token = EncodeUtils.base64Decode2String(token);
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mainView == null) {
            mainView = x.view().inject(this, inflater, container);
            setWidget();
            ptrl.setOnRefreshListener(this);
            // ptrl.autoRefresh(); //去掉自动刷新

            context = getActivity();
            dialog = new LoadingDialog(context, R.style.dialog, "加载中...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            adapter = new RecommendAdapter(getActivity(), friendList);
            lv_friends.setAdapter(adapter);
            lv_friends.canPullUp=false;
            isPrepared = true;
            lazyLoad();
        }
        // 因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
        ViewGroup parent = (ViewGroup) mainView.getParent();
        if (parent != null) {
            parent.removeView(mainView);
        }
        return mainView;

    }

    private void setWidget() {
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFriendsList(1);
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFriendsList(1);
            }
        });
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }

        dialog.show();
        getFriendsList(1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        context = getActivity();// 这个必须这里初始化，要不会空指针


    }

    // 进入采购详情返回后刷新
    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * @param state
     */
    private void getFriendsList(final int state) {
//        stateLayout.showProgressView();
        APIService userBiz = RetrofitWrapper.getInstance().create(APIService.class);
        Call<BaseResponse<List<RecomFriendBase>>> call = userBiz.getFriends(token, position);
        call.enqueue(new HttpCallBack<BaseResponse<List<RecomFriendBase>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<RecomFriendBase>>> arg0,
                                   Response<BaseResponse<List<RecomFriendBase>>> response) {
                mHasLoadedOnce = true;   //没有这个每次滑动的时候都会刷新
                dialog.dismiss();
                if (state == 2) {
                    if (friendList != null) {
                        friendList.clear();
                    }
                    ptrl.refreshFinish(PullLayout.SUCCEED);
                }
                super.onResponse(arg0,response);
                BaseResponse<List<RecomFriendBase>> baseResponse = response.body();
                if (null != baseResponse) {
                    String desc = baseResponse.getMsg();
                    int retCode = baseResponse.getCode();
                    if (retCode == MyConstant.SUCCESS) {
                        List<RecomFriendBase> data = baseResponse.getData();
                        if (null == data || data.isEmpty()) {
                            stateLayout.showEmptyView("暂无数据");
                            ptrl.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                            tv_friends_num.setText("0");
                        } else {
                            stateLayout.showContentView();
                            ptrl.setVisibility(View.VISIBLE);
                            RecomFriendBase recomFriendBase=data.get(0);
                            List<RecomFriend>  items =recomFriendBase.getItems();
                            int refreNum=recomFriendBase.getRefreNum();
                            tv_friends_num.setText(""+refreNum);
                            adapter.updateListView(items,recomFriendBase.getHeadurl());
                        }
                    } else {
                        ToastUtil.showToast(context, desc);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RecomFriendBase>>> arg0,
                                  Throwable arg1) {
                dialog.dismiss();
                stateLayout.showErrorView("");
                if (state == 1) {
                    dialog.dismiss();
                }
                if (state == 2) {
                    // 刷新完成调用
                    ptrl.refreshFinish(PullLayout.FAIL);
                }
                ToastUtil.showToast(context, "网络状态不佳,请检查您的网络设置");
            }
        });
    }


    /**
     * 下拉
     */
    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        getFriendsList(2);

    }

    /**
     * 上拉
     */
    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
