package com.caihongzhibo.phonelive2.utils;

import android.content.Intent;

import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.AppContext;
import com.caihongzhibo.phonelive2.activity.LoginActivity;
import com.caihongzhibo.phonelive2.im.IMUtil;


/**
 * Created by cxf on 2017/8/18.
 */

public class LoginUtil {

    /**
     * 跳转到登录页面
     */
    public static void forwardLogin() {
        Intent intent = new Intent(AppContext.sInstance, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        AppContext.sInstance.startActivity(intent);
    }

    /**
     * 登出
     */
    public static void logout() {
        stopThridLibrary();
        SharedPreferencesUtil.getInstance().clear();
        AppConfig.getInstance().reset();
        forwardLogin();
    }

    /**
     * 登入
     */
    public static void login(String uid, String token) {
        SharedPreferencesUtil.getInstance().saveUidAndToken(uid, token);
        AppConfig.getInstance().setUid(uid);
        AppConfig.getInstance().setToken(token);
        startThridLibray();
    }

    /**
     * 启动三方库
     */
    public static void startThridLibray() {
        JPushUtil.setAlias(AppConfig.getInstance().getUid());//给极光推送设置别名
        IMUtil.getInstance().loginClient(AppConfig.getInstance().getUid());//登录IM
    }

    /**
     * 停止三方库
     */
    public static void stopThridLibrary() {
        //退出IM
        IMUtil.getInstance().logoutClient();
        //退出极光推送
        JPushUtil.stopPush();
        //停止高德定位
        LocationUtil.getInstance().stopLocation();
    }
}
