package com.caihongzhibo.phonelive2.activity;

import android.app.Dialog;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.FunctionBean;
import com.caihongzhibo.phonelive2.bean.LiveBean;
import com.caihongzhibo.phonelive2.bean.LiveLrcBean;
import com.caihongzhibo.phonelive2.bean.UserBean;
import com.caihongzhibo.phonelive2.beauty.BeautyHolder;
import com.caihongzhibo.phonelive2.custom.linkmic.LinkMicViewHolder;
import com.caihongzhibo.phonelive2.fragment.LiveAnchorBottomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveAnchorEndFragment;
import com.caihongzhibo.phonelive2.fragment.LiveAuctionFragment;
import com.caihongzhibo.phonelive2.fragment.LiveBottomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveEffectFragment;
import com.caihongzhibo.phonelive2.fragment.LiveFunctionFragment;
import com.caihongzhibo.phonelive2.fragment.LiveMusicFragment;
import com.caihongzhibo.phonelive2.fragment.LivePushStreamFragment;
import com.caihongzhibo.phonelive2.fragment.LiveTimeChargeFragment;
import com.caihongzhibo.phonelive2.game.ChooseGameFragment;
import com.caihongzhibo.phonelive2.game.GameManager;
import com.caihongzhibo.phonelive2.glide.ImgLoader;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.CommonCallback;
import com.caihongzhibo.phonelive2.socket.SocketUtil;
import com.caihongzhibo.phonelive2.utils.DialogUitl;
import com.caihongzhibo.phonelive2.utils.IconUitl;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import cn.tillusory.sdk.bean.TiDistortionEnum;
import cn.tillusory.sdk.bean.TiFilterEnum;
import cn.tillusory.sdk.bean.TiRockEnum;

/**
 * Created by cxf on 2017/8/19.
 * 主播直播间
 */

public class LiveAnchorActivity extends LiveActivity {

    private LiveFunctionFragment mFunctionFragment;//功能弹窗
    public LivePushStreamFragment mPushStreamFragment;//推流
    private LiveEffectFragment mLiveEffectFragment;//美颜滤镜
    private LiveMusicFragment mMusicFragment;//音乐
    private LiveTimeChargeFragment mTimeChargeFragment;//计时收费弹窗
    private String mLiveType;
    private String mTypeVal;//计时收费的value
    private JSONObject mLiveData;//开播后返回的数据
    private ChooseGameFragment mGameFragment;//选择游戏的弹窗
    private boolean mPaused;
//    private BeautyHolder mBeautyHolder;
    private boolean mLinkMicDialogShowed;
    private Dialog mLinkMicWaitDialog;//连麦同意弹窗
    private boolean mLinkMicAgreeWaiting;//连麦同意等待
    private String mApplyLinkMicUid;//正在申请连麦的人的uid

    @Override
    protected void main() {
        super.main();
        mActivityType = ACTIVITYTYPE_ANCHOR;
        showData();
        mLiveAnimPresenter.playLiveStartAnim();
    }

