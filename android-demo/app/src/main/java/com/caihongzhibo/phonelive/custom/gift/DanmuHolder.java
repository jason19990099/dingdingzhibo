package com.caihongzhibo.phonelive.custom.gift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.ReceiveDanMuBean;
import com.caihongzhibo.phonelive.glide.ImgLoader;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.utils.DpUtil;

/**
 * Created by cxf on 2017/8/25.
 */

public class DanmuHolder {
    private static Interpolator sInterpolator = new LinearInterpolator();
    private static final float SPEED = 0.2f;//弹幕的速度，这个值越小，弹幕走的越慢
    private static final int MARGIN_TOP = DpUtil.dp2px(150);
    private static final int SPACE = DpUtil.dp2px(50);
    private ViewGroup mParent;
    private View mView;
    private ImageView mAvatar;
    private TextView mName;
    private TextView mContent;
    private int mStartX;
    private int mWidth;
    private boolean canNext;//是否可以有下一个
    private boolean idle = true;//是否空闲
    private CommonCallback<Integer> mNextCallback;
    private int mLineNum;


    public DanmuHolder(ViewGroup parent, CommonCallback<Integer> nextCallback) {
        mParent = parent;
        mView = LayoutInflater.from(AppContext.sInstance).inflate(R.layout.view_gift_anim_danmu, parent, false);
        mAvatar = (ImageView) mView.findViewById(R.id.uhead);
        mName = (TextView) mView.findViewById(R.id.uname);
        mContent = (TextView) mView.findViewById(R.id.content);
        mStartX = mParent.getWidth();
        mNextCallback = nextCallback;
    }

    public void show(ReceiveDanMuBean bean, int lineNum) {
        ImgLoader.display(bean.getUhead(), mAvatar);
        mName.setText(bean.getUname());
        mContent.setText(bean.getContent());
        mParent.addView(mView);
        mView.measure(0, 0);
        mWidth = mView.getMeasuredWidth();
        int duration = (int) ((mStartX + mWidth) / SPEED);
        canNext = false;
        idle = false;
        mLineNum = lineNum;
        mView.setY(MARGIN_TOP + lineNum * SPACE);
        ValueAnimator a = ValueAnimator.ofFloat(mStartX, -mWidth);
        a.addUpdateListener(mUpdateListener);
        a.setInterpolator(sInterpolator);
        a.setDuration(duration);
        a.addListener(mAdapter);
        a.start();
    }

    private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float v = (float) animation.getAnimatedValue();
            mView.setX(v);
            if (!canNext && v <= mStartX - mWidth - 30) {
                canNext = true;
                if (mNextCallback != null) {
                    mNextCallback.callback(mLineNum);
                }
            }
        }
    };

    private AnimatorListenerAdapter mAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mParent.removeView(mView);
            idle = true;
        }
    };

    public boolean isIdle() {
        return idle;
    }

}
