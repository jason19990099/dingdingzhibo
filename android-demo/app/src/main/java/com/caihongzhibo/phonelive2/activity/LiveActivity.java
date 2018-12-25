package com.caihongzhibo.phonelive2.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.adapter.LiveChatListAdapter;
import com.caihongzhibo.phonelive2.adapter.LiveUserAdapter;
import com.caihongzhibo.phonelive2.bean.LiveBean;
import com.caihongzhibo.phonelive2.bean.LiveChatBean;
import com.caihongzhibo.phonelive2.bean.ReceiveDanMuBean;
import com.caihongzhibo.phonelive2.bean.ReceiveGiftBean;
import com.caihongzhibo.phonelive2.bean.UserBean;
import com.caihongzhibo.phonelive2.custom.DragLayout;
import com.caihongzhibo.phonelive2.custom.FrameAnimImageView;
import com.caihongzhibo.phonelive2.custom.MyLinearLayoutManger;
import com.caihongzhibo.phonelive2.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive2.custom.auction.AuctionWindow;
import com.caihongzhibo.phonelive2.custom.linkmic.LinkMicViewHolder;
import com.caihongzhibo.phonelive2.event.AttentionEvent;
import com.caihongzhibo.phonelive2.event.ConnEvent;
import com.caihongzhibo.phonelive2.event.IgnoreUnReadEvent;
import com.caihongzhibo.phonelive2.event.LiveRoomCloseEvent;
import com.caihongzhibo.phonelive2.event.VisibleHeightEvent;
import com.caihongzhibo.phonelive2.fragment.EMChatFragment;
import com.caihongzhibo.phonelive2.fragment.EMChatRoomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveAdminListFragment;
import com.caihongzhibo.phonelive2.fragment.LiveBottomFragment;
import com.caihongzhibo.phonelive2.fragment.LiveInputFragment;
import com.caihongzhibo.phonelive2.fragment.LiveOrderFragment;
import com.caihongzhibo.phonelive2.fragment.LiveSettingFragment;
import com.caihongzhibo.phonelive2.fragment.LiveUserInfoFragment;
import com.caihongzhibo.phonelive2.fragment.NetWorkErrorFragment;
import com.caihongzhibo.phonelive2.game.GameManager;
import com.caihongzhibo.phonelive2.game.LastCoinEvent;
import com.caihongzhibo.phonelive2.glide.TopGradual;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.im.JIM;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive2.presenter.LiveAnimPresenter;
import com.caihongzhibo.phonelive2.socket.SocketMsgListener;
import com.caihongzhibo.phonelive2.socket.SocketUtil;
import com.caihongzhibo.phonelive2.utils.DpUtil;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2017/8/15.
 */

public abstract class LiveActivity extends AbsActivity2 implements SocketMsgListener, LinkMicViewHolder.LinkMicCallback {

    private View mDecorView;
    protected DragLayout mRoot;//根布局
    protected ImageView mBg;//背景图
    protected ViewGroup mWrap;//包裹内容区域的外层布局,里面可以动态添加动画等
    protected ViewGroup mContent;//内容区域，包含常规界面聊天等 和游戏区域
    private View mGamePlaceholder;//游戏区域
    private RecyclerView mChatView;//聊天栏
    private ViewGroup mLinkMicGroup;
    protected LinkMicViewHolder mLinkMicViewHolder;
    private View mBottom;//底部栏
    protected LiveChatListAdapter mChatListAdapter;
    protected FragmentManager mFragmentManager;
    private LiveBottomFragment mBottomFragment;//底部菜单
    private int mUnReadCount;//环信未读消息数量
    private int mScreenHeight;//屏幕的高度
    private LiveInputFragment mInputFragment;//输入框
    private boolean mSoftInputShowed;//软键盘是否开启的标识符
    private boolean mChatRoomFragmentShowed;//私信聊天窗口是否打开的标识符
    private int mVisibleHeight;//可视区的高度
    private Rect mRect = new Rect();

