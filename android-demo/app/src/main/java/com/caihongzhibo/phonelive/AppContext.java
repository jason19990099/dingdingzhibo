package com.caihongzhibo.phonelive;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;

import cn.tillusory.sdk.TiSDK;


/**
 * Created by cxf on 2017/8/3.
 */

public class AppContext extends MultiDexApplication {
    public static AppContext sInstance;
    //public static RefWatcher sRefWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        //sRefWatcher = LeakCanary.install(this);
        CrashReport.initCrashReport(getApplicationContext());
        //初始化萌颜
        TiSDK.init(AppConfig.BEAUTY_KEY, this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }
}
