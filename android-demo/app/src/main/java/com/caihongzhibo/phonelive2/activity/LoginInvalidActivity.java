package com.caihongzhibo.phonelive2.activity;

import android.view.View;
import android.widget.TextView;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.utils.LoginUtil;

/**
 * Created by cxf on 2017/10/9.
 * 登录失效的时候以dialog形式弹出的activity
 */

public class LoginInvalidActivity extends AbsActivity implements View.OnClickListener {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_invalid;
    }

    @Override
    protected void main() {
        TextView textView = (TextView) findViewById(R.id.content);
        String msg = getIntent().getStringExtra("msg");
        textView.setText(msg);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        finish();
        LoginUtil.logout();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
