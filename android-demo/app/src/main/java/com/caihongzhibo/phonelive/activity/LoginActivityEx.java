package com.caihongzhibo.phonelive.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.adapter.SharedSdkAdapter;
import com.caihongzhibo.phonelive.bean.ConfigBean;
import com.caihongzhibo.phonelive.bean.SharedSdkBean;
import com.caihongzhibo.phonelive.event.LoginSuccessEvent;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.LoginUtil;
import com.caihongzhibo.phonelive.utils.SharedSdkUitl;
import com.caihongzhibo.phonelive.utils.ToastUtil;
import com.caihongzhibo.phonelive.utils.ValidateUitl;
import com.caihongzhibo.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by cxf on 2017/8/5.
 * 登录页面
 */

public class LoginActivityEx extends AbsActivity {

    private EditText mPhoneNum;
    private EditText mPwd;
    private String mType;
    private static final int REGISTER_CODE = 100;
    public static final String LOGIN_PHONE_NUM = "phoneNum";
    public static final String LOGIN_PWD = "pwd";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login_phone;
    }

    @Override
    protected void main() {
        initView();
    }

    private void initView() {
        mPhoneNum = (EditText) findViewById(R.id.phone_num);
        mPwd = (EditText) findViewById(R.id.pwd);
        //登录即代表你同意《服务和隐私协议》
        TextView textView = (TextView) findViewById(R.id.login_text);
        textView.setText(Html.fromHtml(WordUtil.getString(R.string.login_text)
                + "<font color='#ffd350'>" + WordUtil.getString(R.string.login_text_2) + "</font>"
        ));
    }


    public void loginExClick(View view) {
        switch (view.getId()) {
            case R.id.register:
            case R.id.register2:
                startActivityForResult(new Intent(this, RegisterActivity.class), REGISTER_CODE);
                break;
            case R.id.login:
                login();
                break;
            case R.id.find_pwd:
                startActivity(new Intent(this, FindPwdActivity.class));
                break;
            case R.id.login_text:
                forwardHtml();
                break;
        }
    }

    private void forwardHtml() {
        String url = AppConfig.HOST + "/index.php?g=portal&m=page&a=index&id=4";
        Intent intent = new Intent(mContext, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void login() {
        String phoneNum = mPhoneNum.getText().toString();
        if ("".equals(phoneNum)) {
            mPhoneNum.setError(getString(R.string.phone_num_empty));
            mPhoneNum.requestFocus();
            return;
        }
        if (!ValidateUitl.validateMobileNumber(phoneNum)) {
            mPhoneNum.setError(getString(R.string.phone_num_error));
            mPhoneNum.requestFocus();
            return;
        }
        String pwd = mPwd.getText().toString();
        if ("".equals(pwd)) {
            mPwd.setError(getString(R.string.pwd_empty));
            mPwd.requestFocus();
            return;
        }
        HttpUtil.login(phoneNum, pwd, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                loginSuccess(code, msg, info);
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, getString(R.string.login_ing));
            }
        });
    }

    //登录成功！
    private void loginSuccess(int code, String msg, String[] info) {
        if (code == 0 && info.length > 0) {
            JSONObject obj = JSON.parseObject(info[0]);
            String uid = obj.getString("id");
            String token = obj.getString("token");
            L.e("登录成功--uid-->" + uid);
            L.e("登录成功--token-->" + token);
            LoginUtil.login(uid, token);
            String reg = obj.getString("isreg");
            if ("1".equals(reg)) {
                getRecommendList(reg, obj.getString("user_nicename"));
            } else {
                forwardMainActivity(reg, obj.getString("user_nicename"));
            }
        } else {
            ToastUtil.show(msg);
        }
    }

    private void forwardMainActivity(String reg, String name) {
        EventBus.getDefault().post(new LoginSuccessEvent());
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("isreg", reg);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }

    /**
     * 获取推荐列表
     */
    private void getRecommendList(final String reg, final String name) {
        HttpUtil.getRecommend(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        Intent intent = new Intent(mContext, RecommendActivity.class);
                        intent.putExtra("isreg", reg);
                        intent.putExtra("name", name);
                        intent.putExtra("recommend", Arrays.toString(info));
                        startActivity(intent);
                        finish();
                    } else {
                        forwardMainActivity(reg, name);
                    }
                } else {
                    forwardMainActivity(reg, name);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        SharedSdkUitl.getInstance().releaseShareListener();
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        HttpUtil.cancel(HttpUtil.LOGIN);
        HttpUtil.cancel(HttpUtil.GET_RECOMMEND);
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REGISTER_CODE && resultCode == RESULT_OK) {
            String phoneNum = intent.getStringExtra(LOGIN_PHONE_NUM);
            String pwd = intent.getStringExtra(LOGIN_PWD);
            mPhoneNum.setText(phoneNum);
            mPwd.setText(pwd);
        }
    }
}
