package com.caihongzhibo.phonelive2.fragment;

import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;

import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.activity.LiveAnchorActivity;
import com.caihongzhibo.phonelive2.bean.LiveLrcBean;
import com.caihongzhibo.phonelive2.custom.music.MusicPlayer;
import com.caihongzhibo.phonelive2.socket.SocketUtil;
import com.caihongzhibo.phonelive2.utils.L;

import java.util.List;

import cn.tillusory.sdk.TiSDKManager;
import cn.tillusory.sdk.TiSDKManagerBuilder;
import cn.tillusory.sdk.bean.TiDistortionEnum;
import cn.tillusory.sdk.bean.TiFilterEnum;
import cn.tillusory.sdk.bean.TiRockEnum;

/**
 * Created by cxf on 2017/8/31.
 * 主播直播间推流的fragment
 */

public class LivePushStreamFragment extends AbsFragment {
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
    private GLSurfaceView mCameraPreView;//预览的控件
    private KSYStreamer mStreamer;//金山推流器
    private String mStreamUrl;//推流的地址
    private boolean mStarted;//是否推流成功了
    private boolean mPaused;//是否在推流中切后台了
    private boolean mStoped;//是否停止了推流
    private ImgFilterBase mFilter;//美颜 滤镜
    private float mMopiVal;//磨皮
    private float mMeibaiVal;//美白
    private float mHongRunVal;//红润
    private MusicPlayer mMusicPlayer;

