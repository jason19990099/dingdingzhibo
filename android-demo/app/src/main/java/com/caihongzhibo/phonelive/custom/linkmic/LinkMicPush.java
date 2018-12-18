package com.caihongzhibo.phonelive.custom.linkmic;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

/**
 * Created by cxf on 2018/9/19.
 */

public class LinkMicPush extends LinkMicStrategy {

    private LinkMicPushView mPushView;
    private LinkMicPushView.ActionListener mActionListener;

    public LinkMicPush(Context context, ViewGroup container) {
        super(context, container);
        mActionListener = new LinkMicPushView.ActionListener() {
            @Override
            public void onPushSuccess() {
                if (mLinkMicCallback != null) {
                    mLinkMicCallback.onPushSuccess();
                }
            }

            @Override
            public void onPushFailed() {
                if (mLinkMicCallback != null) {
                    mLinkMicCallback.onPushFailed();
                }
            }

        };
    }

    @Override
    public void start(boolean isAnchor) {
        if (mContext != null && mContainer != null && !TextUtils.isEmpty(mUrl)) {
            mPushView = new LinkMicPushView(mContext);
            mPushView.setActionListener(mActionListener);
            mContainer.addView(mPushView);
            mPushView.startPush(mUrl);
        }
    }


    @Override
    public void stop() {
        if (mPushView != null) {
            mPushView.stopPush();
        }
        if (mContainer != null) {
            mContainer.removeAllViews();
        }
    }

    @Override
    public void onPause() {
        if (mPushView != null) {
            mPushView.onPause();
        }
    }

    @Override
    public void onResume() {
        if (mPushView != null) {
            mPushView.onResume();
        }
    }
}
