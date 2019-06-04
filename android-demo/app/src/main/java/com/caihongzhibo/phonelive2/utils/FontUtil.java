package com.caihongzhibo.phonelive2.utils;

import android.graphics.Typeface;

import com.caihongzhibo.phonelive2.AppContext;

/**
 * Created by cxf on 2017/11/17.
 */

public class FontUtil {
    private static FontUtil sInstance;
    private Typeface mTypeface;

    private FontUtil() {
        mTypeface = Typeface.createFromAsset(AppContext.sInstance.getAssets(), "fonts/myFont.ttf");
    }

    public static FontUtil getInstance() {
        if (sInstance == null) {
            synchronized (FontUtil.class) {
                sInstance = new FontUtil();
            }
        }
        return sInstance;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }
}