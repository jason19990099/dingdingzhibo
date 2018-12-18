package com.yunbao.beauty.bean;

import cn.tillusory.sdk.bean.TiFilterEnum;

/**
 * Created by cxf on 2018/8/4.
 */

public class FilterBean {
    private TiFilterEnum mTiFilterEnum;
    private boolean mChecked;

    public FilterBean(TiFilterEnum tiFilterEnum) {
        mTiFilterEnum = tiFilterEnum;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public TiFilterEnum getTiFilterEnum() {
        return mTiFilterEnum;
    }
}
