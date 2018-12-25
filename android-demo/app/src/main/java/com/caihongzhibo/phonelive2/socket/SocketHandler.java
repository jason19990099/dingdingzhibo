package com.caihongzhibo.phonelive2.socket;

import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.LiveChatBean;
import com.caihongzhibo.phonelive2.bean.ReceiveDanMuBean;
import com.caihongzhibo.phonelive2.bean.ReceiveGiftBean;
import com.caihongzhibo.phonelive2.bean.UserBean;
import com.caihongzhibo.phonelive2.game.GameConst;
import com.caihongzhibo.phonelive2.game.GameManager;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import java.util.List;

/**
 * Created by cxf on 2017/8/22.
 */

public class SocketHandler extends Handler {

    private SocketMsgListener mListener;
    private GameManager mGameManager;

    @Override
    public void handleMessage(Message msg) {
        if (mListener == null) {
            return;
        }
        switch (msg.what) {
            case SocketUtil.WHAT_CONN:
                mListener.onConnect((Boolean) msg.obj);
                break;
            case SocketUtil.WHAT_BROADCAST:
                processBroadcast((String) msg.obj);
                break;
            case SocketUtil.WHAT_DISCONN:
                mListener.onDisConnect();
                break;
        }

    }


    private void processBroadcast(String socketMsg) {
        L.e("收到socket--->" + socketMsg);
        if (SocketUtil.STOP_PLAY.equals(socketMsg)) {
            mListener.onSuperCloseLive();//超管关闭房间
            return;
        }
        ReceiveSocketBean received = JSON.parseObject(socketMsg, ReceiveSocketBean.class);
        JSONObject map = received.getMsg().getJSONObject(0);
        switch (map.getString("_method_")) {
            case SocketUtil.SYSTEM://系统消息
                systemChatMessage(map.getString("ct"));
                break;
            case SocketUtil.KICK://踢人
                systemChatMessage(map.getString("ct"));
                mListener.onKick(map.getString("touid"));
                break;
            case SocketUtil.SHUT_UP://禁言
                String ct = map.getString("ct");
                systemChatMessage(ct);
                mListener.onShutUp(map.getString("touid"), ct);
                break;
            case SocketUtil.SEND_MSG://文字消息，点亮，用户进房间
                String msgtype = map.getString("msgtype");
                if ("2".equals(msgtype)) {//发言，点亮
                    if ("409002".equals(received.getRetcode())) {
                        ToastUtil.show(WordUtil.getString(R.string.you_are_shut));
                        return;
                    }
                    LiveChatBean chatBean = new LiveChatBean();
                    chatBean.setId(map.getString("uid"));
                    chatBean.setUser_nicename(map.getString("uname"));
                    chatBean.setLevel(map.getIntValue("level"));
                    chatBean.setContent(map.getString("ct"));
                    chatBean.setHeart(map.getIntValue("heart"));
                    chatBean.setLiangName(map.getString("liangname"));
                    chatBean.setVipType(map.getIntValue("vip_type"));
                    mListener.onChat(chatBean);
                } else if ("0".equals(msgtype)) {//用户进入房间
                    JSONObject obj = JSON.parseObject(map.getString("ct"));
                    UserBean u = JSON.toJavaObject(obj, UserBean.class);
                    UserBean.Vip vip = new UserBean.Vip();
                    vip.setType(obj.getIntValue("vip_type"));
                    u.setVip(vip);
                    UserBean.Car car = new UserBean.Car();
                    car.setId(obj.getIntValue("car_id"));
                    car.setSwf(obj.getString("car_swf"));
                    car.setSwftime(obj.getFloatValue("car_swftime"));
                    car.setWords(obj.getString("car_words"));
                    u.setCar(car);
                    mListener.onEnterRoom(u);
                }
                break;
            case SocketUtil.LIGHT://飘心
                mListener.onLight();
                break;
            case SocketUtil.SEND_GIFT://送礼物
                ReceiveGiftBean receiveGiftBean = JSON.parseObject(map.getString("ct"), ReceiveGiftBean.class);
                receiveGiftBean.setUhead(map.getString("uhead"));
                receiveGiftBean.setUname(map.getString("uname"));
                receiveGiftBean.setEvensend(map.getString("evensend"));
                receiveGiftBean.setLiangName(map.getString("liangname"));
                receiveGiftBean.setVipType(map.getIntValue("vip_type"));
                mListener.onSendGift(receiveGiftBean);
                break;
            case SocketUtil.SEND_BARRAGE://发弹幕
                ReceiveDanMuBean receiveDanMuBean = JSON.parseObject(map.getString("ct"), ReceiveDanMuBean.class);
                receiveDanMuBean.setUhead(map.getString("uhead"));
                receiveDanMuBean.setUname(map.getString("uname"));
                mListener.onSendDanMu(receiveDanMuBean);
                break;
            case SocketUtil.LEAVE_ROOM://离开房间
                UserBean u = JSON.parseObject(map.getString("ct"), UserBean.class);
                mListener.onLeaveRoom(u);
                break;
            case SocketUtil.LIVE_END://主播关闭直播
                mListener.onLiveEnd();
                break;
            case SocketUtil.CHANGE_LIVE://主播切换计时收费类型
                mListener.onChangeTimeCharge(map.getIntValue("type_val"));
                break;
            case SocketUtil.UPDATE_VOTES:
                mListener.updateVotes(map.getString("uid"), map.getIntValue("isfirst"), map.getIntValue("votes"));
                break;
            case SocketUtil.FAKE_FANS:
                JSONObject obj = map.getJSONObject("ct");
                String s = obj.getJSONObject("data").getJSONArray("info").getJSONObject(0).getString("list");
                L.e("僵尸粉--->" + s);
                List<UserBean> list = JSON.parseArray(s, UserBean.class);
                mListener.addFakeFans(list);
                break;
            case GameConst.SOKCET_GAME_JIN_HUA://炸金花游戏
            case GameConst.SOKCET_GAME_HAI_DAO://海盗船长
            case GameConst.SOKCET_GAME_NIU_ZAI://开心牛仔
            case GameConst.SOKCET_GAME_LUCK_PAN://幸运转盘
            case GameConst.SOKCET_GAME_ER_BA_BEI://二八贝
                mGameManager.processGame(map);
                break;
            case SocketUtil.LINK_MIC://连麦
                processLinkMic(map);
                break;

        }
    }

