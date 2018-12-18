package com.caihongzhibo.phonelive.im;

import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.event.JIMLoginEvent;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.SharedPreferencesUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by cxf on 2017/8/10.
 * 极光IM注册、登陆等功能
 */

public class JIMUtil {

    private static final String TAG = "极光IM";
    private static final String PWD_SUFFIX = "PUSH";//注册极光IM的时候，密码是用户id+"PUSH"这个常量构成的
    //前缀，当uid不够长的时候无法注册
    public static final String PREFIX = "";
    private Map<String, Long> mMap;

    private static JIMUtil sInstance;

    private JIMUtil() {
        mMap = new HashMap<>();
    }

    public static JIMUtil getInstance() {
        if (sInstance == null) {
            synchronized (JIMUtil.class) {
                if (sInstance == null) {
                    sInstance = new JIMUtil();
                }
            }
        }
        return sInstance;
    }


    public void init() {
        JMessageClient.init(AppContext.sInstance);
    }

    /**
     * 登出极光IM
     */
    public void logoutEMClient() {
        JMessageClient.logout();
        SharedPreferencesUtil.getInstance().saveEMLoginStatus(false);
        AppConfig.getInstance().setIMLogined(false);
        L.e(TAG, "极光IM登出");
    }

    /**
     * 登录极光IM
     */
    public void loginEMClient(final String uid) {
        if (SharedPreferencesUtil.getInstance().readEMLoginStatus()) {
            L.e(TAG, "极光IM已经登录了");
            JMessageClient.registerEventReceiver(JIMUtil.this);
            AppConfig.getInstance().setIMLogined(true);
            return;
        }
        JMessageClient.login(uid, uid + PWD_SUFFIX, new BasicCallback() {

            @Override
            public void gotResult(int code, String msg) {
                //L.e(TAG, "登录极光回调---gotResult--->code: " + code + " msg: " + msg);
                if (code == 801003) {//用户不存在
                    L.e(TAG, "未注册，用户不存在");
                    registerAndLoginEMClient(uid);
                } else if (code == 0) {
                    L.e(TAG, "极光IM登录成功");
                    SharedPreferencesUtil.getInstance().saveEMLoginStatus(true);
                    AppConfig.getInstance().getIgnoreMessage();
                    EventBus.getDefault().post(new JIMLoginEvent());
                    JMessageClient.registerEventReceiver(JIMUtil.this);
                    AppConfig.getInstance().setIMLogined(true);
                }
            }
        });

    }

    //注册并登录极光IM
    private void registerAndLoginEMClient(final String uid) {
        JMessageClient.register(uid, uid + PWD_SUFFIX, new BasicCallback() {
            @Override
            public void gotResult(int code, String msg) {
                L.e(TAG, "注册极光回调---gotResult--->code: " + code + " msg: " + msg);
                if (code == 0) {
                    L.e(TAG, "极光IM注册成功");
                    loginEMClient(uid);
                }
            }
        });
    }

    /**
     * 接收消息 目前是在子线程接收的
     *
     * @param event
     */
    public void onEvent(MessageEvent event) {
        //收到消息
        boolean canShow = true;
        Message msg = event.getMessage();
        String from = msg.getFromUser().getUserName().substring(JIMUtil.PREFIX.length());
        //L.e(TAG, "onEvent--->来自：" + from + "---内容--> " + ((TextContent) msg.getContent()).getText());
        Object lastTime = mMap.get(from);
        if (lastTime != null) {
            if (System.currentTimeMillis() - (long) lastTime < 1500) {
                //同一个人，上条消息距离这条消息间隔不到1秒，则不显示这条消息
                canShow = false;
            } else {
                mMap.put(from, System.currentTimeMillis());
            }
        } else {
            //说明sMap内没有保存这个人的信息，则是首次收到这人的信息，可以显示
            mMap.put(from, System.currentTimeMillis());
        }
        if (canShow) {
            L.e(TAG, "显示消息--->");
            AppConfig.getInstance().setIgnoreMessage(false);
            EventBus.getDefault().post(msg);
        }
    }

//    /**
//     * 接收离线消息 目前是在子线程接收的
//     *
//     * @param event
//     */
//    public void onEvent(OfflineMessageEvent event) {
//        List<Message> list = event.getOfflineMessageList();
//        for (Message msg : list) {
//            if (!msg.getFromUser().getUserName().equals(AppConfig.getInstance().getUid())) {
//                L.e(TAG, "显示离线消息--->");
//                AppConfig.getInstance().setIgnoreMessage(false);
//                EventBus.getDefault().post(msg);
//            }
//        }
//    }

    /**
     * 忽略未读消息
     */
    public void ignoreUnReadMessage() {
        List<Conversation> list = JMessageClient.getConversationList();
        for (Conversation conversation : list) {
            conversation.resetUnreadCount();
        }
    }

}
