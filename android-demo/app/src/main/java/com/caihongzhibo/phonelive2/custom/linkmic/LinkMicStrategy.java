package com.caihongzhibo.phonelive2.custom.linkmic;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by cxf on 2018/9/19.
 */

public abstract class LinkMicStrategy {

    protected Context mContext;
    protected ViewGroup mContainer;
    protected String mUrl;
    protected LinkMicViewHolder.LinkMicCallback mLinkMicCallback;

    public LinkMicStrategy(Context context, ViewGroup container) {
        mContext = context;
        mContainer = container;
    }

    public void setLinkMicCallback(LinkMicViewHolder.LinkMicCallback linkMicCallback) {
        mLinkMicCallback = linkMicCallback;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public abstract void start(boolean isAnchor);

    public abstract void stop();

    public abstract void onPause();

    public abstract void onResume();
}
