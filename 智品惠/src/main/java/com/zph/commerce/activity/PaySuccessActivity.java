package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zph.commerce.R;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 购买成功
 */

public class PaySuccessActivity extends BaseActivity {


    @ViewInject(R.id.tv_order_money)
    private TextView tv_order_money;

    /**
     * 立即发货
     */
    @ViewInject(R.id.tv_fahuo)
    private TextView tv_fahuo;

    private String trade_id;
    private String pay_money;
    /**
     * 商品类型
     */
    private int goods_type;
    /**
     * 购买数量+销量
     */
    private int rank_id;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_success);
        x.view().inject(this);
        mContext = this;
        trade_id = getIntent().getStringExtra("trade_id");
        pay_money = getIntent().getStringExtra("pay_money");
        goods_type = getIntent().getIntExtra("goods_type", 1);
        rank_id = getIntent().getIntExtra("rank_id", 0);
        initViews();
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
        if (goods_type == 1) {
            tv_fahuo.setVisibility(View.GONE);
        } else {
            if (rank_id != 1) {//普通会员以上
                tv_fahuo.setVisibility(View.VISIBLE);
            } else {
                tv_fahuo.setVisibility(View.GONE);
            }
        }
        tv_order_money.setText(pay_money);

    }

    @Override
    protected void initEvents() {
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

    @Event({R.id.tv_shouye, R.id.tv_fahuo})
    private void click(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.tv_shouye://返回首页
                MyApplication.finishSingleActivityByClass(SearchActivity.class);
                MyApplication.finishSingleActivityByClass(CategoryGoodsListActivity.class);
                MyApplication.finishSingleActivityByClass(GoodsListActivity.class);
                finish();
                break;
            case R.id.tv_fahuo://立即发货
                startActivity(PickGoodsActivity.class);
                finish();
                break;
            default:
                break;
        }
    }
}
