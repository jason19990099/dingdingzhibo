package com.caihongzhibo.phonelive.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.ChargeActivity;
import com.caihongzhibo.phonelive.utils.DpUtil;

/**
 * Created by cxf on 2017/9/21.
 */

public class ChargeFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        Dialog dialog = new Dialog(mContext, R.style.dialog);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_charge, null);
        dialog.setContentView(mRootView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        View btnAli = mRootView.findViewById(R.id.btn_ali);
        if (1 == bundle.getInt("ali")) {
            btnAli.setOnClickListener(this);
        } else {
            btnAli.setVisibility(View.GONE);
        }
        View btnWx = mRootView.findViewById(R.id.btn_wx);
        if (1 == bundle.getInt("wx")) {
            btnWx.setOnClickListener(this);
        } else {
            btnWx.setVisibility(View.GONE);
        }
        mRootView.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ali:
                dismiss();
                ((ChargeActivity) mContext).aliPay();
                break;
            case R.id.btn_wx:
                dismiss();
                ((ChargeActivity) mContext).wxPay();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }


}
