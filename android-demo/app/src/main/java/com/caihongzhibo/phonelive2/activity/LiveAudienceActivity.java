package com.caihongzhibo.phonelive2.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.GiftBean;
import com.caihongzhibo.phonelive2.bean.UserBean;
import com.caihongzhibo.phonelive2.custom.linkmic.LinkMicViewHolder;
import com.caihongzhibo.phonelive2.event.JPushEvent;
import com.caihongzhibo.phonelive2.fragment.LiveAudienceBottomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveAudienceEndFragment;
import com.caihongzhibo.phonelive2.fragment.LiveBottomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveGiftFragment;
import com.caihongzhibo.phonelive2.fragment.LivePullStreamFragment;
import com.caihongzhibo.phonelive2.fragment.LiveShareFragment;
import com.caihongzhibo.phonelive2.fragment.PullStreamPlayer;
import com.caihongzhibo.phonelive2.fragment.TimeChargeFragment;
import com.caihongzhibo.phonelive2.game.GameManager;
import com.caihongzhibo.phonelive2.game.LastCoinEvent;
import com.caihongzhibo.phonelive2.glide.ImgLoader;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.CommonCallback;
import com.caihongzhibo.phonelive2.presenter.CheckLivePresenter;
import com.caihongzhibo.phonelive2.socket.SocketUtil;
import com.caihongzhibo.phonelive2.utils.DialogUitl;
import com.caihongzhibo.phonelive2.utils.IconUitl;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cxf on 2017/8/19.
 * 观众直播间
 */

public class LiveAudienceActivity extends LiveActivity {