    /**
     * 连麦逻辑处理
     */
    private void processLinkMic(JSONObject map) {
        int action = map.getIntValue("action");
        switch (action) {
            case 1:
                mListener.onLinkMicApply(map.getString("uid"), map.getString("uname"));
                break;
            case 2:
                if (map.getString("touid").equals(AppConfig.getInstance().getUid())) {
                    mListener.onAgreeLinkMic();
                }
                break;
            case 3:
                if (map.getString("touid").equals(AppConfig.getInstance().getUid())) {
                    mListener.onRefuseLinkMic();
                }
                break;
            case 4:
                mListener.onSendLinkMicUrl(map.getString("uid"), map.getString("uname"), map.getString("playurl"));
                break;
            case 5:
                mListener.onLinkMicClose(map.getString("uid"), map.getString("uname"));
                break;
            case 6:
                mListener.onLinkMicKick(map.getString("touid"), map.getString("uname"));
                break;
            case 7:
                if (map.getString("touid").equals(AppConfig.getInstance().getUid())) {
                    mListener.onAnchorBusy();
                }
                break;
            case 8:
                if (map.getString("touid").equals(AppConfig.getInstance().getUid())) {
                    mListener.onAnchorNotResponse();
                }
                break;
            case 9:
                mListener.onLinkMicUserExit(map.getString("touid"));
                break;
        }
    }



    /**
     * 处理竞拍的逻辑
     */
    private void processAuction(JSONObject map) {
        switch (map.getIntValue("action")) {
            case 1://主播发起竞拍
                JSONObject auction = JSON.parseObject(map.getString("ct"));
                mListener.auctionStart(
                        AppConfig.getInstance().getUid().equals(auction.getString("uid")),
                        auction.getString("id"),
                        auction.getString("thumb"),
                        auction.getString("title"),
                        auction.getString("price_start"),
                        auction.getIntValue("long"));
                break;
            case 2://竞拍加价
                mListener.auctionAddMoney(map.getString("uhead"), map.getString("uname"), map.getString("money"));
                break;
            case 3://流拍
                mListener.auctionFailure();
                break;
            case 4://竞拍成功
                mListener.auctionSuccess(map.getString("bid_uid"), map.getString("touhead"), map.getString("toname"), map.getString("money"));
                break;
        }
    }

    /**
     * 接收到系统消息，显示在聊天栏中
     *
     * @param content
     * @return
     */
    private LiveChatBean systemChatMessage(String content) {
        LiveChatBean bean = new LiveChatBean();
        bean.setContent(content);
        bean.setType(LiveChatBean.SYSTEM);
        mListener.onChat(bean);
        return bean;
    }

    public void setSocketMsgListener(SocketMsgListener listener) {
        mListener = listener;
    }

    public void setGameManager(GameManager gameManager) {
        mGameManager = gameManager;
    }
}
