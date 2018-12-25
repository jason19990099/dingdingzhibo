package com.caihongzhibo.phonelive2.custom.linkmic;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.utils.L;

/**
 * Created by cxf on 2018/9/18.
 */

public class LinkMicPushView extends FrameLayout {
    private static final int CAP_RESOLUTION = StreamerConstants.VIDEO_RESOLUTION_480P;//采集分辨率
    private static final int PREVIEW_RESOLUTION = StreamerConstants.VIDEO_RESOLUTION_720P;//预览分辨率
    private static final int VIDEO_RESOLUTION = StreamerConstants.VIDEO_RESOLUTION_480P;//推流分辨率
    private static final int ENCODE_TYPE = AVConst.CODEC_ID_AVC;//H264
    private static final int ENCODE_METHOD = StreamerConstants.ENCODE_METHOD_SOFTWARE;//软编
    private static final int ENCODE_SCENE = VideoEncodeFormat.ENCODE_SCENE_SHOWSELF;//秀场模式
    private static final int ENCODE_PROFILE = VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER;//低功耗
    private static final int FRAME_RATE = 15;//采集帧率
    private static final int VIDEO_BITRATE = 800;//视频码率
    private static final int AUDIO_BITRATE = 48;//音频码率
    private int mScreenWidth;
    private int mScreenHeight;
    private Context mContext;
    private GLSurfaceView mCameraPreView;//预览的控件
    private KSYStreamer mStreamer;//金山推流器
    private ActionListener mActionListener;
    private boolean mStarted;//是否推流成功了
    private boolean mStoped;//是否停止了推流
    private boolean mPaused;//是否在推流中切后台了

    public LinkMicPushView(Context context) {
        this(context, null);
    }

    public LinkMicPushView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkMicPushView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_linkmic_push, this, true);
        mCameraPreView = (GLSurfaceView) view.findViewById(R.id.camera_preview);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mScreenWidth / 4, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mScreenHeight / 4, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void startPush(String pushUrl){
        mStarted=false;
        mStoped=false;
        mStreamer = new KSYStreamer(mContext);
        mStreamer.setPreviewFps(FRAME_RATE);//预览采集帧率
        mStreamer.setTargetFps(FRAME_RATE);//推流采集帧率
        mStreamer.setVideoKBitrate(VIDEO_BITRATE * 3 / 4, VIDEO_BITRATE, VIDEO_BITRATE / 4);//视频码率
        mStreamer.setAudioKBitrate(AUDIO_BITRATE);//音频码率
        mStreamer.setCameraCaptureResolution(CAP_RESOLUTION);//采集分辨率
        mStreamer.setPreviewResolution(PREVIEW_RESOLUTION);//预览分辨率
        mStreamer.setTargetResolution(VIDEO_RESOLUTION);//推流分辨率
        mStreamer.setVideoCodecId(ENCODE_TYPE);//H264
        mStreamer.setEncodeMethod(ENCODE_METHOD);//软编
        mStreamer.setVideoEncodeScene(ENCODE_SCENE);//秀场模式
        mStreamer.setVideoEncodeProfile(ENCODE_PROFILE);
        mStreamer.setAudioChannels(2);//双声道推流
        mStreamer.setVoiceVolume(2f);
        mStreamer.setEnableRepeatLastFrame(false);  // 切后台的时候不使用最后一帧
        mStreamer.setEnableAutoRestart(true, 3000); // 自动重启推流
        mStreamer.setCameraFacing(CameraCapture.FACING_FRONT);
        mStreamer.setFrontCameraMirror(true);
        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
        mStreamer.setOnLogEventListener(mOnLogEventListener);
        mStreamer.setUrl(pushUrl);//推流地址
        mStreamer.setDisplayPreview(mCameraPreView);
        mStreamer.startCameraPreview();//启动预览
        mStreamer.startStream();
    }

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case 1000://初始化完毕
                    L.e("mStearm--->初始化完毕");
                    break;
                case 0://推流成功
                    L.e("mStearm--->推流成功");
                    mStarted=true;
                    if(mActionListener!=null){
                        mActionListener.onPushSuccess();
                    }
                    break;
            }
        }
    };

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case -1009://推流url域名解析失败
                    L.e("mStearm--->推流url域名解析失败");
                    break;
                case -1006://网络连接失败，无法建立连接
                    L.e("mStearm--->网络连接失败，无法建立连接");
                    break;
                case -1010://跟RTMP服务器完成握手后,推流失败
                    L.e("mStearm--->跟RTMP服务器完成握手后,推流失败");
                    break;
                case -1007://网络连接断开
                    L.e("mStearm--->网络连接断开");
                    break;
                case -2004://音视频采集pts差值超过5s
                    L.e("mStearm--->音视频采集pts差值超过5s");
                    break;
                case -1004://编码器初始化失败
                    L.e("mStearm--->编码器初始化失败");
                    break;
                case -1003://视频编码失败
                    L.e("mStearm--->视频编码失败");
                    break;
                case -1008://音频初始化失败
                    L.e("mStearm--->音频初始化失败");
                    break;
                case -1011://音频编码失败
                    L.e("mStearm--->音频编码失败");
                    break;
                case -2001: //摄像头未知错误
                    L.e("mStearm--->摄像头未知错误");
                    break;
                case -2002://打开摄像头失败
                    L.e("mStearm--->打开摄像头失败");
                    break;
                case -2003://录音开启失败
                    L.e("mStearm--->录音开启失败");
                    break;
                case -2005://录音开启未知错误
                    L.e("mStearm--->录音开启未知错误");
                    break;
                case -2006://系统Camera服务进程退出
                    L.e("mStearm--->系统Camera服务进程退出");
                    break;
                case -2007://Camera服务异常退出
                    L.e("mStearm--->Camera服务异常退出");
                    break;
            }
            switch (what) {
                case -2001:
                case -2002:
                case -2006:
                case -2007:
                    mStreamer.stopCameraPreview();
                    break;
            }
            if(mActionListener!=null){
                mActionListener.onPushFailed();
            }
        }
    };

    public void stopPush(){
        mStoped=true;
        if(mStreamer!=null){
            mStreamer.stopStream();
            mStreamer.stopCameraPreview();
            mStreamer.release();
            mStreamer.setOnInfoListener(null);
            mStreamer.setOnErrorListener(null);
            mStreamer.setOnLogEventListener(null);
            mStreamer = null;
        }
    }

    public void onPause(){
        if (mStoped) {
            return;
        }
        if (mStarted) {
            mStreamer.onPause();
            // 切后台时，将SDK设置为离屏推流模式，继续采集camera数据
            mStreamer.setOffscreenPreview(mStreamer.getPreviewWidth(), mStreamer.getPreviewHeight());
            mPaused = true;
        }
    }

    public void onResume() {
        if (mStoped) {
            return;
        }
        if (mPaused) {
            mPaused = false;
            mStreamer.onResume();
        }
    }

    private StatsLogReport.OnLogEventListener mOnLogEventListener = new StatsLogReport.OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuilder singleLogContent) {
            //打印推流信息
            //L.e("mStearm--->" + singleLogContent.toString());
        }
    };

    public interface ActionListener{
        void onPushSuccess();

        void onPushFailed();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }
}
