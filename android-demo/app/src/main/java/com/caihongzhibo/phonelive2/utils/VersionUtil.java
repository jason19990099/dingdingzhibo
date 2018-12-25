package com.caihongzhibo.phonelive2.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.ConfigBean;

/**
 * Created by cxf on 2017/10/9.
 */

public class VersionUtil {
    public static void checkVersion(ConfigBean configBean, final Context context,Runnable runnable) {
        if (!AppConfig.getInstance().getVersion().equals(configBean.getApk_ver())) {
            DialogUitl.confirmDialog(
                    context,
                    context.getString(R.string.tip),
                    configBean.getApk_des(),
                    context.getString(R.string.immediate_use),
                    context.getString(R.string.not_update), true,
                    new DialogUitl.Callback() {
                        @Override
                        public void confirm(Dialog dialog) {
                            dialog.dismiss();
                            String apkUrl = AppConfig.getInstance().getConfig().getApk_url();
                            if ("".equals(apkUrl)) {
                                ToastUtil.show(context.getString(R.string.apk_url_not_exist));
                            } else {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    intent.setData(Uri.parse(apkUrl));
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    ToastUtil.show(context.getString(R.string.apk_url_not_exist));
                                }
                            }
                        }

                        @Override
                        public void cancel(Dialog dialog) {
                            dialog.dismiss();
                        }
                    }
            ).show();
        }else {
            if(runnable!=null){
                runnable.run();
            }
        }
    }
}
