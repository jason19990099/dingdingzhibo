package com.caihongzhibo.phonelive2.utils;

import com.caihongzhibo.phonelive2.AppContext;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by cxf on 2017/8/3.
 */

public class JPushUtil {

    public static final String TAG = "极光推送";
    public static boolean isSetAlians;

    public static void init() {
        JPushInterface.init(AppContext.sInstance);
    }

    public static void setAlias(String uid) {
        if (JPushInterface.isPushStopped(AppContext.sInstance)) {
            JPushInterface.resumePush(AppContext.sInstance);
        }
        if(!isSetAlians){
            JPushInterface.setAlias(AppContext.sInstance, uid + "PUSH", new TagAliasCallback() {

                @Override
                public void gotResult(int i, String s, Set<String> set) {
                    L.e(TAG, "设置别名---->" + s);
                    isSetAlians=true;
                }
            });
        }
    }

    public static void stopPush() {
        JPushInterface.stopPush(AppContext.sInstance);
        isSetAlians=false;
    }
}
