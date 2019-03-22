package com.caihongzhibo.phonelive.socket;

import android.os.Message;

import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.game.GameManager;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.RandomUtil;
import com.caihongzhibo.phonelive.utils.WordUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by cxf on 2017/8/18.
 */

public class SocketUtil {
    private final String TAG = "socekt";
    private static SocketUtil sInstance;
    private SocketHandler mSocketHandler;
    private Socket mSocket;
    private String mLiveuid;
    private String mStream;
    private final String CONN = "conn";//连接
    private final String SEND = "broadcast";//发送
    private final String BROADCAST = "broadcastingListen";//socket广播
    public static final int WHAT_CONN = 0;
    public static final int WHAT_DISCONN = 2;
    public static final int WHAT_BROADCAST = 1;

    public static final String STOP_PLAY = "stopplay";//超管关闭直播间
    public static final String STOP_LIVE = "stopLive";//超管关闭直播间
    public static final String SEND_MSG = "SendMsg";//发送文字消息，点亮，用户进房间  PS:这种混乱的设计是因为服务器端逻辑就是这样设计的,客户端无法自行修改
    public static final String LIGHT = "light";//飘心
    public static final String SEND_GIFT = "SendGift";//送礼物
    public static final String SEND_BARRAGE = "SendBarrage";//发弹幕
    public static final String LEAVE_ROOM = "disconnect";//用户离开房间
    public static final String LIVE_END = "StartEndLive";//主播关闭直播
    public static final String SYSTEM = "SystemNot";//系统消息
    public static final String KICK = "KickUser";//踢人
    public static final String SHUT_UP = "ShutUpUser";//禁言
    public static final String CHANGE_LIVE = "changeLive";//切换计时收费类型
    public static final String UPDATE_VOTES = "updateVotes";//计时收费或门票收费的时候更新主播的映票数
    public static final String AUCTION = "auction";//竞拍
    public static final String FAKE_FANS = "requestFans";//僵尸粉
    public static final String LINK_MIC = "ConnectVideo";//连麦


