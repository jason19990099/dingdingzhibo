package com.caihongzhibo.phonelive2;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.commonsdk.UMConfigure;

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
        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(sInstance, "5ca34393203657f4c2000f94","android-user", UMConfigure.DEVICE_TYPE_PHONE, "5ca34393203657f4c2000f94");

    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }
}
