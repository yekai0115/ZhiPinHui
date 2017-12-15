package com.zph.commerce.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zph.commerce.R;


/**
 * Created by StormShadow on 2017/3/18.
 * Knowledge is power.
 */
public class ShareDialog {

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity, R.style.shareDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.share_layout);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final LinearLayout ll_wx = (LinearLayout) dialog.findViewById(R.id.ll_wx);
        final LinearLayout ll_pyq = (LinearLayout) dialog.findViewById(R.id.ll_pyq);
        final LinearLayout ll_qq = (LinearLayout) dialog.findViewById(R.id.ll_qq);
        final LinearLayout ll_kj = (LinearLayout) dialog.findViewById(R.id.ll_kj);
        final TextView tv_quxiao = (TextView) dialog.findViewById(R.id.tv_quxiao);
        final View close_view = (View) dialog.findViewById(R.id.close_view);
        // 从底部弹出
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER | Gravity.BOTTOM);
        lp.x = 0;
        lp.y = 0;
        dialogWindow.setAttributes(lp);
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        RelativeLayout lLayout_bg = (RelativeLayout) dialog.findViewById(R.id.rl_share);
        // 调整dialog背景大小
        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int)
                (display.getWidth()), WindowManager.LayoutParams.MATCH_PARENT));


        ll_wx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClick(ll_wx.getId());
                }
                dialog.dismiss();
            }
        });
        ll_pyq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClick(ll_pyq.getId());
                }
                dialog.dismiss();
            }
        });

        ll_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClick(ll_qq.getId());
                }
                dialog.dismiss();
            }
        });

        ll_kj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClick(ll_kj.getId());
                }
                dialog.dismiss();
            }
        });
        tv_quxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });
        close_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private OnOkCancelClickedListener listener;

    public void setListener(OnOkCancelClickedListener listener) {
        this.listener = listener;
    }

    public interface OnOkCancelClickedListener {
        void onClick(int id);
    }
}