    private LiveAudienceBottomFragment mBottomFragment;
    private LiveGiftFragment mGiftFragment;
    private GiftBean mTempGiftBean;//当前选择的礼物
    private int mIsAttention;//是否关注了主播
    private PullStreamPlayer mPullStreamPlayer;
    private int mLiveType;//直播间的类型  普通 密码 门票 计时等
    private int mLiveTypeVal;//收费价格,计时收费每次扣费的值
    private int mTimeChargeInterval = 60000;//计时收费每次扣费的时间间隔，毫秒
    private TimeChargeFragment mTimeChargeFragment;
    private final int CHARGE_CODE = 1000;
    private LiveShareFragment mShareFragment;
    private boolean mPaused;
    private boolean mVideoLoadSucceed;
    private static final int REQUEST_LINK_MIC_PERMISSION = 100;
    protected boolean isApplyLinkMic;//是否正在申请连麦
    private String mLinkMicPushUrl;//连麦推流地址
    private String mLinkMicPlayUrl;//连麦播流地址

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mHandler.removeCallbacksAndMessages(null);
        SocketUtil.getInstance().close();
        mUserAdapter.clear();
        mChatListAdapter.clear();
        initRoomParams(intent);
    }

    @Override
    protected void main() {
        super.main();
        mRoot.setScrollView(mWrap);
        mPullStreamPlayer = new LivePullStreamFragment();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.video_place, mPullStreamPlayer).commit();
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEnd) {
                    //点亮，飘心
                    mLiveAnimPresenter.floatHeart();
                }
            }
        });
        initRoomParams(getIntent());
    }

    /**
     * 初始化房间内的各种参数
     */
    private void initRoomParams(Intent intent) {
        if (mBg.getVisibility() == View.GONE) {
            mBg.setVisibility(View.VISIBLE);
        }
        mLiveBean = intent.getParcelableExtra("liveBean");
        mLiveType = intent.getIntExtra("type", 0);
        mLiveTypeVal = intent.getIntExtra("typeVal", 0);
        mLiveUid = mLiveBean.getUid();
        mStream = mLiveBean.getStream();
        ImgLoader.displayBitmap(mLiveBean.getThumb(), new ImgLoader.BitmapCallback() {
            @Override
            public void callback(Bitmap bitmap) {
                if (!mVideoLoadSucceed && mBg != null) {
                    mBg.setImageBitmap(bitmap);
                } else {
                    bitmap.recycle();
                }
            }
        });
        if (mLiveType == 3) {
            mHandler.sendEmptyMessageDelayed(TIME_CHARGE, mTimeChargeInterval);
        }
        mGameManager = new GameManager(mContext, mFragmentManager, mStream, mLiveUid);
        mGameManager.setBankerGroup(mContent);
        enterRoom();
    }

    @Override
    protected LiveBottomFragment getBottomFragment() {
        mBottomFragment = new LiveAudienceBottomFragment();
        return mBottomFragment;
    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case TIME_CHARGE://计时收费的时候用，每隔一段时间扣一次费
                timeCharge();
                break;
            case USER_LIST:
                getUserList();
                break;
            case LIVE_LINK_APPLY_AGAIN:
                isApplyLinkMic = false;
                break;
        }
    }

    /**
     * 请求enterRoom接口，告诉服务器我进了这个直播间,同时获取直播间的相关信息
     */
    private void enterRoom() {
        HttpUtil.enterRoom(mLiveUid, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    AppConfig.getInstance().setSocketServer(obj.getString("chatserver"));
                    connectSocket();
                    //弹幕价格
                    mBarrageFee = obj.getString("barrage_fee");
                    //主播映票数量
                    mVotestotal = obj.getString("votestotal");
                    //刷新用户列表的间隔时间
                    mUserlistRefreshTime = obj.getIntValue("userlist_time") * 1000;
                    mKickTime = obj.getString("kick_time");
                    mShutTime = obj.getString("shut_time");
                    //观众数量
                    mNumsVal = obj.getIntValue("nums");
                    //是否关注了主播 0未关注  1 已关注
                    mIsAttention = obj.getIntValue("isattention");
                    //观众列表
                    mUserList = JSON.parseArray(obj.getString("userlists"), UserBean.class);
                    showData();
                    mGameManager.setBankerInfo(
                            obj.getString("game_bankerid"),
                            obj.getString("game_banker_name"),
                            obj.getString("game_banker_avatar"),
                            obj.getString("game_banker_coin"),
                            obj.getString("game_banker_limit")
                    );
                    int gameaction = obj.getIntValue("gameaction");
                    if (gameaction != 0) {
                        mGameManager.enterRoomStartGame(
                                gameaction,
                                obj.getString("gameid"),
                                obj.getIntValue("gametime"),
                                obj.getObject("game", int[].class),
                                obj.getObject("gamebet", int[].class));
                    }
                    String linkmic_uid = obj.getString("linkmic_uid");
                    if (!TextUtils.isEmpty(linkmic_uid) && !"0".equals(linkmic_uid)) {
                        String linkmic_pull = obj.getString("linkmic_pull");
                        if (!TextUtils.isEmpty(linkmic_pull)) {
                            mLinkMicViewHolder
                                    .setType(LinkMicViewHolder.LINK_MIC_PLAY)
                                    .setUrl(linkmic_pull)
                                    .start(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * 视频显示出来，加载成功
     */
    public void videoLoadSucess() {
        mVideoLoadSucceed = true;
    }

    /**
     * 隐藏背景图片
     */
    public void hideBackgroundImage() {
        mBg.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBg.setVisibility(View.GONE);
                mBg.setAlpha(1f);
            }
        }).start();
    }

    private void showData() {
        ImgLoader.display(mLiveBean.getAvatar(), mAnchorAvatar);
        mAnchorLevel.setImageResource(IconUitl.getAnchorLiveDrawable(mLiveBean.getLevel_anchor()));
        mNums.setText(String.valueOf(mNumsVal));
        String coinName = AppConfig.getInstance().getConfig().getName_votes();
        mCoinName.setText(coinName + "：");
        mVotes.setText(mVotestotal);
        String liangNum = mLiveBean.getGoodnum();
        if (!"0".equals(liangNum)) {
            mRoomName.setText(getString(R.string.room) + getString(R.string.liang));
            mRoomNum.setText(liangNum);
        } else {
            mRoomName.setText(getString(R.string.room));
            mRoomNum.setText(mLiveBean.getUid());
        }
        if (mIsAttention == 0) {
            mBtnAttention.setVisibility(View.VISIBLE);
        }
        mUserAdapter.addUserList(mUserList);
        mPullStreamPlayer.play(mLiveBean.getPull());
    }

    /**
     * 显示正在进行的竞拍
     */
    private void showAuction(JSONObject auction) {
        auctionStart(false, auction.getString("id"),
                auction.getString("thumb"),
                auction.getString("title"),
                auction.getString("price_start"),
                auction.getIntValue("long"));
        String bidUid = auction.getString("bid_uid");
        if (!"0".equals(bidUid)) {
            auctionAddMoney(auction.getString("avatar"), auction.getString("user_nicename"), auction.getString("bid_price"));
        }
    }

    public void liveAudienceClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share://分享
                openShareWindow();
                break;
            case R.id.btn_gift://礼物
                openGiftWindow();
                break;
            case R.id.btn_close://关闭
                onBackPressed();
                break;
            case R.id.btn_attention:
                attentAnchor();
                break;
            case R.id.btn_lianmai://连麦
                lianmai();
                break;
        }
    }


    /**
     * 关注主播
     */
    private void attentAnchor() {
        HttpUtil.setAttention(mLiveUid, mAttentionCallback);
    }

    private CommonCallback<Integer> mAttentionCallback = new CommonCallback<Integer>() {
        @Override
        public void callback(Integer isAttention) {
            if (isAttention == 1) {
                SocketUtil.getInstance().sendSystemMessage(AppConfig.getInstance().getUserBean().getUser_nicename() + getString(R.string.attention_anchor));
            }
        }
    };

    /**
     * 打开分享窗口
     */
    private void openShareWindow() {
        if (mShareFragment == null) {
            mShareFragment = new LiveShareFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("live", mLiveBean);
            mShareFragment.setArguments(bundle);
        }
        if (!mShareFragment.isAdded()) {
            mShareFragment.show(mFragmentManager, "LiveShareFragment");
        }
    }

    /**
     * 打开送礼物的窗口
     */
    private void openGiftWindow() {
        if (mGiftFragment == null) {
            mGiftFragment = new LiveGiftFragment();
        }
        if (!mGiftFragment.isAdded()) {
            mGiftFragment.show(mFragmentManager, "GiftFragment");
        }
    }

    /**
     * 请求送礼物接口
     */
    public void sendGift(GiftBean bean) {
        mTempGiftBean = bean;
        HttpUtil.sendGift(mLiveUid, bean.getId(), "1", mStream, mSendGiftCallback);
    }

    private HttpCallback mSendGiftCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                String lastCoin = obj.getString("coin");
                mGiftFragment.updateCoin(lastCoin);
                AppConfig.getInstance().getUserBean().setLevel(obj.getIntValue("level"));
                SocketUtil.getInstance().sendGift(mTempGiftBean.getEvensend(), obj.getString("gifttoken"));
                EventBus.getDefault().post(new LastCoinEvent(lastCoin));
            } else {
                ToastUtil.show(msg);
            }
        }
    };


    @Override
    public void onBackPressed() {
        onClose();
        //停止播放
        if (mPullStreamPlayer != null) {
            mPullStreamPlayer.pausePlay();
        }
        finish();
    }

    @Override
    public void onKick(String touid) {
        if (touid.equals(AppConfig.getInstance().getUid())) {//被踢的是自己
            onBackPressed();
            ToastUtil.show(getString(R.string.you_are_kicked));
        }
    }

    @Override
    public void onShutUp(String touid, String content) {
        if (touid.equals(AppConfig.getInstance().getUid())) {
            DialogUitl.messageDialog(mContext, getString(R.string.tip), content, null).show();
        }
    }


    @Override
    public void onConnect(boolean successConn) {
        super.onConnect(successConn);
        if (successConn) {
            if (mLiveType == 2 || mLiveType == 3) {
                //发送socket更新直播间的映票
                SocketUtil.getInstance().updateVotes("1", mLiveTypeVal);
            }
        }
    }

    /**
     * 主播切换计时收费或更改计时收费的价格
     */
    @Override
    public void onChangeTimeCharge(int typeVal) {
        mLiveTypeVal = typeVal;
        mHandler.removeMessages(0);
        if (mPullStreamPlayer != null) {
            mPullStreamPlayer.pausePlay();
        }
        if (mBg.getVisibility() == View.GONE) {
            mBg.setVisibility(View.VISIBLE);
        }
        showRoomChargeDialog(getString(R.string.room_charge) + mLiveTypeVal + AppConfig.getInstance().getConfig().getName_coin());
    }

    private void showRoomChargeDialog(String msg) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (mTimeChargeFragment == null) {
            mTimeChargeFragment = new TimeChargeFragment();
        }
        mTimeChargeFragment.setMessage(msg);
        if (!mTimeChargeFragment.isAdded()) {
            mTimeChargeFragment.show(mFragmentManager, "TimeChargeFragment");
        } else {
            mTimeChargeFragment.showMessage();
        }
    }

    /**
     * 计时收费的时候向主播交钱
     */
    public void roomCharge() {
        HttpUtil.roomCharge(mLiveUid, mStream, mRoomChargeCallback);
        mHandler.sendEmptyMessageDelayed(TIME_CHARGE, mTimeChargeInterval);
    }

    private HttpCallback mRoomChargeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                //发送socket更新直播间的映票
                SocketUtil.getInstance().updateVotes("0", mLiveTypeVal);
                hideBackgroundImage();
                if (mPullStreamPlayer != null) {
                    mPullStreamPlayer.resumePlay();
                }
            } else {
                mHandler.removeMessages(TIME_CHARGE);
                if (mPullStreamPlayer != null) {
                    mPullStreamPlayer.pausePlay();
                }
                if (mBg.getVisibility() == View.GONE) {
                    mBg.setVisibility(View.VISIBLE);
                }
                if (code == 1008) {//如果是余额不足，显示充值弹窗
                    showMoneyNotEnoughDialog(getString(R.string.coin_not_enough));
                } else {
                    ToastUtil.show(msg);
                }
            }
        }
    };


    /**
     * 定时器计时收费
     */
    private void timeCharge() {
        HttpUtil.timeCharge(mLiveUid, mStream, mTimeChargeCallback);
        mHandler.sendEmptyMessageDelayed(TIME_CHARGE, mTimeChargeInterval);
    }

    private HttpCallback mTimeChargeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                //发送socket更新直播间的映票
                SocketUtil.getInstance().updateVotes("0", mLiveTypeVal);
            } else {
                mHandler.removeMessages(TIME_CHARGE);
                if (mPullStreamPlayer != null) {
                    mPullStreamPlayer.pausePlay();
                }
                if (mBg.getVisibility() == View.GONE) {
                    mBg.setVisibility(View.VISIBLE);
                }
                if (code == 1008) {//如果是余额不足，显示充值弹窗
                    showMoneyNotEnoughDialog(getString(R.string.coin_not_enough));
                } else {
                    ToastUtil.show(msg);
                }
            }
        }
    };


    /**
     * 显示余额不足的充值弹窗
     */
    private void showMoneyNotEnoughDialog(String msg) {
        DialogUitl.confirmDialog(mContext, getString(R.string.tip), msg, false, new DialogUitl.Callback() {
            @Override
            public void confirm(Dialog dialog) {
                dialog.dismiss();
                Intent intent = new Intent(mContext, ChargeActivity.class);
                intent.putExtra("from", "live");
                startActivityForResult(intent, CHARGE_CODE);
            }

            @Override
            public void cancel(Dialog dialog) {
                dialog.dismiss();
                onBackPressed();
            }
        }).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHARGE_CODE) {
            if (resultCode == RESULT_OK) {
                showRoomChargeDialog(getString(R.string.contiune_watch_live));
            } else {
                showMoneyNotEnoughDialog(getString(R.string.charge_failed));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    /**
     * 直播被关闭
     */
    @Override
    public void onLiveEnd() {
        closeRoom();
    }

    @Override
    public void closeRoom() {
        onClose();
        //停止播放
        if (mPullStreamPlayer != null) {
            mPullStreamPlayer.pausePlay();
        }
        //显示结束的fragment
        LiveAudienceEndFragment fragment = new LiveAudienceEndFragment();
        Bundle bundle = new Bundle();
        bundle.putString("anchorAvatar", mLiveBean.getAvatar());
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
    public void onNetWorkErrorCloseRoom() {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.SET_ATTENTION);
        HttpUtil.cancel(HttpUtil.SEND_GIFT);
        HttpUtil.cancel(HttpUtil.ENTER_ROOM);
        HttpUtil.cancel(HttpUtil.ROOM_CHARGE);
        HttpUtil.cancel(HttpUtil.TIME_CHARGE);
        HttpUtil.cancel(HttpUtil.GET_LINKMICSTREAM);
        super.onDestroy();
    }

    /**
     * 点击推送消息  先关闭房间然后进入推送房间
     *
     * @param e
     */
    @Override
    public void jpushEvent(JPushEvent e) {
        super.jpushEvent(e);
        if (mPullStreamPlayer != null) {
            mPullStreamPlayer.pausePlay();
        }
        finish();
        CheckLivePresenter mCheckLivePresenter = new CheckLivePresenter(mContext);
        mCheckLivePresenter.setSelectLiveBean(e);
        mCheckLivePresenter.watchLive();
    }

    private void lianmai() {
        if (mGameManager.isGaming()) {
            ToastUtil.show(WordUtil.getString(R.string.link_mic_not_game_2));
        } else {
            if (isLinkMic) {
                SocketUtil.getInstance().exitLinkMic();
            } else {
                checkLianmaiPermission();
            }
        }
    }

    /**
     * 检查连麦权限 即拍照 录音 存储的权限
     */
    private void checkLianmaiPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                        },
                        REQUEST_LINK_MIC_PERMISSION);
            } else {
                requestLinkMic();
            }
        } else {
            requestLinkMic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LINK_MIC_PERMISSION:
                if (isAllGranted(permissions, grantResults)) {
                    requestLinkMic();
                }
                break;
        }
    }

    //判断申请的权限有没有被允许
    private boolean isAllGranted(String[] permissions, int[] grantResults) {
        boolean isAllGranted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                showTip(permissions[i]);
            }
        }
        return isAllGranted;
    }

    //拒绝某项权限时候的提示
    private void showTip(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                ToastUtil.show(getString(R.string.storage_permission_refused));
                break;
            case Manifest.permission.CAMERA:
                ToastUtil.show(getString(R.string.camera_permission_refused));
                break;
            case Manifest.permission.RECORD_AUDIO:
                ToastUtil.show(getString(R.string.record_audio_permission_refused));
                break;
        }
    }

    /**
     * 连麦,正式向主播发出连麦申请
     */
    private void requestLinkMic() {
        if (!isApplyLinkMic) {
            isApplyLinkMic = true;
            SocketUtil.getInstance().applyLinkMic();
            ToastUtil.show(WordUtil.getString(R.string.lianmai_apply));
            mHandler.sendEmptyMessageDelayed(LIVE_LINK_APPLY_AGAIN, 10000);//10秒后可以重新申请连麦
        } else {
            ToastUtil.show(WordUtil.getString(R.string.lianmai_apply_waiting));
        }
    }

    /**
     * 连麦,主播同意连麦
     */
    @Override
    public void onAgreeLinkMic() {
        isApplyLinkMic = false;
        ToastUtil.show(WordUtil.getString(R.string.link_mic_agree));
        HttpUtil.getLinkMicStream(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mLinkMicPushUrl = obj.getString("pushurl");
                    mLinkMicPlayUrl = obj.getString("playurl");
                    L.e("getLinkMicStream", "pushurl--推流地址--->" + mLinkMicPushUrl);
                    L.e("getLinkMicStream", "playurl--播放地址--->" + mLinkMicPlayUrl);
                    mLinkMicUid = AppConfig.getInstance().getUid();
                    isLinkMic = true;
                    mLinkMicViewHolder
                            .setType(LinkMicViewHolder.LINK_MIC_PUSH)
                            .setUrl(mLinkMicPushUrl)
                            .start(false);
                }
            }
        });
    }

    /**
     * 连麦,收到别的连麦的观众发来的播放地址
     */
    @Override
    public void onSendLinkMicUrl(String uid, String uname, String playUrl) {
        if (!TextUtils.isEmpty(uid) && !uid.equals(AppConfig.getInstance().getUid())) {
            mLinkMicUid = uid;
            mLinkMicViewHolder
                    .setType(LinkMicViewHolder.LINK_MIC_PLAY)
                    .setUrl(playUrl)
                    .start(false);
        }
    }

    /**
     * 连麦,观众推流成功
     */
    @Override
    public void onPushSuccess() {
        SocketUtil.getInstance().sendLinkMicUrl(mLinkMicPlayUrl);
        if (mBottomFragment != null) {
            mBottomFragment.setLinkIcon(true);
        }
    }

    /**
     * 连麦,主播主动关闭用户的连麦
     */
    @Override
    public void onLinkMicClose(String uid, String uname) {
        super.onLinkMicClose(uid, uname);
        if (!TextUtils.isEmpty(uid) && uid.equals(AppConfig.getInstance().getUid())) {
            if (mBottomFragment != null) {
                mBottomFragment.setLinkIcon(false);
            }
        }
    }

    /**
     * 连麦,主播拒绝连麦
     */
    @Override
    public void onRefuseLinkMic() {
        ToastUtil.show(WordUtil.getString(R.string.link_mic_refuse));
        isApplyLinkMic = false;
    }

    /**
     * 连麦,主播无响应
     */
    @Override
    public void onAnchorNotResponse() {
        ToastUtil.show(WordUtil.getString(R.string.lianmai_anchor_not_response));
        isApplyLinkMic = false;
    }

    /**
     * 连麦,主播正在忙
     */
    @Override
    public void onAnchorBusy() {
        ToastUtil.show(WordUtil.getString(R.string.lianmai_anchor_busy));
        isApplyLinkMic = false;
    }

    /**
     * 连麦推流失败
     */
    @Override
    public void onPushFailed() {
        DialogUitl.messageDialog(mContext, WordUtil.getString(R.string.tip), WordUtil.getString(R.string.link_mic_push_failed), new DialogUitl.Callback2() {
            @Override
            public void confirm(Dialog dialog) {
                dialog.dismiss();
            }
        }).show();
        //子类各自实现自己的逻辑
        SocketUtil.getInstance().exitLinkMic();
    }
}
