package com.caihongzhibo.phonelive2.custom.linkmic;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

/**
 * Created by cxf on 2018/9/19.
 */

public class LinkMicPlay extends LinkMicStrategy {

    private LinkMicPlayView mPlayView;
    private LinkMicPlayView.ActionListener mActionListener;

    public LinkMicPlay(Context context, ViewGroup container) {
        super(context, container);
        mActionListener = new LinkMicPlayView.ActionListener() {
            @Override
            public void onPlaySuccess() {
                if (mLinkMicCallback != null) {
                    mLinkMicCallback.onPlaySuccess();
                }
            }

            @Override
            public void onPlayFailed() {
                if (mLinkMicCallback != null) {
                    mLinkMicCallback.onPlayFailed();
                }
            }

            @Override
            public void onCloseClick() {
                if (mLinkMicCallback != null) {
                    mLinkMicCallback.onPlayClose();
                }
            }
        };
    }

    @Override
    public void start(boolean isAnchor) {
        if (mContext != null && mContainer != null && !TextUtils.isEmpty(mUrl)) {
            mPlayView = new LinkMicPlayView(mContext);
            mPlayView.setActionListener(mActionListener);
            mContainer.addView(mPlayView);
            mPlayView.play(mUrl, isAnchor);
        }
    }

    @Override
    public void stop() {
        if (mPlayView != null) {
            mPlayView.destroy();
        }
        if (mContainer != null) {
            mContainer.removeAllViews();
        }
    }

    @Override
    public void onPause() {
        if (mPlayView != null) {
            mPlayView.onPause();
        }
    }

    @Override
    public void onResume() {
        if (mPlayView != null) {
            mPlayView.onResume();
        }
    }


}
