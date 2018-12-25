package com.caihongzhibo.phonelive2.custom.linkmic;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import java.io.IOException;

/**
 * Created by cxf on 2018/9/18.
 */

public class LinkMicPlayView extends FrameLayout {

    private int mScreenWidth;
    private int mScreenHeight;
    private Context mContext;
    private KSYTextureView mTextureView;
    private View mLoading;
    private ActionListener mActionListener;
    private String mStreamUrl;
    private boolean mStarted;
    private boolean mPaused;
    private boolean mDestoryed;//是否被销毁了
    private View mBtnClose;

    public LinkMicPlayView(@NonNull Context context) {
        this(context, null);
    }

    public LinkMicPlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkMicPlayView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_linkmic_play, this, true);
        mTextureView = (KSYTextureView) view.findViewById(R.id.player);
        mTextureView.setKeepScreenOn(true);
        mTextureView.setOnPreparedListener(mOnPreparedListener);
        mTextureView.setOnErrorListener(mOnErrorListener);
        mTextureView.setOnInfoListener(mOnInfoListener);
        mTextureView.setScreenOnWhilePlaying(true);
        mTextureView.setTimeout(5000, 5000);
        mTextureView.setVolume(1f, 1f);
        mTextureView.setLooping(true);//循环播放
        mTextureView.setDecodeMode(KSYMediaPlayer.KSYDecodeMode.KSY_DECODE_MODE_AUTO);
        mTextureView.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mLoading = view.findViewById(R.id.loading);
        mBtnClose = view.findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionListener != null) {
                    mActionListener.onCloseClick();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mScreenWidth / 4, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mScreenHeight / 4, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(IMediaPlayer mp) {
            mStarted = true;
            if (!mDestoryed) {
                mTextureView.start();
                if (mActionListener != null) {
                    mActionListener.onPlaySuccess();
                }
            } else {
                mTextureView.stop();
                mTextureView.reset();
                mTextureView.release();
                mTextureView.setOnPreparedListener(null);
                mTextureView.setOnErrorListener(null);
                mTextureView.setOnInfoListener(null);
            }
        }
    };

    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int info, int extra) {
            if (mLoading == null) {
                return false;
            }
            switch (info) {
                case KSYMediaPlayer.MEDIA_INFO_BUFFERING_START://缓冲开始
                    ToastUtil.show(WordUtil.getString(R.string.net_work_error));
                    mLoading.setVisibility(View.VISIBLE);
                    break;
                case KSYMediaPlayer.MEDIA_INFO_BUFFERING_END://缓冲结束
                    mLoading.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_RELOADED:// reload成功后会有消息回调
                    break;
            }
            return false;
        }
    };


    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            L.e("OnErrorListener--->" + what);
            switch (what) {
                //网络较差播放过程中触发设置超时时间，报错退出
                case -10011://播放http+mp4点播流，弱网环境
                case -10002://播放http(mp4/flv/hls)点播流，无效wifi环境,连接无效wifi，播放rtmp直播流
                case -10004://播放rtmp直播流，弱网环境
                case -1004://播播http+flv点播流，播放过程中断网
                    ToastUtil.show(mContext.getString(R.string.play_failure));
                    break;
                case -10007:
                case -10008://播放无效的http地址,超时设置足够长
                case 1:
                    ToastUtil.show(mContext.getString(R.string.mp4_error));
                    break;
            }
            if (mActionListener != null) {
                mActionListener.onPlayFailed();
            }
            return false;
        }
    };


    public void play(String streamUrl,boolean showCloseBtn) {
        if (mTextureView == null) {
            return;
        }
        if(showCloseBtn){
            mBtnClose.setVisibility(VISIBLE);
        }
        mStreamUrl = streamUrl;
        //mStreamUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
        try {
            mTextureView.setDataSource(mStreamUrl);
            mTextureView.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        if (mTextureView != null && !mPaused) {
            mPaused = true;
            mTextureView.runInBackground(false);
        }
    }


    public void onResume() {
        if (mTextureView != null && mPaused) {
            mPaused = false;
            mTextureView.runInForeground();
            mTextureView.start();
        }
    }

    public void destroy() {
        mDestoryed = true;
        if (mStarted) {
            mTextureView.stop();
            mTextureView.reset();
            mTextureView.release();
            mTextureView.setOnPreparedListener(null);
            mTextureView.setOnErrorListener(null);
            mTextureView.setOnInfoListener(null);
        }
    }

    public interface ActionListener {
        void onPlaySuccess();

        void onPlayFailed();

        void onCloseClick();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
