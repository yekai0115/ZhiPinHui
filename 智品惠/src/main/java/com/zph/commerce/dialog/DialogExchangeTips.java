package com.zph.commerce.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.zph.commerce.R;


/**
 * Created by StormShadow on 2017/3/18.
 * Knowledge is power.
 */
public class DialogExchangeTips {

    public void showDialog(Activity activity, String msg,int type){
        final Dialog dialog = new Dialog(activity, R.style.destionStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_exchange_tips);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final TextView text = (TextView) dialog.findViewById(R.id.dialog_confirm_message);
        if(type==1){//货款
            text.setText("您有"+msg+"货款等待好友确认");
        }else{//鼓励积分
            text.setText("您有"+msg+"鼓励积分等待好友确认");
        }

        TextView okBtn = (TextView) dialog.findViewById(R.id.dialog_confirm_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    listener.onClick(true);
                }
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
        void onClick(boolean isOkClicked);
    }
}