    @Override
    protected LiveBottomFragment getBottomFragment() {
        return new LiveAnchorBottomFragment();
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case USER_LIST:
                getUserList();
                break;
            case LIVE_PAUSE:
                endLive();
                break;
            case LIVE_LINK_MIC_AGREE:
                linkMicNoResponse((String) msg.obj);
                break;
        }
    }


    public void liveAnchorClick(View v) {
        switch (v.getId()) {
            case R.id.btn_function://功能
                openFunctionWindow();
                break;
            case R.id.btn_close://关闭
                showCloseDilaog();
                break;
            case R.id.btn_close_game://关闭游戏
                mGameManager.anchorCloseGame();
                break;
        }
    }

    private void showData() {
        UserBean u = AppConfig.getInstance().getUserBean();
        mLiveUid = u.getId();
        Intent intent = getIntent();
        mLiveType = intent.getStringExtra("type");
        mTypeVal = intent.getStringExtra("typeVal");
        mLiveData = JSON.parseObject(intent.getStringExtra("data"));
        mStream = mLiveData.getString("stream");
        String pull = mLiveData.getString("pull_wheat");
        mLiveBean = new LiveBean();
        mLiveBean.setUid(mLiveUid);
        mLiveBean.setUser_nicename(u.getUser_nicename());
        mLiveBean.setAvatar(u.getAvatar());
        mLiveBean.setThumb(u.getAvatar_thumb());
        mLiveBean.setNums(mNumsVal + "");
        mLiveBean.setCity(u.getCity());
        mLiveBean.setPull(pull + mStream);
        mLiveBean.setStream(mStream);
        mLiveBean.setLevel_anchor(u.getLevel_anchor());
        mLiveBean.setGoodnum(u.getLiang().getName());
        mBarrageFee = mLiveData.getString("barrage_fee");
        mVotestotal = mLiveData.getString("votestotal");
        mUserlistRefreshTime = mLiveData.getIntValue("userlist_time") * 1000;
        mKickTime = mLiveData.getString("kick_time");
        mShutTime = mLiveData.getString("shut_time");

        //显示主播头头像，观看人数，房间号等
        ImgLoader.display(mLiveBean.getAvatar(), mAnchorAvatar);
        mAnchorLevel.setImageResource(IconUitl.getAnchorLiveDrawable(u.getLevel_anchor()));
        mNums.setText(String.valueOf(mNumsVal));
        mCoinName.setText(AppConfig.getInstance().getConfig().getName_votes() + "：");
        mVotes.setText(mVotestotal);
        String liangNum = mLiveBean.getGoodnum();
        if (!"0".equals(liangNum)) {
            mRoomName.setText(getString(R.string.room) + getString(R.string.liang));
            mRoomNum.setText(liangNum);
        } else {
            mRoomName.setText(getString(R.string.room));
            mRoomNum.setText(mLiveUid);
        }
        //加载游戏
        mGameManager = new GameManager(mContext, mFragmentManager, mStream, mLiveUid);
        mGameManager.setBankerGroup(mContent);
        mGameManager.setBankerInfo(
                mLiveData.getString("game_bankerid"),
                mLiveData.getString("game_banker_name"),
                mLiveData.getString("game_banker_avatar"),
                mLiveData.getString("game_banker_coin"),
                mLiveData.getString("game_banker_limit")
        );
        //连接socket
        AppConfig.getInstance().setSocketServer(mLiveData.getString("chatserver"));
        connectSocket();
        //加载推流fragment
        mPushStreamFragment = new LivePushStreamFragment();
        Bundle bundle = new Bundle();
        bundle.putString("streamUrl", mLiveData.getString("push"));
        mPushStreamFragment.setArguments(bundle);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.video_place, mPushStreamFragment).commit();


    }

    public void changeLive() {
        HttpUtil.changeLive(mStream, "1", new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                L.e("开播--->" + info[0]);
            }
        });
    }

    /**
     * 功能按钮点击事件
     *
     * @param id
     */
    public void functionClick(int id) {
        switch (id) {
            case FunctionBean.TIME://计时收费
                timeCharge();
                break;
            case FunctionBean.MEI_YAN://美颜
                openBeautyWindow();
                break;
            case FunctionBean.CAMERA://切换摄像头
                switchCamera();
                break;
            case FunctionBean.MUSIC://音乐伴奏
                openMusicWindow();
                break;
            case FunctionBean.GAME://游戏
                if(isLinkMic){
                    ToastUtil.show(WordUtil.getString(R.string.link_mic_not_game));
                }else{
                    openChooseGameWindow();
                }
                break;
            case FunctionBean.AUCTION://竞拍
                openAuctionWindow();
                break;
            case FunctionBean.FLASH://闪光灯
                toggleFlash();
                break;
        }
    }


    /**
     * 选择游戏
     */
    private void openChooseGameWindow() {
        if (mGameFragment == null) {
            mGameFragment = new ChooseGameFragment();
            Bundle bundle = new Bundle();
            bundle.putString("game_switch", mLiveData.getString("game_switch"));
            mGameFragment.setArguments(bundle);
            mGameFragment.setGameManager(mGameManager);
        }
        if (!mGameFragment.isAdded()) {
            mGameFragment.show(mFragmentManager, "ChooseGameFragment");
        }
    }

    /**
     * 计时收费
     */
    private void timeCharge() {
        if (mTimeChargeFragment == null) {
            mTimeChargeFragment = new LiveTimeChargeFragment();
            mTimeChargeFragment.setOnConfrimClick(new CommonCallback<String>() {
                @Override
                public void callback(String typeVal) {
                    mTypeVal = typeVal;
                    //切换收费模式
                    HttpUtil.changeLiveType(mStream, mTypeVal, mChangeTypeCallback);
                }
            });
        }
        if (!mTimeChargeFragment.isAdded()) {
            mTimeChargeFragment.show(getSupportFragmentManager(), "LiveTimeChargeFragment");
        }
    }


    private HttpCallback mChangeTypeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                ToastUtil.show(getString(R.string.change_time_charge));
                SocketUtil.getInstance().changeTimeCharge(mTypeVal);
            }
        }
    };


    /**
     * 切换摄像头
     */
    private void switchCamera() {
        if (mPushStreamFragment != null) {
            mPushStreamFragment.switchCamera();
        }
    }

    /**
     * 开启或关闭闪光灯
     */
    private void toggleFlash() {
        if (mPushStreamFragment != null) {
            mPushStreamFragment.toggleFlash();
        }
    }

    /**
     * 打开美颜窗口
     */
    private void openBeautyWindow() {
        //金山自带的旧版美颜
        if(mLiveEffectFragment==null){
            mLiveEffectFragment=new LiveEffectFragment();
        }
        mLiveEffectFragment.show(mFragmentManager, "LiveEffectFragment");

        /************分割线************/

//        //萌颜 新版美颜
//        if (mBeautyHolder == null) {
//            mBeautyHolder = new BeautyHolder(mContext, mWrap);
//            mBeautyHolder.setEffectListener(mEffectListener);
//        }
//        mBeautyHolder.show();
    }

    /**
     * 各种美颜效果的回调
     */
    private BeautyHolder.EffectListener mEffectListener = new BeautyHolder.EffectListener() {

        /**
         * 设置滤镜
         */
        @Override
        public void onFilterChanged(TiFilterEnum tiFilterEnum) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onFilterChanged(tiFilterEnum);
            }
        }

        @Override
        public void onRockChanged(TiRockEnum tiRockEnum) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onRockChanged(tiRockEnum);
            }
        }

        @Override
        public void onMeiBaiChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onMeiBaiChanged(progress);
            }
        }

        @Override
        public void onMoPiChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onMoPiChanged(progress);
            }
        }

        @Override
        public void onBaoHeChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onBaoHeChanged(progress);
            }
        }

        @Override
        public void onFengNenChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onFengNenChanged(progress);
            }
        }

        @Override
        public void onBigEyeChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onBigEyeChanged(progress);
            }
        }

        @Override
        public void onFaceChanged(int progress) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onFaceChanged(progress);
            }
        }

        @Override
        public void onTieZhiChanged(String tieZhiName) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onTieZhiChanged(tieZhiName);
            }
        }

        @Override
        public void onHaHaChanged(TiDistortionEnum tiDistortionEnum) {
            if (mPushStreamFragment != null) {
                mPushStreamFragment.onHaHaChanged(tiDistortionEnum);
            }
        }
    };

    /**
     * 获取美颜数值
     *
     * @return
     */
    public int[] getBeautyData() {
        if (mPushStreamFragment != null) {
            return mPushStreamFragment.getBeautyData();
        }
        return null;
    }

    /**
     * 设置美颜数值
     *
     * @param type
     * @param val
     */
    public void setBeautyData(int type, float val) {
        if (mPushStreamFragment != null) {
            mPushStreamFragment.setBeautyData(type, val);
        }
    }

    /**
     * 设置特殊滤镜
     *
     * @param id
     */
    public void setSpecialFilter(int id) {
        if (mPushStreamFragment != null) {
            mPushStreamFragment.setSpecialFilter(id);
        }
    }

    /**
     * 打开音乐搜索窗口
     */
    private void openMusicWindow() {
        if (mMusicFragment == null) {
            mMusicFragment = new LiveMusicFragment();
        }
        mMusicFragment.show(mFragmentManager, "LiveMusicFragment");
    }

    /**
     * 播放音乐
     */
    public void playMusic(String musicId, LiveLrcBean bean) {
        if (mPushStreamFragment != null) {
            mPushStreamFragment.playMusic(mWrap, musicId, bean);
        }
    }

    /**
     * 打开竞拍窗口
     */
    public void openAuctionWindow() {
        LiveAuctionFragment fragment = new LiveAuctionFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stream", mStream);
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "LiveAuctionFragment");
    }


    //打开功能窗口
    private void openFunctionWindow() {
        if (mFunctionFragment == null) {
            mFunctionFragment = new LiveFunctionFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("auction_switch", mLiveData.getIntValue("auction_switch"));
            bundle.putString("game_switch", mLiveData.getString("game_switch"));
            bundle.putString("type", mLiveType);
            mFunctionFragment.setArguments(bundle);
        }
        if (!mFunctionFragment.isAdded()) {
            mFunctionFragment.show(mFragmentManager, "LiveFunctionFragment");
        }
    }

    /**
     * 竞拍的时候显示小窗口
     */
    public void setCameraPreView(GLSurfaceView surfaceView) {
        mPushStreamFragment.setCameraPreView(surfaceView);
    }

    /**
     * 竞拍页面返回的时候恢复大窗口
     */
    public void setOriginCameraPreView() {
        mPushStreamFragment.setOriginCameraPreView();
    }


    @Override
    public void onBackPressed() {
        if (mEnd) {
            super.onBackPressed();
        } else {
            showCloseDilaog();
        }
    }

    /**
     * 显示关闭直播间的弹窗
     */
    private void showCloseDilaog() {
        DialogUitl.confirmDialog(mContext,
                getString(R.string.tip),
                getString(R.string.are_you_end_live),
                new DialogUitl.Callback() {
                    @Override
                    public void confirm(Dialog dialog) {
                        dialog.dismiss();
                        endLive();
                    }

                    @Override
                    public void cancel(Dialog dialog) {
                        dialog.dismiss();
                    }
                }
        ).show();
    }


    /**
     * 结束直播
     */
    private void endLive() {
        HttpUtil.stopRoom(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    closeRoom();
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, getString(R.string.closing_live));
            }
        });
    }

    /**
     * @param isSuperClose 是否是超管关闭的，如果是超管关闭的，要显示弹窗
     */
    private void closeLiveRoom(boolean isSuperClose) {
        onClose();
        //停止推流
        mPushStreamFragment.stopPushStream();
        //显示结束的fragment
        LiveAnchorEndFragment fragment = new LiveAnchorEndFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stream", mStream);
        bundle.putBoolean("isSuperClose", isSuperClose);
        fragment.setArguments(bundle);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.wrap, fragment);
        if (mPaused) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }
    }

    @Override
    public void closeRoom() {
        closeLiveRoom(false);
    }

    /**
     * 超管关闭直播间
     */
    @Override
    public void onSuperCloseLive() {
        closeLiveRoom(true);
    }


    @Override
    public void onNetWorkErrorCloseRoom() {
        closeRoom();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //主播离开50秒后关闭直播间
        mHandler.sendEmptyMessageDelayed(LIVE_PAUSE, 50 * 1000);
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //主播在50秒之内回来了
        mHandler.removeMessages(LIVE_PAUSE);
        mPaused = false;
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.CHANGE_LIVE);
        HttpUtil.cancel(HttpUtil.STOP_ROOM);
        HttpUtil.cancel(HttpUtil.CHANGE_LIVE_TYPE);
        HttpUtil.cancel(HttpUtil.KICKING);
        HttpUtil.cancel(HttpUtil.SET_SHUT_UP);
        HttpUtil.cancel(HttpUtil.SUPER_STOP_ROOM);
        super.onDestroy();