    protected ImageView mAnchorAvatar;//主播头像
    protected ImageView mAnchorLevel;//主播等级
    protected TextView mNums;//观看人数
    protected TextView mCoinName;//映票名字
    protected TextView mVotes;//映票数
    protected TextView mRoomName;//房间名字，默认是"房间"二字，如果有靓号，则是"房间：靓"
    protected TextView mRoomNum;//房间号
    protected View mBtnAttention;//关注按钮
    protected RecyclerView mUserView;//观众列表
    protected LiveBean mLiveBean;//主播的信息
    protected String mBarrageFee;//弹幕价格
    protected String mVotestotal;//主播映票数量
    protected int mUserlistRefreshTime;//刷新用户列表的间隔时间
    protected String mKickTime;//踢人时间
    protected String mShutTime;//禁言时间
    protected int mNumsVal;//观众数量
    protected List<UserBean> mUserList;//观众列表
    protected LiveUserAdapter mUserAdapter;
    protected LiveAnimPresenter mLiveAnimPresenter;//各种动画的Presenter
    protected String mLiveUid;//主播uid
    protected String mStream;//当前直播间的stream
    private AuctionWindow mAuctionWindow;//竞拍窗口
    private boolean mMaxUserNum;//用户最大数量是否超过了20个
    //计时收费的message的what
    protected static final int TIME_CHARGE = 0;
    //定时刷新用户列表的message的what
    protected static final int USER_LIST = 1;
    //直播暂停（主播直播暂停50秒后关闭直播）
    protected static final int LIVE_PAUSE = 2;
    //主播连麦等待延时
    protected static final int LIVE_LINK_MIC_AGREE = 3;
    //观众再次申请连麦延时
    protected static final int LIVE_LINK_APPLY_AGAIN = 4;
    protected boolean mEnd;
    protected Handler mHandler;
    protected GameManager mGameManager;
    private boolean mFirstConn;//是否是第一次连接socket
    private boolean mNetWorkBroken;//网络是否断开了
    private JIM mIM;
    protected boolean isLinkMic;//是否已经连麦
    protected String mLinkMicUid;//正在连麦的人的uid
    protected String mLinkMicName;//正在连麦的人的uname

