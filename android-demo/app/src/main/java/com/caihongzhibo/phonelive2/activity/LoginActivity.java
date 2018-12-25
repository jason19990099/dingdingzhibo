package com.caihongzhibo.phonelive2.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.adapter.SharedSdkAdapter;
import com.caihongzhibo.phonelive2.bean.ConfigBean;
import com.caihongzhibo.phonelive2.bean.SharedSdkBean;
import com.caihongzhibo.phonelive2.event.LoginSuccessEvent;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.CommonCallback;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive2.utils.DialogUitl;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.LoginUtil;
import com.caihongzhibo.phonelive2.utils.SharedSdkUitl;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by cxf on 2017/8/5.
 * 登录页面
 */

public class LoginActivity extends AbsActivity implements OnItemClickListener<SharedSdkBean> {

    private String mType;
    private Dialog mLoginAuthDialog;
    private RecyclerView mRecyclerView;
    private View mOtherLogin;
    private static final int REGISTER_CODE = 100;
    public static final String LOGIN_PHONE_NUM = "phoneNum";
    public static final String LOGIN_PWD = "pwd";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void main() {
        initView();
        getConfig();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mOtherLogin = findViewById(R.id.other_login);
        //登录即代表你同意《服务和隐私协议》
        TextView textView = (TextView) findViewById(R.id.login_text);
        textView.setText(Html.fromHtml(WordUtil.getString(R.string.login_text)
                + "<font color='#ffd350'>" + WordUtil.getString(R.string.login_text_2) + "</font>"
        ));
        EventBus.getDefault().register(this);
    }

    private void getConfig() {
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                String[] loginTypes = bean.getLogin_type();
                if (loginTypes.length > 0) {
                    SharedSdkAdapter adapter = new SharedSdkAdapter(loginTypes, false, false);
                    adapter.setOnItemClickListener(LoginActivity.this);
                    mRecyclerView.setAdapter(adapter);
                } else {
                    mOtherLogin.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void loginClick(View view) {
        switch (view.getId()) {
            case R.id.login_text:
                forwardHtml();
                break;
            case R.id.btn_phone_login:
                phoneLoginActivity();
                break;
        }
    }

    private void forwardHtml() {
        String url = AppConfig.HOST + "/index.php?g=portal&m=page&a=index&id=4";
        Intent intent = new Intent(mContext, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void phoneLoginActivity() {
        startActivity(new Intent(mContext, LoginActivityEx.class));
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


    private void thirdLogin(String openid, PlatformDb platDB) {
        HttpUtil.loginByThird(openid, platDB.getUserName(), mType, platDB.getUserIcon(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                loginSuccess(code, msg, info);
            }

            @Override
            public void onFinish() {
                if (mLoginAuthDialog != null) {
                    mLoginAuthDialog.dismiss();
                }
            }
        });
    }


    //第三方登录回调
    private SharedSdkUitl.ShareListener mShareListener = new SharedSdkUitl.ShareListener() {
        @Override
        public void onSuccess(Platform platform) {
            ToastUtil.show(getString(R.string.auth_success));
            final PlatformDb platDB = platform.getDb();
            if (platDB.getPlatformNname().equals(Wechat.NAME)) {
                String openid = platDB.get("unionid");
                thirdLogin(openid, platDB);
            } else if (platDB.getPlatformNname().equals(QQ.NAME)) {
                //需要数据打通的时候用
//                HttpUtil.getQQLoginOpenid(platDB.getToken(), new StringCallback() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        String res = response.body();
//                        String openid = res.trim().substring(res.indexOf("unionid") + 10, res.indexOf("}") - 1);
//                        thirdLogin(openid, platDB);
//                    }
//                });

                //没有数据打通的时候时候用
                thirdLogin(platDB.getUserId(), platDB);
            }
        }

        @Override
        public void onError(Platform platform) {
            if (mLoginAuthDialog != null) {
                mLoginAuthDialog.dismiss();
            }
            ToastUtil.show(getString(R.string.auth_failure));
        }

        @Override
        public void onCancel(Platform platform) {
            if (mLoginAuthDialog != null) {
                mLoginAuthDialog.dismiss();
            }
            ToastUtil.show(getString(R.string.auth_cancle));
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginSuccessEvent(LoginSuccessEvent e) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        SharedSdkUitl.getInstance().releaseShareListener();
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        HttpUtil.cancel(HttpUtil.LOGIN_BY_THIRD);
        HttpUtil.cancel(HttpUtil.GET_QQLOGIN_OPENID);
        HttpUtil.cancel(HttpUtil.GET_RECOMMEND);
        super.onDestroy();
    }

    @Override
    public void onItemClick(SharedSdkBean item, int position) {
        mType = item.getType();
        if (mLoginAuthDialog == null) {
            mLoginAuthDialog = DialogUitl.loginAuthDialog(this);
        }
        mLoginAuthDialog.show();
        SharedSdkUitl.getInstance().login(mType, mShareListener);
    }

}