//        if (mBeautyHolder != null) {
//            mBeautyHolder.setEffectListener(null);
//            mBeautyHolder.release();
//        }

    }

    @Override
    public void onDisConnect() {
        if (mGameManager != null) {
            mGameManager.setSocketConn(false);
        }
    }

    @Override
    public void onConnect(boolean successConn) {
        super.onConnect(successConn);
        mGameManager.setSocketConn(true);
    }

    @Override
    public void onLinkMicApply(final String uid, final String username) {
        mApplyLinkMicUid = uid;
        if (!isLinkMic&&!mLinkMicDialogShowed) {
            mLinkMicWaitDialog = DialogUitl.confirmDialog(mContext, WordUtil.getString(R.string.tip),
                    username + WordUtil.getString(R.string.apply_linkMic), WordUtil.getString(R.string.agree),
                    WordUtil.getString(R.string.refuse), false, new DialogUitl.Callback() {
                        @Override
                        public void confirm(Dialog dialog) {
                            dialog.dismiss();
                            mLinkMicDialogShowed = false;
                            mLinkMicAgreeWaiting = false;
                            mHandler.removeMessages(LIVE_LINK_MIC_AGREE);
                            SocketUtil.getInstance().agreeLinkMic(uid);
                        }

                        @Override
                        public void cancel(Dialog dialog) {
                            mApplyLinkMicUid = null;
                            dialog.dismiss();
                            mLinkMicDialogShowed = false;
                            mLinkMicAgreeWaiting = false;
                            SocketUtil.getInstance().refuseLinkMic(uid);
                            mHandler.removeMessages(LIVE_LINK_MIC_AGREE);

                        }
                    });
            if (!this.isFinishing()) {
                mLinkMicWaitDialog.show();
            }
            mLinkMicDialogShowed = true;
            if (!mLinkMicAgreeWaiting) {
                mLinkMicAgreeWaiting = true;
                Message msg = Message.obtain();
                msg.what = LIVE_LINK_MIC_AGREE;
                msg.obj = uid;
                mHandler.sendMessageDelayed(msg, 10000);
            }
        } else {
            SocketUtil.getInstance().anchorBusy(uid);
        }
    }

    /**
     * 主播连麦无响应
     */
    private void linkMicNoResponse(String touid) {
        if (mLinkMicWaitDialog != null) {
            mLinkMicWaitDialog.dismiss();
        }
        mLinkMicDialogShowed = false;
        mLinkMicAgreeWaiting = false;
        SocketUtil.getInstance().anchorNotResponse(touid);
    }

    /**
     * 主播收到其他将要连麦的人发过来的流地址
     */
    @Override
    public void onSendLinkMicUrl(String uid, String uname, String playUrl) {
        if (TextUtils.isEmpty(uid) || !uid.equals(mApplyLinkMicUid)) {
            return;
        }
        mApplyLinkMicUid=null;
        HttpUtil.linkMicShowVideo(uid, playUrl);
        mLinkMicUid = uid;
        mLinkMicName = uname;
        isLinkMic = true;
        mLinkMicViewHolder
                .setType(LinkMicViewHolder.LINK_MIC_PLAY)
                .setUrl(playUrl)
                .start(true);
    }

    /**
     * 主播关闭某人连麦
     */
    @Override
    public void onPlayClose() {
        if (isLinkMic && !TextUtils.isEmpty(mLinkMicUid) && !TextUtils.isEmpty(mLinkMicName)) {
            SocketUtil.getInstance().kickLinkMic(mLinkMicUid, mLinkMicName);
        }
    }


    @Override
    public void onLeaveRoom(UserBean bean) {
        super.onLeaveRoom(bean);
        if (bean != null) {
            String uid = bean.getId();
            if (!TextUtils.isEmpty(uid)) {
                if (uid.equals(mApplyLinkMicUid)) {
                    if (mLinkMicWaitDialog != null) {
                        mLinkMicWaitDialog.dismiss();
                        mLinkMicDialogShowed = false;
                        mLinkMicAgreeWaiting = false;
                        mHandler.removeMessages(LIVE_LINK_MIC_AGREE);
                    }
                } else if (uid.equals(mLinkMicUid)) {
                    onLinkMicClose(uid, bean.getUser_nicename());
                }
            }
        }
    }
}