    //加载底部布局的抽象方法
    protected abstract LiveBottomFragment getBottomFragment();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live;
    }

    protected abstract void handleMsg(Message msg);

    @Override
    protected void main() {
        mDecorView = getWindow().getDecorView();
        mRoot = (DragLayout) findViewById(R.id.root);
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                mScreenHeight = mRoot.getHeight();
                mLiveAnimPresenter.setScreenDimens(mRoot.getWidth(), mScreenHeight);
            }
        });
        mBg = (ImageView) findViewById(R.id.bg);
        mWrap = (ViewGroup) findViewById(R.id.wrap);
        mContent = (ViewGroup) findViewById(R.id.content);
        mLinkMicGroup = (ViewGroup) findViewById(R.id.link_mic_group);
        mLinkMicViewHolder = new LinkMicViewHolder(mContext, mLinkMicGroup);
        mLinkMicViewHolder.setActionListener(this);
        mBottom = findViewById(R.id.repalced_bottom);
        mGamePlaceholder = findViewById(R.id.repalced_game);
        mChatView = (RecyclerView) findViewById(R.id.chat_list);
        mChatView.setHasFixedSize(true);
        mChatView.setLayoutManager(new MyLinearLayoutManger(mContext, LinearLayoutManager.VERTICAL, false));
        mChatView.setItemAnimator(new NoAlphaItemAnimator());
        mChatView.addItemDecoration(new TopGradual());
        mChatListAdapter = new LiveChatListAdapter(mContext);
        mChatListAdapter.setOnItemClickListener(new OnItemClickListener<LiveChatBean>() {
            @Override
            public void onItemClick(LiveChatBean item, int position) {
                if (item.getType() != LiveChatBean.SYSTEM) {//如果不是系统消息，点击打开弹窗
                    openUserInfoDialog(item.getId());
                }
            }
        });
        mChatView.setAdapter(mChatListAdapter);
        mAnchorAvatar = (ImageView) findViewById(R.id.anchor_avatar);
        mAnchorLevel = (ImageView) findViewById(R.id.anchor_level);
        mNums = (TextView) findViewById(R.id.nums);
        mCoinName = (TextView) findViewById(R.id.coin_name);
        mVotes = (TextView) findViewById(R.id.votes);
        mRoomName = (TextView) findViewById(R.id.room_name);
        mRoomNum = (TextView) findViewById(R.id.room_num);
        mBtnAttention = findViewById(R.id.btn_attention);
        mUserView = (RecyclerView) findViewById(R.id.user_list);
        mUserView.setHasFixedSize(true);
        mUserView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mUserAdapter = new LiveUserAdapter(mContext);
        mUserAdapter.setOnItemClickListener(new OnItemClickListener<UserBean>() {
            @Override
            public void onItemClick(UserBean item, int position) {
                openUserInfoDialog(item.getId());
            }
        });
        mUserView.setAdapter(mUserAdapter);
        mLiveAnimPresenter = new LiveAnimPresenter(mContext);
        mLiveAnimPresenter.setEnterRoomView((TextView) findViewById(R.id.enter_room_anim));
        mLiveAnimPresenter.setAnimContainer(mWrap);
        mLiveAnimPresenter.setGiftAnimView((FrameAnimImageView) findViewById(R.id.gift_frame_anim));
        mLiveAnimPresenter.setEnterRoomAnimView(
                (FrameAnimImageView) findViewById(R.id.enter_room_frame_anim_1),
                (GifImageView) findViewById(R.id.enter_room_frame_anim_2));
        mLiveAnimPresenter.setEnterRoomWords((TextView) findViewById(R.id.enter_room_frame_words));
        mBottomFragment = getBottomFragment();
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.repalced_bottom, mBottomFragment).commit();
        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                handleMsg(msg);
            }
        };
        mIM = new JIM();
    }


    /**
     * 添加布局变化的监听器,用来监听键盘弹出和收回
     */
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            //获取当前界面可视部分
            mDecorView.getWindowVisibleDisplayFrame(mRect);
            int visibleHeight = mRect.height();
            if (mVisibleHeight == visibleHeight) {
                return;
            }
            mVisibleHeight = visibleHeight;

            L.e("onGlobalLayout-----mVisibleHeight----->" + mVisibleHeight);

            //情况① ：可视区高度发生变化时，要通知EMChatRoomFragment，使其调整自身大小
            if (mChatRoomFragmentShowed) {
                EventBus.getDefault().post(new VisibleHeightEvent(mVisibleHeight));
                return;
            }

            //情况② 可视区高度发生变化时，要使聊天栏上移
            //具体逻辑如下
            //先计算出 软键盘的高度 =屏幕的高度- 可视区的高度
            int softInputHeight = mScreenHeight - mVisibleHeight;
            if (softInputHeight > 0) {//软键盘弹出
                mSoftInputShowed = true;
                //游戏区域的高度
                int gameHeight = mGamePlaceholder.getHeight();
                //算出差值
                int dY = softInputHeight - gameHeight;
                if (dY > 0) {//软键盘比游戏区域高，产生了遮挡
                    mContent.setY(-dY);
                    if (mInputFragment != null) {
                        mInputFragment.translateY(-dY);
                    }
                }
            } else {//软键盘收回
                if (mSoftInputShowed) {
                    mSoftInputShowed = false;
                    mContent.setY(0);
                    if (mInputFragment != null && !mInputFragment.isHided()) {
                        mInputFragment.translateY(0);
                    } else {
                        removeLayoutListener();
                    }
                }
            }
        }
    };

    /**
     * 添加布局变化的监听器
     */
    public void addLayoutListener() {
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        L.e("onGlobalLayout-----添加onGlobalLayout--->");
    }

    /**
     * 移除布局变化的监听器
     */
    public void removeLayoutListener() {
        mRoot.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        L.e("onGlobalLayout-----移除onGlobalLayout--->");
    }

    public void setChatRoomFragmentShowed(boolean showed) {
        mChatRoomFragmentShowed = showed;
    }

    public void showBottom() {
        if (mBottom != null && mBottom.getVisibility() != View.VISIBLE) {
            mBottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUnReadCount();
    }

    public void liveClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat://聊天发言
                openChatWindow();
                break;
            case R.id.btn_msg://私信
                openChatListWindow();
                break;
            case R.id.votes_group://点击映票排行榜
                openOrderListWindow();
                break;
            case R.id.anchor_avatar_group://点击主播头像
                openUserInfoDialog(mLiveUid);
                break;
        }
    }


    /**
     * 打开聊天输入框
     */
    private void openChatWindow() {
        addLayoutListener();
        if (mBottom.getVisibility() == View.VISIBLE) {
            mBottom.setVisibility(View.INVISIBLE);
        }
        mInputFragment = new LiveInputFragment();
        int y = mScreenHeight - mGamePlaceholder.getHeight() - DpUtil.dp2px(40);
        Bundle bundle = new Bundle();
        bundle.putInt("y", y);
        bundle.putString("barrage_fee", mBarrageFee);
        mInputFragment.setArguments(bundle);
        mInputFragment.show(mFragmentManager, "LiveInputFragment");
    }

    /**
     * 打开私信聊天列表
     */
    public void openChatListWindow() {
        EMChatFragment chatFragment = new EMChatFragment();
        chatFragment.setIM(mIM);
        Bundle bundle = new Bundle();
        bundle.putInt("from", 1);
        chatFragment.setArguments(bundle);
        chatFragment.show(mFragmentManager, "EMChatFragment");
    }

    /**
     * 打开私信聊天窗口
     */
    public void openChatRoomWindow(UserBean bean, int isAttention) {
        EMChatRoomFragment chatRoomFragment = new EMChatRoomFragment();
        chatRoomFragment.setIM(mIM);
        Bundle bundle = new Bundle();
        bundle.putInt("from", 1);
        bundle.putParcelable("touser", bean);
        bundle.putInt("isAttention", isAttention);
        bundle.putBoolean("fromPop", true);
        chatRoomFragment.setArguments(bundle);
        chatRoomFragment.show(mFragmentManager, "EMChatRoomFragment");
    }

    /**
     * 打开设置窗口,踢人,禁言，设置管理员等
     */
    public void openSettingWindow(int action, UserBean bean) {
        LiveSettingFragment fragment = new LiveSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("action", action);
        bundle.putParcelable("user", bean);
        bundle.putString("liveuid", mLiveUid);
        bundle.putString("kick_time", mKickTime);
        bundle.putString("shut_time", mShutTime);
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "LiveSettingFragment");
    }

    /**
     * 打开管理员列表窗口
     */
    public void openAdminListWindow() {
        LiveAdminListFragment fragment = new LiveAdminListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("liveuid", mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "LiveAdminListFragment");
    }

    /**
     * 打开排行榜窗口
     */
    private void openOrderListWindow() {
        LiveOrderFragment fragment = new LiveOrderFragment();
        Bundle bundle = new Bundle();
        bundle.putString("touid", mLiveUid);
        bundle.putInt("type", 1);
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "LiveOrderListFragment");
    }


    /**
     * 打开个人信息弹窗
     */
    private void openUserInfoDialog(String touid) {
        LiveUserInfoFragment fragment = new LiveUserInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("touid", touid);
        bundle.putString("liveuid", mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "LiveUserInfoFragment");
    }

    public void showUnReadCount() {
        mUnReadCount = mIM.getAllUnReadCount();
        L.e("IM", "未读消息数量---->" + mUnReadCount);
        mBottomFragment.setUnReadCount(mUnReadCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ignoreUnRead(IgnoreUnReadEvent e) {
        mUnReadCount = 0;
        mBottomFragment.setUnReadCount(mUnReadCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(cn.jpush.im.android.api.model.Message message) {
        mUnReadCount++;
        mBottomFragment.setUnReadCount(mUnReadCount);
    }

    //接收已关注 未关注的切换事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAttentionEvent(AttentionEvent event) {
        if (event.getTouid().equals(mLiveUid)) {
            int isAttention = event.getIsAttention();
            if (isAttention == 1) {
                if (mBtnAttention.getVisibility() == View.VISIBLE) {
                    mBtnAttention.setVisibility(View.GONE);
                }
            } else {
                if (mBtnAttention.getVisibility() == View.GONE) {
                    mBtnAttention.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.SEND_BARRAGE);
        HttpUtil.cancel(HttpUtil.SET_AUCTION);
        HttpUtil.cancel(HttpUtil.AUCTION_END);
        HttpUtil.cancel(HttpUtil.SET_BID_PRICE);
        HttpUtil.cancel(HttpUtil.GET_COIN);
        HttpUtil.cancel(HttpUtil.GET_USER_LIST);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mLiveAnimPresenter = null;
        super.onDestroy();

    }

    /**
     * 连接socket
     */
    protected void connectSocket() {
        SocketUtil.getInstance().connect(mLiveUid, mStream).setMessageListener(this).setGameManager(mGameManager);
    }

    @Override
    public void onConnect(boolean successConn) {
        L.e("连接socket------->" + successConn);
        //获取僵尸粉
        if (successConn) {
            if (!mFirstConn) {
                mFirstConn = true;
                SocketUtil.getInstance().getFakeFans();
            }
        }
    }


    /**
     * 发送聊天消息
     */
    public void sendChatMessage(String content) {
        SocketUtil.getInstance().sendChatMsg(content);
    }

    /**
     * 发送弹幕消息
     */
    public void sendDanmuMessage(String content) {
        HttpUtil.sendBarrage(content, mLiveUid, mStream, mDanmuCallback);
    }

    private HttpCallback mDanmuCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                AppConfig.getInstance().getUserBean().setLevel(obj.getIntValue("level"));
                SocketUtil.getInstance().sendDanmu(obj.getString("barragetoken"));
                EventBus.getDefault().post(new LastCoinEvent(obj.getString("coin")));
            } else {
                ToastUtil.show(msg);
            }
        }
    };


    //收到聊天消息，分为系统消息和用户消息，要在聊天栏里显示
    public void onChat(LiveChatBean bean) {
        L.e("收到聊天消息--->" + bean.getContent());
        mChatListAdapter.insertItem(bean);
    }

    //收到飘心消息
    public void onLight() {
        if (this == null || isFinishing() || isDestroyed() || mLiveAnimPresenter == null) {
            return;
        }
        mLiveAnimPresenter.playFloatHeartAnim();
    }

    //收到用户进房间消息
    public void onEnterRoom(UserBean bean) {
        if (this == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (mUserAdapter != null && mUserAdapter.hasUser(bean.getId())) {
            return;
        }
        if (mLiveAnimPresenter != null) {
            mLiveAnimPresenter.playEnterRoomAnim(bean);
        }
        LiveChatBean chatBean = new LiveChatBean();
        chatBean.setType(LiveChatBean.ENTER_ROOM);
        chatBean.setId(bean.getId());
        chatBean.setUser_nicename(bean.getUser_nicename());
        chatBean.setLevel(bean.getLevel());
        mChatListAdapter.insertItem(chatBean);
        mNumsVal++;
        mNums.setText(String.valueOf(mNumsVal));
        if (!mMaxUserNum) {
            addUserToList(bean);
            if (mNumsVal > 20) {
                mMaxUserNum = true;
                mHandler.sendEmptyMessageDelayed(USER_LIST, mUserlistRefreshTime);
            }
        }
    }

    //收到用户离开房间消息
    public void onLeaveRoom(UserBean bean) {
        removeUserFromList(bean);
        mNumsVal--;
        mNums.setText(String.valueOf(mNumsVal));
    }

    //往用户列表中添加僵尸粉
    @Override
    public void addFakeFans(List<UserBean> list) {
        mNumsVal += list.size();
        mNums.setText(String.valueOf(mNumsVal));
        if (!mMaxUserNum) {
            mUserAdapter.insertList(list);
        }
    }

    @Override
    public void onLinkMicApply(String uid, String username) {

    }

    @Override
    public void onAgreeLinkMic() {

    }

    @Override
    public void onRefuseLinkMic() {

    }

    @Override
    public void onSendLinkMicUrl(String uid, String uname, String playUrl) {

    }

    @Override
    public void onLinkMicKick(String touid, String uname) {
        onLinkMicClose(touid, uname);
    }

    @Override
    public void onLinkMicClose(String uid, String uname) {
        if (!TextUtils.isEmpty(uid) && uid.equals(mLinkMicUid)) {
            ToastUtil.show(uname + WordUtil.getString(R.string.lianmai_exit_link_mic));
            isLinkMic = false;
            if (mLinkMicViewHolder != null) {
                mLinkMicViewHolder.stop();
            }
        }
    }

    @Override
    public void onAnchorNotResponse() {

    }

    @Override
    public void onAnchorBusy() {

    }

    @Override
    public void onLinkMicUserExit(String touid) {

    }

    //收到送礼物消息
    public void onSendGift(ReceiveGiftBean bean) {
        L.e("收到送礼物消息--->" + bean.getGiftname());
        LiveChatBean chatBean = new LiveChatBean();
        chatBean.setContent(getString(R.string.send_a) + bean.getGiftname());
        chatBean.setLevel(bean.getLevel());
        chatBean.setUser_nicename(bean.getUname());
        chatBean.setId(bean.getUid());
        chatBean.setType(LiveChatBean.GIFT);
        chatBean.setLiangName(bean.getLiangName());
        chatBean.setVipType(bean.getVipType());
        mChatListAdapter.insertItem(chatBean);
        mLiveAnimPresenter.playGiftAnim(bean);
        mVotestotal = bean.getVotestotal();
        mVotes.setText(mVotestotal);
    }

    //收到弹幕消息
    public void onSendDanMu(ReceiveDanMuBean bean) {
        mLiveAnimPresenter.addDanmu(bean);
        mVotestotal = bean.getVotestotal();
        mVotes.setText(mVotestotal);
    }

    /**
     * 主播关闭直播的时候
     */
    @Override
    public void onLiveEnd() {
        //子类各自实现自己的逻辑
    }

    /**
     * 超管关闭直播间
     */
    @Override
    public void onSuperCloseLive() {
        //子类各自实现自己的逻辑
    }

    /**
     * 观众被踢的时候
     */
    @Override
    public void onKick(String touid) {
        //子类各自实现自己的逻辑
    }

    /**
     * 观众被禁言的时候
     */
    @Override
    public void onShutUp(String touid, String content) {
        //子类各自实现自己的逻辑
    }

    /**
     * 主播切换计时收费的时候
     */
    @Override
    public void onChangeTimeCharge(int typeVal) {
        //子类各自实现自己的逻辑
    }

    @Override
    public void onDisConnect() {
        //子类各自实现自己的逻辑
    }

    /**
     * 计时收费的时候更新主播映票数
     */
    @Override
    public void updateVotes(String uid, int frist, int votes) {
        if (frist != 1 || !uid.equals(AppConfig.getInstance().getUid())) {
            mVotestotal = String.valueOf(Integer.parseInt(mVotestotal) + votes);
            mVotes.setText(mVotestotal);
        }
    }

    /**
     * 主播发起竞拍
     */
    @Override
    public void auctionStart(boolean isAnchor, String auctionId, String thumb, String title, String startPrice, int duration) {
        mAuctionWindow = new AuctionWindow(mContext, mWrap, isAnchor);
        mAuctionWindow.setData(auctionId, thumb, title, startPrice, duration)
                .show();
    }

    /**
     * 竞拍加价
     *
     * @param uhead     出价最多的人的头像
     * @param uname     出价最多的人的昵称
     * @param mostPrice 最高价格
     */
    @Override
    public void auctionAddMoney(String uhead, String uname, String mostPrice) {
        if (mAuctionWindow != null) {
            mAuctionWindow.showMostUser(uhead, uname, mostPrice);
        }
    }

    /**
     * 流拍
     */
    @Override
    public void auctionFailure() {
        if (mAuctionWindow != null) {
            mAuctionWindow.auctionFailure();
        }
    }

    /**
     * 竞拍成功
     *
     * @param uhead     出价最多的人的头像
     * @param uname     出价最多的人的昵称
     * @param mostPrice 最高价格
     */
    @Override
    public void auctionSuccess(String bidUid, String uhead, String uname, String mostPrice) {
        if (mAuctionWindow != null) {
            mAuctionWindow.auctionSuccess(bidUid, uhead, uname, mostPrice);
        }
    }

    /**
     * 切断socket
     */
    protected void closeSocket() {
        SocketUtil.getInstance().close();
    }

    /**
     * 把新进来的观众添加到观众列表中
     */
    private void addUserToList(UserBean bean) {
        mUserAdapter.insertItem(bean);
    }


    /**
     * 把离开房间的观众从观众列表中删除
     */
    private void removeUserFromList(UserBean bean) {
        mUserAdapter.removeItem(bean.getId());
    }


    /**
     * 当前直播间用户数大于20个后用这个刷新用户列表
     */
    protected void getUserList() {
        HttpUtil.getUserList(mLiveUid, mStream, mUserListCallback);
        mHandler.sendEmptyMessageDelayed(USER_LIST, mUserlistRefreshTime);
    }

    private HttpCallback mUserListCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                mUserList = JSON.parseArray(obj.getString("userlist"), UserBean.class);
                if (mUserAdapter != null) {
                    mUserAdapter.refreshList(mUserList);
                }
                mNumsVal = obj.getIntValue("nums");
                mNums.setText(String.valueOf(mNumsVal));
                mVotestotal = obj.getString("votestotal");
                mVotes.setText(mVotestotal);
            }
        }
    };

    /**
     * 房间关闭的时候执行一些关闭操作
     */
    protected void onClose() {
        mEnd = true;
        EventBus.getDefault().post(new LiveRoomCloseEvent());
        mGameManager.closeGame();//关闭游戏
        mLiveAnimPresenter.clearAnimQueue();//清空动画队列
        mRoot.setScrollView(null);
        //切断socket
        closeSocket();
        releaseLinkMic();
    }

    public abstract void closeRoom();

    //网络差，关闭直播间
    public abstract void onNetWorkErrorCloseRoom();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkEvent(ConnEvent e) {
        if (e.getCode() == ConnEvent.CONN_ERROR) {
            if (!mNetWorkBroken) {
                mNetWorkBroken = true;
                NetWorkErrorFragment fragment = new NetWorkErrorFragment();
                fragment.show(mFragmentManager, "NetWorkErrorFragment");
            }
        } else if (e.getCode() == ConnEvent.CONN_OK) {
            mNetWorkBroken = false;
        }
    }


    /**
     * 释放连麦相关窗口
     */
    public void releaseLinkMic() {
        if (mLinkMicViewHolder != null) {
            mLinkMicViewHolder.stop();
        }
    }


    /**
     * 连麦推流成功
     */
    @Override
    public void onPushSuccess() {
        //子类各自实现自己的逻辑
    }

    /**
     * 连麦推流失败
     */
    @Override
    public void onPushFailed() {
        //子类各自实现自己的逻辑
    }

    /**
     * 关闭连麦推流
     */
    @Override
    public void onPlayClose() {
        //子类各自实现自己的逻辑
    }

    /**
     * 连麦播放成功
     */
    @Override
    public void onPlaySuccess() {
        //子类各自实现自己的逻辑
    }

    /**
     * 连麦播放失败
     */
    @Override
    public void onPlayFailed() {
        //子类各自实现自己的逻辑
    }
}
