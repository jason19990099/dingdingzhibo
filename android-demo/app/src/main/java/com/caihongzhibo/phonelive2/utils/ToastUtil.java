package com.caihongzhibo.phonelive2.utils;

import android.widget.Toast;

import com.caihongzhibo.phonelive2.AppContext;

/**
 * Created by cxf on 2017/8/3.
 */

public class ToastUtil {

    private static Toast sToast;

    static {
        sToast = Toast.makeText(AppContext.sInstance, "", Toast.LENGTH_SHORT);
    }

    public static void show(String s) {
        sToast.setText(s);
        sToast.show();
    }

}
