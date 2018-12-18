package com.caihongzhibo.phonelive.activity;

import com.lzy.okgo.model.Response;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.http.JsonBean;
import com.caihongzhibo.phonelive.im.IMUtil;
import com.caihongzhibo.phonelive.utils.IntervalCountDown;
import com.caihongzhibo.phonelive.utils.JPushUtil;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.LoginUtil;
import com.caihongzhibo.phonelive.utils.SharedPreferencesUtil;

import cn.sharesdk.framework.ShareSDK;

/**
 * 启动页面
 */
public class LauncherActivity extends AbsActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    protected void main() {
        //开启倒计时
        startCountDown();
        //初始化http
        HttpUtil.init();
        //初始化极光推送
        JPushUtil.init();
        //初始化sharedSdk
        ShareSDK.initSDK(AppContext.sInstance);
        //初始化IM
        IMUtil.getInstance().init(IMUtil.JIM);
        AppConfig.getInstance().setLaunched(true);
    }

    /**
     * 启动定时器，3秒后跳转
     */
    private void startCountDown() {
        final int targetCount = 2;
        new IntervalCountDown(targetCount, new IntervalCountDown.Callback() {
            @Override
            public void callback(int count) {
                L.e("LauncherActivity 定时器-->" + count);
                if (count == targetCount) {
                    readUidAndToken();
                }
            }
        }).start();
    }

    /**
     * 从SharedPreferences中读取用户uid和token，
     * 如果有，验证uid和token
     * 如果没有，则跳转到登录页面
     */
    private void readUidAndToken() {
        String[] uidAndToken = SharedPreferencesUtil.getInstance().readUidAndToken();
        if (uidAndToken != null) {
            validateUidAndToken(uidAndToken[0], uidAndToken[1]);
        } else {
            L.e("不存在用户信息-->跳转到登录页面");
            LoginUtil.forwardLogin();
        }
    }

    /**
     * 验证uid和token是否过期
     */
    private void validateUidAndToken(final String uid, final String token) {
        HttpUtil.ifToken(uid, token, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    //token没有过期
                    AppConfig.getInstance().setUid(uid);
                    AppConfig.getInstance().setToken(token);
                    LoginUtil.startThridLibray();//启动三方库 IM 极光等
                    MainActivity.startMainActivity(LauncherActivity.this, LauncherActivity.this.getIntent().getBundleExtra("jpusheventBundle"));
                    finish();
                }
            }

            @Override
            public void onError(Response<JsonBean> response) {
                super.onError(response);
                AppConfig.getInstance().setUid(uid);
                AppConfig.getInstance().setToken(token);
                MainActivity.startMainActivity(LauncherActivity.this, LauncherActivity.this.getIntent().getBundleExtra("jpusheventBundle"));
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.IF_TOKEN);
        super.onDestroy();

    }
}