    //各种萌颜效果
    private TiSDKManager mTiSDKManager;
    private int mMeibai = 0;//美白
    private int mMoPi = 0;//磨皮
    private int mBaoHe = 0;//饱和
    private int mFengNen = 0;//粉嫩
    private int mBigEye = 0;//大眼
    private int mFace = 0;//瘦脸
    private String mTieZhi = "";//贴纸
    private TiDistortionEnum mTiDistortionEnum = TiDistortionEnum.NO_DISTORTION;
    private TiFilterEnum mTiFilterEnum = TiFilterEnum.NO_FILTER;
    private TiRockEnum mTiRockEnum = TiRockEnum.NO_ROCK;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_push_stream;
    }

    @Override
    protected void main() {
        mCameraPreView = (GLSurfaceView) mRootView.findViewById(R.id.camera_preview);
        mStreamUrl = getArguments().getString("streamUrl");
        mStreamer = new KSYStreamer(mContext);
        mStreamer.setUrl(mStreamUrl);//推流地址
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
        mStreamer.setDisplayPreview(mCameraPreView);

//        //萌颜的美颜
//        mTiSDKManager = new TiSDKManagerBuilder().build();
//        //使用萌颜的美颜，美型效果
//        mTiSDKManager.setBeautyEnable(true);
//        mTiSDKManager.setFaceTrimEnable(true);
//        mTiSDKManager.setSkinWhitening(mMeibai);//美白
//        mTiSDKManager.setSkinBlemishRemoval(mMoPi);//磨皮
//        mTiSDKManager.setSkinSaturation(mBaoHe);//饱和
//        mTiSDKManager.setSkinTenderness(mFengNen);//粉嫩
//        mTiSDKManager.setEyeMagnifying(mBigEye);//大眼
//        mTiSDKManager.setChinSlimming(mFace);//瘦脸
//        mTiSDKManager.setSticker(mTieZhi);//贴纸
//        mTiSDKManager.setDistortionEnum(mTiDistortionEnum);
//        mTiSDKManager.setFilterEnum(mTiFilterEnum);
//        mTiSDKManager.setRockEnum(mTiRockEnum);
//        mStreamer.getImgTexFilterMgt().setFilter(new TiFilter(mTiSDKManager, mStreamer.getGLRender()));


        mStreamer.startCameraPreview();//启动预览
        mStreamer.startStream();
        //金山自带的美颜
        setDefaultFilter();
    }


    /*********************萌颜的美颜*******************/

    public void onFilterChanged(TiFilterEnum tiFilterEnum) {
        if (mTiSDKManager != null) {
            mTiFilterEnum = tiFilterEnum;
            mTiSDKManager.setFilterEnum(tiFilterEnum);
        }
    }

    public void onRockChanged(TiRockEnum tiRockEnum) {
        if (mTiSDKManager != null) {
            mTiRockEnum = tiRockEnum;
            mTiSDKManager.setRockEnum(tiRockEnum);
        }
    }

    public void onMeiBaiChanged(int progress) {
        if (mTiSDKManager != null) {
            mMeibai = progress;
            mTiSDKManager.setSkinWhitening(progress);
        }
    }

    public void onMoPiChanged(int progress) {
        if (mTiSDKManager != null) {
            mMoPi = progress;
            mTiSDKManager.setSkinBlemishRemoval(progress);
        }
    }

    public void onBaoHeChanged(int progress) {
        if (mTiSDKManager != null) {
            mBaoHe = progress;
            mTiSDKManager.setSkinSaturation(progress);
        }
    }

    public void onFengNenChanged(int progress) {
        if (mTiSDKManager != null) {
            mFengNen = progress;
            mTiSDKManager.setSkinTenderness(progress);
        }
    }

    public void onBigEyeChanged(int progress) {
        if (mTiSDKManager != null) {
            mBigEye = progress;
            mTiSDKManager.setEyeMagnifying(progress);
        }
    }

    public void onFaceChanged(int progress) {
        if (mTiSDKManager != null) {
            mFace = progress;
            mTiSDKManager.setChinSlimming(progress);
        }
    }

    public void onTieZhiChanged(String tieZhiName) {
        if (mTiSDKManager != null) {
            mTieZhi = tieZhiName;
            mTiSDKManager.setSticker(tieZhiName);
        }
    }

    public void onHaHaChanged(TiDistortionEnum tiDistortionEnum) {
        if (mTiSDKManager != null) {
            mTiDistortionEnum = tiDistortionEnum;
            mTiSDKManager.setDistortionEnum(tiDistortionEnum);
        }
    }

    /*********************萌颜的美颜*******************/

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case 1000://初始化完毕
                    L.e("mStearm--->初始化完毕");
                    break;
                case 0://推流成功
                    L.e("mStearm--->推流成功");
                    ((LiveAnchorActivity) mContext).changeLive();
                    mStarted = true;
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
        }
    };

    private StatsLogReport.OnLogEventListener mOnLogEventListener = new StatsLogReport.OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuilder singleLogContent) {
            //打印推流信息
            //L.e("mStearm--->" + singleLogContent.toString());
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (mStoped) {
            return;
        }
        if (mPaused) {
            mPaused = false;
            mStreamer.onResume();
            SocketUtil.getInstance().sendSystemMessage(mContext.getString(R.string.anchor_come_back));
        }
        if (mMusicPlayer != null) {
            mMusicPlayer.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mStoped) {
            return;
        }
        if (mStarted) {
            mStreamer.onPause();
            // 切后台时，将SDK设置为离屏推流模式，继续采集camera数据
            mStreamer.setOffscreenPreview(mStreamer.getPreviewWidth(), mStreamer.getPreviewHeight());
            mPaused = true;
        }
        SocketUtil.getInstance().sendSystemMessage(mContext.getString(R.string.anchor_leave));
        if (mMusicPlayer != null) {
            mMusicPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPushStream();
    }

    /**
     * 停止推流
     */
    public void stopPushStream() {
        if (mStoped) {
            return;
        }
        mStoped = true;
        if (mMusicPlayer != null) {
            mMusicPlayer.destroy();
        }
        mStreamer.stopStream();
        mStreamer.stopCameraPreview();
        mStreamer.release();
        mStreamer.setOnInfoListener(null);
        mStreamer.setOnErrorListener(null);
        mStreamer.setOnLogEventListener(null);
        mStreamer = null;
        mFilter = null;
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mStreamer != null) {
            mStreamer.switchCamera();
        }
    }

    /**
     * 打开闪光灯
     */
    public void toggleFlash() {
        if (mStreamer != null) {
            CameraCapture capture = mStreamer.getCameraCapture();
            Camera.Parameters parameters = capture.getCameraParameters();
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {//如果闪光灯已开启
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//设置成关闭的
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//设置成开启的
            }
            capture.setCameraParameters(parameters);
        }
    }

    /*********************金山自带的美颜*******************/

    /**
     * 设置默认滤镜
     */
    public void setDefaultFilter() {
        //设置美颜模式
        mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO3);
        if (mFilter == null) {
            List<ImgFilterBase> filters = mStreamer.getImgTexFilterMgt().getFilter();
            if (filters != null && filters.size() > 0) {
                mFilter = filters.get(0);
                mMopiVal = mFilter.getGrindRatio();
                mMeibaiVal = mFilter.getWhitenRatio();
                mHongRunVal = mFilter.getRuddyRatio();
            }
        } else {
            List<ImgFilterBase> filters = mStreamer.getImgTexFilterMgt().getFilter();
            if (filters != null && filters.size() > 0) {
                mFilter = filters.get(0);
            }
            mFilter.setGrindRatio(mMopiVal);
            mFilter.setWhitenRatio(mMeibaiVal);
            mFilter.setRuddyRatio(mHongRunVal);
        }
    }

    /**
     * 创建特殊滤镜  等
     *
     * @param id 1 原图 2 清新 3 靓丽 4 甜美 5 怀旧
     */
    public void setSpecialFilter(int id) {
        if (id == 1) {
            setDefaultFilter();
            return;
        }
        int type = 0;
        switch (id) {
            case 2:
                type = ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_FRESHY;
                break;
            case 3:
                type = ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_BEAUTY;
                break;
            case 4:
                type = ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SWEETY;
                break;
            case 5:
                type = ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SEPIA;
                break;
        }
        ImgBeautySpecialEffectsFilter effectsFilter = new ImgBeautySpecialEffectsFilter(
                mStreamer.getGLRender(), mContext, type);
        mStreamer.getImgTexFilterMgt().setFilter(effectsFilter);
    }

    /**
     * 获取当前美颜的数值
     *
     * @return
     */
    public int[] getBeautyData() {
        int[] data = null;
        if (mFilter != null) {
            data = new int[3];
            data[0] = (int) (mMopiVal * 100);//磨皮
            data[1] = (int) (mMeibaiVal * 100);//美白
            data[2] = (int) (mHongRunVal * 100);//红润
        }
        return data;
    }

    /**
     * 设置美颜数值
     *
     * @param type 美颜的类型   0 磨皮 1 美白 2 红润
     * @param val  0~1 的float数值
     */
    public void setBeautyData(int type, float val) {
        switch (type) {
            case 0:
                mMopiVal = val;
                L.e("磨皮--->" + mMopiVal);
                mFilter.setGrindRatio(mMopiVal);
                break;
            case 1:
                mMeibaiVal = val;
                L.e("美白--->" + mMeibaiVal);
                mFilter.setWhitenRatio(mMeibaiVal);
                break;
            case 2:
                mHongRunVal = val;
                L.e("红润--->" + mHongRunVal);
                mFilter.setRuddyRatio(mHongRunVal);
                break;
        }

    }

    /*********************金山自带的美颜*******************/

    /**
     * 播放音乐
     */
    public void playMusic(ViewGroup parent, String musicId, LiveLrcBean bean) {
        if (mMusicPlayer == null) {
            mMusicPlayer = new MusicPlayer(parent, mStreamer, new Runnable() {
                @Override
                public void run() {
                    mMusicPlayer = null;
                }
            });
        }
        mMusicPlayer.play(AppConfig.getInstance().MUSIC_PATH + musicId + ".mp3", bean);
    }

    public void setOriginCameraPreView() {
        if (mStreamer != null && mCameraPreView != null) {
            mStreamer.onPause();
            mStreamer.setDisplayPreview((GLSurfaceView) null);
            mStreamer.setDisplayPreview(mCameraPreView);
            mStreamer.onResume();
        }
    }

    public void setCameraPreView(GLSurfaceView surfaceView) {
        if (mStreamer != null) {
            mStreamer.setDisplayPreview(surfaceView);
        }
    }

}
