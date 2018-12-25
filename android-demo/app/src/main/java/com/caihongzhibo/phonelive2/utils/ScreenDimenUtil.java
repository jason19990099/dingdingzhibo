package com.caihongzhibo.phonelive2.utils;

import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.caihongzhibo.phonelive2.AppContext;

import java.lang.reflect.Field;

/**
 * Created by cxf on 2017/10/30.
 * 获取屏幕尺寸
 */

public class ScreenDimenUtil {

    private int mStatusBarHeight;//状态栏高度
    private Rect mContentViewRect;
    private int mContentViewHeight;
    private int mScreenHeight;
    private int mScreenWidth;

    private static ScreenDimenUtil sInstance;

    private ScreenDimenUtil() {
        Resources resources = AppContext.sInstance.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        //网上找的办法，使用反射在DecoderView未绘制出来之前计算状态栏的高度
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            mStatusBarHeight = resources.getDimensionPixelSize(x);
            mContentViewHeight = mScreenHeight - mStatusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ScreenDimenUtil getInstance() {
        if (sInstance == null) {
            synchronized (ScreenDimenUtil.class) {
                if (sInstance == null) {
                    sInstance = new ScreenDimenUtil();
                }
            }
        }
        return sInstance;
    }


    /**
     * 获取contentView的宽高
     */
    public Rect getContentViewDimens() {
        if (mContentViewRect == null) {
            mContentViewRect = new Rect(0, mStatusBarHeight, mScreenWidth, mScreenHeight);
        }
        return mContentViewRect;
    }

    public int getContentViewHeight() {
        return mContentViewHeight;
    }

}
