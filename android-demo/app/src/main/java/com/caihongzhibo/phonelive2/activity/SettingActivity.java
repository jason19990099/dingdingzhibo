package com.caihongzhibo.phonelive2.activity;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.ConfigBean;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.CommonCallback;
import com.caihongzhibo.phonelive2.utils.DialogUitl;
import com.caihongzhibo.phonelive2.utils.GlideCatchUtil;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.LoginUtil;
import com.caihongzhibo.phonelive2.utils.TimeOutCountDown;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.VersionUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

/**
 * Created by cxf on 2017/8/18.
 */

public class SettingActivity extends AbsActivity {

    private TextView mVersion;
    private TextView mCache;
    private Dialog mDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void main() {
        setTitle(getString(R.string.setting));
        mVersion = (TextView) findViewById(R.id.version);
        mCache = (TextView) findViewById(R.id.cache);
        mVersion.setText("("+getString(R.string.cur_version) + AppConfig.getInstance().getVersion() + ")");
        mCache.setText(getCacheSize());
    }

    public void settingClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_pwd:
                startActivity(new Intent(mContext, ResetPwdActivity.class));
                break;
            case R.id.btn_check_update:
                checkVersion();
                break;
            case R.id.btn_clear_cache:
                clearCache();
                break;
            case R.id.btn_logout:
                LoginUtil.logout();
                break;
        }
    }


    private String getCacheSize() {
        String cache = GlideCatchUtil.getInstance().getCacheSize();
        L.e("缓存大小--->" + cache);
        if ("0.0Byte".equalsIgnoreCase(cache)) {
            cache = getString(R.string.no_cache);
        }
        return cache;
    }

    private void clearCache() {
        if (mDialog == null) {
            mDialog = DialogUitl.loadingDialog(mContext, getString(R.string.clear_ing));
        }
        mDialog.show();
        GlideCatchUtil.getInstance().clearImageAllCache();
        new TimeOutCountDown(2000, new TimeOutCountDown.Callback() {
            @Override
            public void callback() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mCache.setText(getCacheSize());
            }
        }).start();

    }

    private void checkVersion() {
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(final ConfigBean bean) {
                VersionUtil.checkVersion(bean, mContext, new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show(WordUtil.getString(R.string.cur_version_newset));
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        super.onDestroy();

    }
}