    private SocketUtil() {
        try {
            IO.Options option = new IO.Options();
            option.forceNew = true;
            option.reconnection = true;
            option.reconnectionDelay = 2000;
            mSocket = IO.socket(AppConfig.getInstance().getSocketServer(), option);
            mSocketHandler = new SocketHandler();
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "socket异常--->" + e.getMessage());
        }
    }

    public static SocketUtil getInstance() {
        if (sInstance == null) {
            synchronized (SocketUtil.class) {
                if (sInstance == null) {
                    sInstance = new SocketUtil();
                }
            }
        }
        return sInstance;
    }

    public void sendSocketMessage(JSONObject json) {
        if (mSocket != null) {
            mSocket.emit(SEND, json);
        }
    }


    private void sendData() {
        JSONObject data = new JSONObject();
        try {
            data.put("uid", AppConfig.getInstance().getUid());
            data.put("token", AppConfig.getInstance().getToken());
            data.put("liveuid", mLiveuid);
            data.put("roomnum", mLiveuid);
            data.put("stream", mStream);
            mSocket.emit("conn", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public SocketUtil connect(String liveuid, String stream) {
        if (mSocket != null) {
            mSocket.on(Socket.EVENT_CONNECT, mConnectListener);//连接成功
            mSocket.on(Socket.EVENT_DISCONNECT, mDisConnectListener);//断开连接
            mSocket.on(Socket.EVENT_CONNECT_ERROR, mErrorListener);//连接错误
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, mTimeOutListener);//连接超时
            mSocket.on(Socket.EVENT_RECONNECT, mReConnectListener);//重连
            mSocket.on(CONN, onConn);//连接socket消息
            mSocket.on(BROADCAST, onBroadcast);//接收服务器广播的具体业务逻辑相关的消息
            mSocket.connect();
            mLiveuid = liveuid;
            mStream = stream;
            sendData();
        }
        return this;
    }

    private Emitter.Listener mConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnect-->" + args);
        }
    };

    private Emitter.Listener mReConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--reConnect-->" + args);
            sendData();
        }
    };

    private Emitter.Listener mDisConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onDisconnect-->" + args);
            mSocketHandler.sendEmptyMessage(WHAT_DISCONN);
        }
    };
    private Emitter.Listener mErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnectError-->" + args);
        }
    };

    private Emitter.Listener mTimeOutListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            L.e(TAG, "--onConnectError-->" + args);
        }
    };

    private Emitter.Listener onBroadcast = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONArray array = (JSONArray) args[0];
                for (int i = 0; i < array.length(); i++) {
                    Message msg = Message.obtain();
                    msg.what = WHAT_BROADCAST;
                    msg.obj = array.getString(i);
                    mSocketHandler.sendMessage(msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private Emitter.Listener onConn = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String s = ((JSONArray) args[0]).getString(0);
                L.e(TAG, "--onConn-->" + s);
                Message msg = Message.obtain();
                msg.what = WHAT_CONN;
                msg.obj = s.equals("ok");
                mSocketHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    public SocketUtil setMessageListener(SocketMsgListener listener) {
        if (null!=mSocketHandler&&null!=listener){
            mSocketHandler.setSocketMsgListener(listener);
        }

        return this;
    }

    public SocketUtil setGameManager(GameManager gameManager) {
        if (null!=mSocketHandler&&null!=gameManager){
            mSocketHandler.setGameManager(gameManager);
        }
        return this;
    }

    public void close() {
        if (mSocketHandler != null) {
            mSocketHandler.setSocketMsgListener(null);
            mSocketHandler.removeCallbacksAndMessages(null);
        }
        if (mSocket != null) {
            mSocket.off();
            mSocket.disconnect();
        }
    }

    /**
     * 发送普通发言消息
     */
    public void sendChatMsg(String content) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SEND_MSG)
                        .param("action", 0)
                        .param("msgtype", 2)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("liangname", u.getLiang().getName())
                        .param("vip_type", u.getVip().getType())
                        .param("ct", content)
                        .create()
        );
    }

    /**
     * 发送点亮消息
     */
    public void sendLightMsg() {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SEND_MSG)
                        .param("action", 0)
                        .param("msgtype", 2)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("heart", RandomUtil.getRandom(1, 5))
                        .param("liangname", u.getLiang().getName())
                        .param("vip_type", u.getVip().getType())
                        .param("ct", WordUtil.getString(R.string.I_am_lighted))
                        .create()
        );
    }


    /**
     * 发送飘心消息
     */
    public void sendFloatHeart() {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LIGHT)
                        .param("action", 2)
                        .param("msgtype", 0)
                        .param("ct", "")
                        .create()
        );
    }

    /**
     * 发送礼物消息
     *
     * @param evensend
     * @param giftToken
     */
    public void sendGift(String evensend, String giftToken) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SEND_GIFT)
                        .param("action", 0)
                        .param("msgtype", 1)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("uhead", u.getAvatar())
                        .param("evensend", evensend)
                        .param("liangname", u.getLiang().getName())
                        .param("vip_type", u.getVip().getType())
                        .param("ct", giftToken)
                        .create()
        );
    }

    /**
     * 发送弹幕消息
     */
    public void sendDanmu(String barragetoken) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SEND_BARRAGE)
                        .param("action", 7)
                        .param("msgtype", 1)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("uhead", u.getAvatar())
                        .param("ct", barragetoken)
                        .create()
        );
    }

    /**
     * 主播或管理员 踢人
     */
    public void kickUser(String touid, String toname) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", KICK)
                        .param("action", 2)
                        .param("msgtype", 4)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("touid", touid)
                        .param("toname", toname)
                        .param("ct", toname + WordUtil.getString(R.string.be_kicked))
                        .create()
        );
    }

    /**
     * 主播或管理员 禁言
     */
    public void shutUpUser(String touid, String toname, String shut_time) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SHUT_UP)
                        .param("action", 1)
                        .param("msgtype", 4)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("touid", touid)
                        .param("toname", toname)
                        .param("ct", toname + WordUtil.getString(R.string.be_shut) + shut_time)
                        .create()
        );
    }

    /**
     * 发送系统消息
     */
    public void sendSystemMessage(String touid, String toname, String content) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SYSTEM)
                        .param("action", 13)
                        .param("msgtype", 4)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("touid", touid)
                        .param("toname", toname)
                        .param("ct", content)
                        .create()
        );
    }

    /**
     * 发送系统消息
     */
    public void sendSystemMessage(String content) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", SYSTEM)
                        .param("action", 13)
                        .param("msgtype", 4)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("ct", content)
                        .create()
        );
    }

    /**
     * 切换计时收费
     */
    public void changeTimeCharge(String typeVal) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", CHANGE_LIVE)
                        .param("action", 1)
                        .param("msgtype", 27)
                        .param("type_val", typeVal)
                        .param("ct", "")
                        .create()
        );
    }

    /**
     * 计时收费或门票收费的时候更新主播的映票数
     *
     * @param isfirst 是否是在当前直播间第一次发这个socket，
     *                如果是第一发socket,则不更新映票了，因为在直播间外面已经扣费了，进来之后的映票数是真实的
     * @param typeVal 每次扣费的值，也就是给主播映票数加的值
     */
    public void updateVotes(String isfirst, int typeVal) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", UPDATE_VOTES)
                        .param("action", 1)
                        .param("msgtype", 26)
                        .param("votes", typeVal)
                        .param("uid", AppConfig.getInstance().getUid())
                        .param("isfirst", isfirst)
                        .param("ct", "")
                        .create()
        );
    }

    /**
     * 发起竞拍
     */
    public void auctionStart(String auctionid) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", AUCTION)
                        .param("action", 1)
                        .param("msgtype", 55)
                        .param("auctionid", auctionid)
                        .create()
        );
    }

    /**
     * 竞拍加价
     */
    public void auctionAddMoney(String money) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", AUCTION)
                        .param("action", 2)
                        .param("msgtype", 55)
                        .param("level", u.getLevel())
                        .param("uname", u.getUser_nicename())
                        .param("uid", u.getId())
                        .param("uhead", u.getAvatar())
                        .param("money", money)
                        .param("ct", "")
                        .create()
        );
    }


    /**
     * 竞拍结束
     */
    public void auctionEnd(int action, String bidUid, String toname, String touhead, String money) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", AUCTION)
                        .param("action", action)
                        .param("msgtype", 55)
                        .param("toname", toname)
                        .param("touhead", touhead)
                        .param("bid_uid", bidUid)
                        .param("money", money)
                        .param("ct", "")
                        .create()
        );
    }

    /**
     * 获取僵尸粉
     */
    public void getFakeFans() {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", FAKE_FANS)
                        .param("action", "")
                        .param("msgtype", "")
                        .create()
        );
    }

    /**
     * 超管关闭直播间
     */
    public void stopLive() {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", STOP_LIVE)
                        .param("action", 19)
                        .param("msgtype", 1)
                        .param("ct", "")
                        .create()
        );
    }

    /**
     * 观众发送连麦请求
     */
    public void applyLinkMic() {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 1)
                        .param("msgtype", 10)
                        .param("uid", u.getId())
                        .param("ct", "")
                        .param("uname", u.getUser_nicename())
                        .param("level", u.getLevel())
                        .create()
        );
    }

    /**
     * 主播同意观众连麦请求
     */
    public void agreeLinkMic(String touid) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 2)
                        .param("msgtype", 10)
                        .param("uid", u.getId())
                        .param("ct", "")
                        .param("uname", u.getUser_nicename())
                        .param("level", u.getLevel())
                        .param("touid", touid)
                        .create()
        );
    }


    /**
     * 主播拒绝观众连麦请求
     */
    public void refuseLinkMic(String touid) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 3)
                        .param("msgtype", 10)
                        .param("uid", u.getId())
                        .param("ct", "")
                        .param("uname", u.getUser_nicename())
                        .param("level", u.getLevel())
                        .param("touid", touid)
                        .create()
        );
    }

    /**
     * 主播同意连麦后，观众把自己的流地址发送给主播
     */
    public void sendLinkMicUrl(String playUrl) {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 4)
                        .param("msgtype", 10)
                        .param("uid", u.getId())
                        .param("ct", "")
                        .param("uname", u.getUser_nicename())
                        .param("level", u.getLevel())
                        .param("playurl", playUrl)
                        .create()
        );
    }

    /**
     * 观众断开连麦
     */
    public void exitLinkMic() {
        if (mSocket == null) {
            return;
        }
        UserBean u = AppConfig.getInstance().getUserBean();
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 5)
                        .param("msgtype", 10)
                        .param("uid", u.getId())
                        .param("ct", "")
                        .param("uname", u.getUser_nicename())
                        .create()
        );
    }

    /**
     * 主播断开某人的连麦
     */
    public void kickLinkMic(String touid, String uname) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 6)
                        .param("msgtype", 10)
                        .param("touid", touid)
                        .param("ct", "")
                        .param("uname", uname)
                        .create()
        );
    }


    /**
     * 主播正在忙
     */
    public void anchorBusy(String touid) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 7)
                        .param("msgtype", 10)
                        .param("touid", touid)
                        .create()
        );
    }

    /**
     * 主播未响应
     */
    public void anchorNotResponse(String touid) {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 8)
                        .param("msgtype", 10)
                        .param("touid", touid)
                        .create()
        );
    }

    /**
     * 观众退出直播间
     */
    public void audienceExit() {
        if (mSocket == null) {
            return;
        }
        mSocket.emit(SEND,
                new SendSocketBean()
                        .param("_method_", LINK_MIC)
                        .param("action", 9)
                        .param("msgtype", 10)
                        .param("touid", AppConfig.getInstance().getUid())
                        .create()
        );
    }


}
