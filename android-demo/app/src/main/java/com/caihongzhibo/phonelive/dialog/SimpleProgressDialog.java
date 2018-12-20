package com.caihongzhibo.phonelive.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.caihongzhibo.phonelive.R;


public class SimpleProgressDialog extends AlertDialog {

    private TextView mTextView;

    public SimpleProgressDialog(Context context, String text) {
        super(context, R.style.MyDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        setView(view);
        mTextView = (TextView) view.findViewById(R.id.tipTextView);
        if (text != null) {
            mTextView.setText(text);
        }

        setCancelable(true); // 是否可以按“返回键”消失
        setCanceledOnTouchOutside(false); // 点击加载框以外的区域

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.CENTER);
        window.setAttributes(lp);
        window.setWindowAnimations(R.style.PopWindowAnimStyle);
    }

    public void setText(String text) {
        if (text != null) {
            mTextView.setText(text);
        }
    }
}
