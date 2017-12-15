package com.zph.commerce.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zph.commerce.R;
import com.zph.commerce.api.APIService;
import com.zph.commerce.api.RetrofitWrapper;
import com.zph.commerce.bean.BaseResponse;
import com.zph.commerce.constant.MyConstant;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.http.HttpCallBack;
import com.zph.commerce.model.AmountDetail;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.view.HandyTextView;
import com.zph.commerce.view.pullableview.PullableListView;
import com.zph.commerce.view.pulltorefresh.PullLayout;
import com.zph.commerce.view.pulltorefresh.PullToRefreshLayout;
import com.zph.commerce.view.statelayout.StateLayout;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by 24448 on 2017/10/20.
 */

public class AmountDetailActivity extends BaseActivity implements PullLayout.OnRefreshListener{


    @ViewInject(R.id.amount_detail_lv)
    private PullableListView amountDetailLv;

    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;

    @ViewInject(R.id.product_refresh_view)
    private PullLayout layout;
    private String token;
    private boolean pull = false;
    private boolean isLastRecord = false;
    private int refreshCount = 1;
    /**
     * 上下文
     **/
    private Context mContext;
    private List<AmountDetail> amountDetailList = new ArrayList<>();

    private AmountDetaileAdapter amountDetaileAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount_details);
        x.view().inject(this);
        // 注册事件
        EventBus.getDefault().register(this);
        mContext = this;
        initViews();
        initDialog();
        initEvents();
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
        token = (String) SPUtils.get(mContext, "token", "");
        token = EncodeUtils.base64Decode2String(token);
//        layout = (PullLayout)findViewById(R.id.product_refresh_view);
//        listMessage = (PullableListView)findViewById(R.id.list_message);
//        noListShow = (LinearLayout)findViewById(R.id.no_list_show);
        layout.setOnRefreshListener(this);
        amountDetaileAdapter = new AmountDetaileAdapter();
        amountDetailLv.setAdapter(amountDetaileAdapter);
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateLayout.showProgressView();
                queryAmountDetailList();
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateLayout.showProgressView();
                queryAmountDetailList();
            }
        });
    }

    @Override
    protected void initEvents() {
        amountDetailList.clear();
        queryAmountDetailList();
    }

    public void queryAmountDetailList(){
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<AmountDetail>>> call = userBiz.activeFriendLog(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<AmountDetail>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<AmountDetail>>> arg0,
                                   Response<BaseResponse<List<AmountDetail>>> response) {
                if (dialog.isShowing()){dialog.dismiss();}
                super.onResponse(arg0,response);
                BaseResponse<List<AmountDetail>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    List<AmountDetail> data = baseResponse.getData();
                    if (data !=null && data.size()>0){
                        amountDetailLv.setPullUp(false);
                        amountDetailList.addAll(data);
                        stateLayout.showContentView();
                        if (amountDetailList.size() < 10){
                            isLastRecord = true;
                            amountDetailLv.setPullUp(true);
                        }
                    }else{
                        if (pull){
                            isLastRecord = true;
                            amountDetailLv.setPullUp(true);
                        }else{
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        }
                    }
                    amountDetaileAdapter.notifyDataSetChanged();
                }else{
//                    startActivity(LoginActivity.class);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<AmountDetail>>> arg0,
                                  Throwable arg1) {
            }
        });
    }

    class AmountDetaileAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            return amountDetailList.size();
        }

        @Override
        public Object getItem(int position) {
            return amountDetailList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                convertView = View.inflate(AmountDetailActivity.this,R.layout.adapter_platform_message_list,null);
                holder = new ViewHolder();
                holder.pMesTitle = (HandyTextView) convertView.findViewById(R.id.palt_mes_title);
                holder.pMesTime = (HandyTextView) convertView.findViewById(R.id.palt_mes_time);
                holder.pMesContent = (HandyTextView) convertView.findViewById(R.id.palt_mes_content);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            try {
//                holder.pMesTitle.setText(messageList.get(position).getTitle());
//                holder.pMesTime.setText(messageList.get(position).getAdd_time());
//                holder.pMesContent.setText(messageList.get(position).getContent());
//                Glide.with(getActivity()).load(Constants.ALI_PRODUCT_LOAD+messageList.get(position).getHeadpic()).fitCenter()
//                        .placeholder(getResources().getDrawable(R.drawable.default_user)).error(getResources().getDrawable(R.drawable.default_user)).into(holder.cMesImage);
            }catch (Exception e){
                e.printStackTrace();
            }
            return convertView;
        }
    }

    public  class ViewHolder{
        private HandyTextView pMesTitle;
        private HandyTextView pMesTime;
        private HandyTextView pMesContent;

    }
    @Override
    public void onRefresh(PullLayout pullToRefreshLayout) {
        // 下拉刷新操作
        amountDetailList.clear();
        initpullView();
        amountDetaileAdapter.notifyDataSetChanged();
        queryAmountDetailList();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {
        // 加载操作
        if (!isLastRecord) {
            refreshCount++;
            pull = true;
            queryAmountDetailList();
        }
        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }
    private void initpullView(){
        refreshCount = 1;
        isLastRecord = false;
        amountDetailLv.setPullUp(false);
        pull = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMain(LoginMsgEvent messageEvent) {
//        int tage = messageEvent.getTage();
        String login = (String) SPUtils.get(mContext, "login", "");
        if (!StringUtils.isBlank(login) && login.equals(MyConstant.SUC_RESULT)) {// 已登录
            MyConstant.HASLOGIN = true;
            token = (String) SPUtils.get(mContext, "token", "");
            token = EncodeUtils.base64Decode2String(token);
            queryAmountDetailList();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
