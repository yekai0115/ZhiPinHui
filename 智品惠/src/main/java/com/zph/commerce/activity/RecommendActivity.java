package com.zph.commerce.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;

import com.zph.commerce.R;
import com.zph.commerce.application.MyApplication;
import com.zph.commerce.eventbus.LoginMsgEvent;
import com.zph.commerce.fragment.RecommendFrament;
import com.zph.commerce.widget.PagerSlidingTabStrip;
import com.zph.commerce.widget.TopNvgBar5;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;


/**
 * 好友管理页面
 */
public class RecommendActivity extends FragmentActivity {

    /**
     * PagerSlidingTabStrip的实例
     */
    @ViewInject(R.id.orderTabs)
    private PagerSlidingTabStrip tabs;

    @ViewInject(R.id.main_viewpager)
    private ViewPager pager;

    /**
     * 获取当前屏幕的密度
     */
    private DisplayMetrics dm;

    // 上下文
    private Context context;
    // intent
    private Intent intent;

    private int type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        MyApplication.addActivity(this);
        x.view().inject(this);
        context = this;
        intent = getIntent();
        dm = getResources().getDisplayMetrics();
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);
        setTabsValue();
        type = intent.getIntExtra("type", 0);
        pager.setCurrentItem(type, true);
        pager.setOffscreenPageLimit(0);
        initView();

    }





    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new LoginMsgEvent());
    }

    /**
     * 对PagerSlidingTabStrip的各项属性进行赋值。
     */
    private void setTabsValue() {
        // 设置Tab是自动填充满屏幕的
        tabs.setShouldExpand(true);
        // 设置Tab的分割线是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 设置Tab底部线的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0, dm));
        // 设置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, dm));
        // 设置Tab标题文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 14, dm));
        // 设置Tab Indicator的颜色
        tabs.setIndicatorColor(context.getResources().getColor(
                R.color.bg_main_bottom));
        // 设置选中Tab文字的颜色 (这是我自定义的一个方法)
        tabs.setSelectedTextColor(context.getResources().getColor(
                R.color.colorPrimary));
        // 取消点击Tab时的背景色
        tabs.setTabBackground(0);
    }

    private void initView(){
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



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监听返回键
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private String[] titles = { "直推好友", "间推好友"};

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            int status = position + 1;
            return RecommendFrament.newInstance(status);
        }

    }

}


