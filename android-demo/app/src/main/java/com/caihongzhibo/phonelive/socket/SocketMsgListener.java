package com.caihongzhibo.phonelive.socket;

import com.caihongzhibo.phonelive.bean.LiveChatBean;
import com.caihongzhibo.phonelive.bean.ReceiveDanMuBean;
import com.caihongzhibo.phonelive.bean.ReceiveGiftBean;
import com.caihongzhibo.phonelive.bean.UserBean;

import java.util.List;

/**
 * Created by cxf on 2017/8/22.
 */

public interface SocketMsgListener {

    //连接成功socket后调用
    void onConnect(boolean successConn);

    //socket断开
    void onDisConnect();

    //收到聊天消息，分为系统消息和用户消息，要在聊天栏里显示
    void onChat(LiveChatBean bean);

    //收到飘心消息
    void onLight();

    //收到用户进房间消息
    void onEnterRoom(UserBean bean);

    //收到用户离开房间消息
    void onLeaveRoom(UserBean bean);

    //收到送礼物消息
    void onSendGift(ReceiveGiftBean bean);

    //收到弹幕消息
    void onSendDanMu(ReceiveDanMuBean bean);

    //观众收到直播结束消息
    void onLiveEnd();

    //超管关闭直播间
    void onSuperCloseLive();

    //踢人
    void onKick(String touid);

    //禁言
    void onShutUp(String touid, String content);

    //主播切换计时收费或更改计时收费价格的时候执行
    void onChangeTimeCharge(int typeVal);

    //主播切换计时收费或更改计时收费价格的时候，更新主播映票数
    void updateVotes(String uid, int frist, int votes);

    /**
     * 发起竞拍
     *
     * @param isAnchor   自己是不是主播
     * @param auctionId  竞拍的id
     * @param startPrice 起拍价格
     */
    void auctionStart(boolean isAnchor, String auctionId, String thumb,String title,String startPrice, int duration);

    /**
     * 竞拍加价
     *
     * @param uhead     出价最多的人的头像
     * @param uname     出价最多的人的昵称
     * @param mostPrice 最高价格
     */
    void auctionAddMoney(String uhead, String uname, String mostPrice);

    /**
     * 流拍
     */
    void auctionFailure();

    /**
     * 竞拍成功
     *
     * @param uhead     出价最多的人的头像
     * @param uname     出价最多的人的昵称
     * @param mostPrice 最高价格
     */
    void auctionSuccess(String bidUid,String uhead, String uname, String mostPrice);

    //添加僵尸粉
    void addFakeFans(List<UserBean> list);

    //主播收到观众的连麦申请
    void onLinkMicApply(String uid, String username);

    //观众收到主播同意连麦的socket
    void onAgreeLinkMic();

    //观众收到主播拒绝连麦的socket
    void onRefuseLinkMic();

    //主播收到观众发过来的流地址
    void onSendLinkMicUrl(String uid, String uname, String playUrl);

    //主播关闭某人的连麦
    void onLinkMicKick(String touid, String uname);

    //某人主动断开连麦
    void onLinkMicClose(String uid, String uname);

    //主播连麦无响应
    void onAnchorNotResponse();

    //主播正在忙
    void onAnchorBusy();

    //连麦用户退出直播间
    void onLinkMicUserExit(String touid);
}
