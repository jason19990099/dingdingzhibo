package com.caihongzhibo.phonelive2.bean;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.utils.WordUtil;

/**
 * Created by cxf on 2017/9/28.
 */

public class SharedSdkBean {
    private String type;
    private int drawable;
    private String title;
    private boolean checked;

    public SharedSdkBean() {
    }

    public SharedSdkBean(String type, int drawable, String title) {
        this.type = type;
        this.drawable = drawable;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public static final String QQ = "qq";//qq
    public static final String QZONE = "qzone";//qq空间
    public static final String WX = "wx";//微信
    public static final String WX_PYQ = "wchat";//微信朋友圈
    public static final String FACEBOOK = "facebook";//脸书
    public static final String TWITTER = "twitter";//推特

    public static SharedSdkBean create(String type) {
        SharedSdkBean bean = null;
        switch (type) {
            case QQ:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_qq, "QQ");
                break;
            case QZONE:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_qzone, WordUtil.getString(R.string.qzone));
                break;
            case WX:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_wx, WordUtil.getString(R.string.wechat));
                break;
            case WX_PYQ:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_wxpyq, WordUtil.getString(R.string.pengyouquan));
                break;
            case FACEBOOK:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_facebook, "facebook");
                break;
            case TWITTER:
                bean = new SharedSdkBean(type, R.mipmap.icon_plat_twitter, "twitter");
                break;
        }
        return bean;
    }
}
