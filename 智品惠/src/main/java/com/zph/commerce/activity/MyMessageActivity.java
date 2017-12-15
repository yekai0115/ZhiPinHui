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
import com.zph.commerce.model.Message;
import com.zph.commerce.utils.EncodeUtils;
import com.zph.commerce.utils.SPUtils;
import com.zph.commerce.utils.StringUtils;
import com.zph.commerce.utils.ToastUtil;
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

public class MyMessageActivity extends BaseActivity implements PullLayout.OnRefreshListener{

    @ViewInject(R.id.list_message)
    private PullableListView listMessage;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    @ViewInject(R.id.product_refresh_view)
    private PullLayout layout;

    private int refreshCount = 1;
    private boolean pull = false;
    private boolean isLastRecord = false;

    @ViewInject(R.id.stateLayout)
    private StateLayout stateLayout;
    /**
     * 上下文
     **/
    private Context mContext;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_message_list);
        mContext = this;
        x.view().inject(this);
        // 注册事件
        EventBus.getDefault().register(this);
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
        layout.setOnRefreshListener(this);
        messageAdapter = new MessageAdapter();
        listMessage.setAdapter(messageAdapter);
        stateLayout.setEmptyAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryMessageList();
            }
        });
        stateLayout.setErrorAction(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryMessageList();
            }
        });
    }

    @Override
    protected void initEvents() {
        messageList.clear();
        queryMessageList();
    }



    public void queryMessageList(){
        dialog.show();
        APIService userBiz = RetrofitWrapper.getInstance().create(
                APIService.class);
        Call<BaseResponse<List<Message>>> call = userBiz.myMessage(token);
        call.enqueue(new HttpCallBack<BaseResponse<List<Message>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Message>>> arg0,
                                   Response<BaseResponse<List<Message>>> response) {
                if (dialog.isShowing()){dialog.dismiss();}
                super.onResponse(arg0,response);
                BaseResponse<List<Message>> baseResponse = response.body();
                if (null != baseResponse) {
                    int code = baseResponse.getCode();
                    List<Message> data = baseResponse.getData();
                    if (data !=null && data.size()>0){
                        listMessage.setPullUp(false);
                        messageList.addAll(data);
                        stateLayout.showContentView();
                        if (messageList.size() < 10){
                            isLastRecord = true;
                            listMessage.setPullUp(true);
                        }
                    }else{
                        if (pull){
                            isLastRecord = true;
                            listMessage.setPullUp(true);
                        }else{
                            stateLayout.showEmptyView();
                            stateLayout.showEmptyView("暂无数据");
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Message>>> arg0,
                                  Throwable arg1) {
                ToastUtil.showToast(mContext,"服务器连接失败,请检查您的网络状态");
            }
        });
    }
    class MessageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public Object getItem(int position) {
            return messageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null){
                convertView = View.inflate(MyMessageActivity.this,R.layout.adapter_platform_message_list,null);
                holder = new ViewHolder();
                holder.pMesTitle = (HandyTextView) convertView.findViewById(R.id.palt_mes_title);
                holder.pMesTime = (HandyTextView) convertView.findViewById(R.id.palt_mes_time);
                holder.pMesContent = (HandyTextView) convertView.findViewById(R.id.palt_mes_content);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                holder.pMesTitle.setText(messageList.get(position).getTitle());
                holder.pMesTime.setText(messageList.get(position).getAdd_time());
                holder.pMesContent.setText(messageList.get(position).getContent());
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
        messageList.clear();
        initpullView();
        messageAdapter.notifyDataSetChanged();
        queryMessageList();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullLayout pullToRefreshLayout) {
        // 加载操作
        if (!isLastRecord) {
            refreshCount++;
            pull = true;
            queryMessageList();
        }
        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
    }
    private void initpullView(){
        refreshCount = 1;
        isLastRecord = false;
        listMessage.setPullUp(false);
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
            queryMessageList();
        }else {
            finish();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
