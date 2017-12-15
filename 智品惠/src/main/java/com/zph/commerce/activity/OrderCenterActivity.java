package com.zph.commerce.activity;

import android.os.Bundle;
import android.view.View;

import com.zph.commerce.R;
import com.zph.commerce.widget.TopNvgBar5;

import org.xutils.view.annotation.Event;
import org.xutils.x;

/**
 * Created by 24448 on 2017/10/20.
 */

public class OrderCenterActivity extends BaseActivity{




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_center);
        x.view().inject(this);
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
    }
    @Event({R.id.rl_shop_order,R.id.rl_wisdom_order})
    private void click(View view) {
        switch (view.getId()){
            case R.id.rl_wisdom_order://智品订单
                startActivity(WisdomOrderActivity.class);
                break;
            case R.id.rl_shop_order://商城订单
                startActivity(MailOrderActivity.class);
                break;
            default:
                break;
        }
    }
    @Override
    protected void initEvents() {

    }

}
