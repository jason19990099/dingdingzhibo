package com.caihongzhibo.phonelive2.custom.linkmic;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by cxf on 2018/9/19.
 */

public class LinkMicViewHolder {

    public static final int LINK_MIC_PUSH = 1;
    public static final int LINK_MIC_PLAY = 2;
    private LinkMicStrategy mStrategy;
    private LinkMicPush mLinkMicPush;
    private LinkMicPlay mLinkMicPlay;

    public LinkMicViewHolder(Context context, ViewGroup container) {
        mLinkMicPush = new LinkMicPush(context, container);
        mLinkMicPlay = new LinkMicPlay(context, container);
    }

    public void setActionListener(LinkMicCallback linkMicCallback) {
        if (mLinkMicPush != null) {
            mLinkMicPush.setLinkMicCallback(linkMicCallback);
        }
        if (mLinkMicPlay != null) {
            mLinkMicPlay.setLinkMicCallback(linkMicCallback);
        }
    }

    public LinkMicViewHolder setType(int type) {
        if (type == LINK_MIC_PUSH) {
            mStrategy = mLinkMicPush;
        } else if (type == LINK_MIC_PLAY) {
            mStrategy = mLinkMicPlay;
        } else {
            mStrategy = null;
        }
        return this;
    }

    public LinkMicViewHolder setUrl(String url) {
        if (mStrategy != null) {
            mStrategy.setUrl(url);
        }
        return this;
    }

    public void start(boolean isAnchor) {
        if (mStrategy != null) {
            mStrategy.start(isAnchor);
        }
    }

    public void stop() {
        if (mStrategy != null) {
            mStrategy.stop();
        }
    }

    public interface LinkMicCallback {
        void onPushSuccess();

        void onPushFailed();

        void onPlayClose();

        void onPlaySuccess();

        void onPlayFailed();
    }
}
