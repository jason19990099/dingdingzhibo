package com.caihongzhibo.phonelive.http;

import android.app.Dialog;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.MemoryCookieStore;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.request.PostRequest;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.ErrorActivity;
import com.caihongzhibo.phonelive.activity.LoginInvalidActivity;
import com.caihongzhibo.phonelive.bean.ConfigBean;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.event.AttentionEvent;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.SharedPreferencesUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;
import com.caihongzhibo.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;


/**
 * Created by cxf on 2017/8/4.
 */

public class HttpUtil {

    private static final String HTTP_URL = AppConfig.HOST + AppConfig.URI;
    private static OkHttpClient sOkHttpClient;

    public static void init() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //okGo默认的超时时间是60秒
        builder.connectTimeout(1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(1000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(1000, TimeUnit.MILLISECONDS);
        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));
        builder.retryOnConnectionFailure(true);

        //输出HTTP请求 响应信息
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("http");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BASIC);
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);

        sOkHttpClient = builder.build();

        OkGo.getInstance().init(AppContext.sInstance)
                .setOkHttpClient(sOkHttpClient)
                .setCacheMode(CacheMode.NO_CACHE)
                .setRetryCount(3);
    }

    public static void cancel(String tag) {
        OkGo.cancelTag(sOkHttpClient, tag);
    }

    //刚好80个接口。。。
    public static final String IF_TOKEN = "ifToken";
    public static final String GET_CONFIG = "getConfig";
    public static final String GET_VALIDATE_CODE = "getValidateCode";
    public static final String GET_VALIDATE_CODE2 = "getValidateCode2";
    public static final String FIND_PWD = "findPwd";
    public static final String REGISTER = "register";
    public static final String LOGIN = "login";
    public static final String LOGIN_BY_THIRD = "loginByThird";
    public static final String GET_QQLOGIN_OPENID = "getQQLoginOpenid";
    public static final String GET_HOT = "getHot";
    public static final String GET_NEW = "getNew";
    public static final String GET_NEAR = "getNear";
    public static final String GET_FOLLOW = "getFollow";
    public static final String SET_ATTENTION = "setAttention";
    public static final String SEARCH = "search";
    public static final String GET_USER_HOME = "getUserHome";
    public static final String GET_FOLLOWS_LIST = "getFollowsList";
    public static final String GET_FANS_LIST = "getFansList";
    public static final String GET_ALI_CDN_RECORD = "getAliCdnRecord";
    public static final String SET_BLACK = "setBlack";
    public static final String CHECK_BLACK = "checkBlack";
    public static final String GET_MULTI_INFO = "getMultiInfo";
    public static final String GET_PM_USER_INFO = "getPmUserInfo";
    public static final String GET_BASE_INFO = "getBaseInfo";
    public static final String GET_LIVERECORD = "getLiverecord";
    public static final String UPDATE_AVATAR = "updateAvatar";
    public static final String UPDATE_FIELDS = "updateFields";
    public static final String UPDATE_PASS = "updatePass";
    public static final String GET_GIFT_LIST = "getGiftList";
    public static final String SEND_GIFT = "sendGift";
    public static final String CHECK_LIVE = "checkLive";
    public static final String ENTER_ROOM = "enterRoom";
    public static final String SEND_BARRAGE = "sendBarrage";
    public static final String GET_POP = "getPop";
    public static final String GET_ADMIN_LIST = "getAdminList";
    public static final String SET_ADMIN = "setAdmin";
    public static final String KICKING = "kicking";
    public static final String SET_SHUT_UP = "setShutUp";
    public static final String SUPER_STOP_ROOM = "superStopRoom";
    public static final String SET_REPORT = "setReport";
    public static final String CREATE_ROOM = "createRoom";
    public static final String CHANGE_LIVE = "changeLive";
    public static final String STOP_ROOM = "stopRoom";
    public static final String STOP_LIVE_INFO = "stopLiveInfo";
    public static final String SEARCH_MUSIC = "searchMusic";
    public static final String GET_DOWN_MUSIC_URL = "getDownMusicUrl";
    public static final String CHANGE_LIVE_TYPE = "changeLiveType";
    public static final String ROOM_CHARGE = "roomCharge";
    public static final String TIME_CHARGE = "timeCharge";
    public static final String GET_BALANCE = "getBalance";
    public static final String GET_ALI_ORDER = "getAliOrder";
    public static final String GET_WX_ORDER = "getWxOrder";
    public static final String GET_PROFIT = "getProfit";
    public static final String GET_CASH = "getCash";
    public static final String GET_BONUS = "getBonus";
    public static final String SET_AUCTION = "setAuction";
    public static final String AUCTION_END = "auctionEnd";
    public static final String SET_BID_PRICE = "setBidPrice";
    public static final String GET_COIN = "getCoin";
    public static final String GET_USER_LIST = "getUserList";
    public static final String SET_DISTRIBUT = "setDistribut";
    public static final String GET_RECOMMEND = "getRecommend";
    public static final String ATTENT_RECOMMEND = "attentRecommend";
    public static final String PROFIT_LIST = "profitList";
    public static final String CONSUME_LIST = "consumeList";
    public static final String GAME_JINHUA_CREATE = "gameJinhuaCreate";
    public static final String GAME_JINHUA_BET = "gameJinhuaBet";
    public static final String GAME_SETTLE = "gameSettle";
    public static final String GAME_HAIDAO_CREATE = "gameHaidaoCreate";
    public static final String GAME_HAIDAO_BET = "gameHaidaoBet";
    public static final String GAME_NIUZAI_CREATE = "gameNiuzaiCreate";
    public static final String GAME_NIUZAI_BET = "gameNiuzaiBet";
    public static final String GAME_NIU_GET_BANKER = "gameNiuGetBanker";
    public static final String GAME_NIU_SET_BANKER = "gameNiuSetBanker";
    public static final String GAME_NIU_QUIT_BANKER = "gameNiuQuitBanker";
    public static final String GAME_NIU_BANKER_WATER = "gameNiuBankerWater";
    public static final String GAME_NIU_RECORD = "gameNiuRecord";
    public static final String GAME_EBB_CREATE = "gameEbbCreate";
    public static final String GAME_EBB_BET = "gameEbbBet";
    public static final String GAME_LUCK_PAN_CREATE = "gameLuckPanCreate";
    public static final String GAME_LUCK_PAN_BET = "gameLuckPanBet";
    public static final String GET_LINKMICSTREAM = "getLinkMicStream";
    public static final String LINK_MIC_SHOW_VIDEO = "linkMicShowVideo";

    /**
     * 验证token是否过期
     *
     * @param uid
     * @param token
     * @param callback
     */
    public static void ifToken(String uid, String token, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.iftoken")
                .params("uid", uid)
                .params("token", token)
                .tag(IF_TOKEN)
                .execute(callback);
    }

    /**
     * 获取config
     */
    public static void getConfig(final CommonCallback<ConfigBean> commonCallback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getConfig")
                .tag(GET_CONFIG)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            try {
                                ConfigBean bean = JSON.parseObject(info[0], ConfigBean.class);
                                AppConfig.getInstance().setConfig(bean);
                                SharedPreferencesUtil.getInstance().saveConfig(info[0]);
                                if (commonCallback != null) {
                                    commonCallback.callback(bean);
                                }
                            } catch (Exception e) {
                                Intent intent = new Intent(AppContext.sInstance, ErrorActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("title", "json解析错误");
                                String error = "info[0]:" + info[0] + "\n" + "Exception:" + e.getClass() + "---->" + e.getMessage();
                                intent.putExtra("error", error);
                                AppContext.sInstance.startActivity(intent);
                            }
                        }
                    }
                });
    }

    /**
     * 获取验证码接口 注册用
     */
    public static void getValidateCode(String mobile, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.getCode")
                .params("mobile", mobile)
                .tag(GET_VALIDATE_CODE)
                .execute(callback);
    }

    /**
     * 获取验证码接口 找回密码用
     */
    public static void getValidateCode2(String mobile, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.getForgetCode")
                .params("mobile", mobile)
                .tag(GET_VALIDATE_CODE2)
                .execute(callback);
    }

    /**
     * 找回密码接口
     */
    public static void findPwd(String user_login, String pass, String pass2, String code, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.userFindPass")
                .params("user_login", user_login)
                .params("user_pass", pass)
                .params("user_pass2", pass2)
                .params("code", code)
                .tag(FIND_PWD)
                .execute(callback);
    }


    /**
     * 手机注册接口
     */
    public static void register(String user_login, String pass, String pass2, String code, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.userReg")
                .params("user_login", user_login)
                .params("user_pass", pass)
                .params("user_pass2", pass2)
                .params("code", code)
                .tag(REGISTER)
                .execute(callback);
    }


    /**
     * 手机号 密码登录
     *
     * @param phoneNum
     * @param pwd
     */
    public static void login(String phoneNum, String pwd, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.userLogin")
                .params("user_login", phoneNum)
                .params("user_pass", pwd)
                .tag(LOGIN)
                .execute(callback);
    }

    /**
     * 第三方登录
     */
    public static void loginByThird(String openid, String nicename, String type, String avatar, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Login.userLoginByThird")
                .params("openid", openid)
                .params("nicename", nicename)
                .params("type", type)
                .params("avatar", avatar)
                .tag(LOGIN_BY_THIRD)
                .execute(callback);
    }

    /**
     * QQ登录的时候 获取openid
     */
    public static void getQQLoginOpenid(String access_token, StringCallback callback) {
        OkGo.<String>get("https://graph.qq.com/oauth2.0/me?access_token=" + access_token)
                .params("unionid", 1 + "")
                .tag(GET_QQLOGIN_OPENID)
                .execute(callback);
    }

    /**
     * 首页 热门标签
     */
    public static void getHot(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getHot")
                .tag(GET_HOT)
                .execute(callback);
    }

    /**
     * 首页 最新标签
     */
    public static void getNew(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getNew")
                .tag(GET_NEW)
                .execute(callback);
    }

    /**
     * 首页 附近标签
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public static void getNear(String lng, String lat, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getNearby")
                .params("lng", lng)
                .params("lat", lat)
                .tag(GET_NEAR)
                .execute(callback);
    }

    /**
     * 首页 关注标签
     */
    public static void getFollow(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getFollow")
                .params("uid", AppConfig.getInstance().getUid())
                .tag(GET_FOLLOW)
                .execute(callback);
    }


    /**
     * 关注别人 或 取消对别人的关注的接口
     */
    public static void setAttention(final String touid, final CommonCallback<Integer> callback) {
        if (touid.equals(AppConfig.getInstance().getUid())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_follow_self));
            return;
        }
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.setAttent")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .tag(SET_ATTENTION)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        int isAttention = JSON.parseObject(info[0]).getIntValue("isattent");//1是 关注  0是未关注
                        EventBus.getDefault().post(new AttentionEvent(touid, isAttention));
                        if (callback != null) {
                            callback.callback(isAttention);
                        }
                    }

                    @Override
                    public boolean showLoadingDialog() {
                        if (callback != null) {
                            return callback.showLoadingDialog();
                        }
                        return false;
                    }

                    @Override
                    public Dialog createLoadingDialog() {
                        if (callback != null) {
                            return callback.createLoadingDialog();
                        }
                        return null;
                    }
                });
    }

    /**
     * 搜索
     */
    public static void search(String key, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.search")
                .params("uid", AppConfig.getInstance().getUid())
                .params("key", key)
                .tag(SEARCH)
                .execute(callback);
    }

    /**
     * 他人个人主页信息
     */
    public static void getUserHome(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getUserHome")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(GET_USER_HOME)
                .execute(callback);
    }

    /**
     * 获取对方的关注列表
     *
     * @param touid    对方的uid
     * @param callback
     */
    public static void getFollowsList(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getFollowsList")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(GET_FOLLOWS_LIST)
                .execute(callback);
    }

    /**
     * 获取对方的粉丝列表
     *
     * @param touid    对方的uid
     * @param callback
     */
    public static void getFansList(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getFansList")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(GET_FANS_LIST)
                .execute(callback);
    }


    /**
     * 获取直播回放url
     *
     * @param recordId 视频的id
     * @param callback
     */
    public static void getAliCdnRecord(String recordId, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getAliCdnRecord")
                .params("id", recordId)
                .tag(GET_ALI_CDN_RECORD)
                .execute(callback);
    }

    /**
     * 拉黑对方， 解除拉黑
     *
     * @param touid    对方的uid
     * @param callback
     */
    public static void setBlack(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.setBlack")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .tag(SET_BLACK)
                .execute(callback);
    }

    /**
     * 判断自己有没有被对方拉黑，环信聊天的时候用到
     *
     * @param touid    对方的uid
     * @param callback
     */
    public static void checkBlack(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.checkBlack")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(CHECK_BLACK)
                .execute(callback);
    }

    /**
     * 获取环信聊天列表用户的信息 uids是多个用户的id,以逗号分隔
     *
     * @param uids
     * @param type     1是已关注  0是未关注
     * @param callback
     */
    public static void getMultiInfo(String uids, String type, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getMultiInfo")
                .params("uid", AppConfig.getInstance().getUid())
                .params("type", type)
                .params("uids", uids)
                .tag(GET_MULTI_INFO)
                .execute(callback);
    }

    /**
     * 用来确定 聊天页面 陌生人是关注的还是未关注的
     *
     * @param touid
     * @param callback
     */
    public static void getPmUserInfo(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getPmUserInfo")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(GET_PM_USER_INFO)
                .execute(callback);
    }

    /**
     * 个人页面获取用户自己的信息
     *
     * @param callback
     */
    public static void getBaseInfo(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getBaseInfo")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_BASE_INFO)
                .execute(callback);
    }

    /**
     * 获取用户的直播记录，个人页面用的时候 touid传自己的uid
     *
     * @param callback
     */
    public static void getLiverecord(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getLiverecord")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(GET_LIVERECORD)
                .execute(callback);
    }

    /**
     * 上传头像，用post
     */
    public static void updateAvatar(File file, HttpCallback callback) {
        OkGo.<JsonBean>post(HTTP_URL + "/?service=User.updateAvatar")
                .isMultipart(true)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("file", file)
                .tag(UPDATE_AVATAR)
                .execute(callback);
    }


    /**
     * 更新用户资料
     *
     * @param fields   用户资料 ,以json形式出现
     * @param callback
     */
    public static void updateFields(String fields, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.updateFields")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("fields", fields)
                .tag(UPDATE_FIELDS)
                .execute(callback);
    }

    /**
     * 修改密码
     *
     * @param oldpass  旧密码
     * @param pass     新密码
     * @param pass2    新密码
     * @param callback
     */
    public static void updatePass(String oldpass, String pass, String pass2, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.updatePass")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("oldpass", oldpass)
                .params("pass", pass)
                .params("pass2", pass2)
                .tag(UPDATE_PASS)
                .execute(callback);
    }

    /**
     * 获取礼物列表，同时会返回剩余的钱
     *
     * @param callback
     */
    public static void getGiftList(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.getGiftList")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_GIFT_LIST)
                .execute(callback);
    }


    /**
     * 观众给主播送礼物
     *
     * @param liveuid   主播的uid
     * @param giftid    礼物的id
     * @param giftcount 礼物的数量，默认是1
     * @param stream    主播直播间的stream
     * @param callback
     */
    public static void sendGift(String liveuid, String giftid, String giftcount, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.sendGift")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveuid)
                .params("giftid", giftid)
                .params("giftcount", giftcount)
                .params("stream", stream)
                .tag(SEND_GIFT)
                .execute(callback);
    }


    /**
     * 检查直播间状态，是否收费 是否有密码等
     *
     * @param liveuid  主播的uid
     * @param stream   主播的stream
     * @param callback
     */
    public static void checkLive(String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.checkLive")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveuid)
                .params("stream", stream)
                .tag(CHECK_LIVE)
                .execute(callback);
    }

    public static void enterRoom(String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.enterRoom")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveuid)
                .params("city", AppConfig.getInstance().getCity())
                .params("stream", stream)
                .tag(ENTER_ROOM)
                .execute(callback);
    }

    /**
     * 发送弹幕
     *
     * @param liveuid  主播的uid
     * @param stream   主播直播间的stream
     * @param callback
     */
    public static void sendBarrage(String content, String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.sendBarrage")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveuid)
                .params("stream", stream)
                .params("giftid", "1")
                .params("giftcount", "1")
                .params("content", content)
                .tag(SEND_BARRAGE)
                .execute(callback);
    }

    /**
     * 直播间点击聊天列表出现的弹窗
     */
    public static void getPop(String touid, String liveuid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.getPop")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .params("liveuid", liveuid)
                .tag(GET_POP)
                .execute(callback);
    }

    /**
     * 主播查看当前直播间的管理员列表
     */
    public static void getAdminList(String liveuid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.getAdminList")
                .params("liveuid", liveuid)
                .tag(GET_ADMIN_LIST)
                .execute(callback);
    }

    /**
     * 主播设置或删除直播间的管理员
     */
    public static void setAdmin(String touid, String liveuid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.setAdmin")
                .params("liveuid", liveuid)
                .params("touid", touid)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(SET_ADMIN)
                .execute(callback);
    }

    /**
     * 主播或管理员踢人
     */
    public static void kicking(String touid, String liveuid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.kicking")
                .params("liveuid", liveuid)
                .params("touid", touid)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(KICKING)
                .execute(callback);
    }


    /**
     * 主播或管理员禁言
     */
    public static void setShutUp(String touid, String liveuid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.setShutUp")
                .params("liveuid", liveuid)
                .params("touid", touid)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(SET_SHUT_UP)
                .execute(callback);
    }

    /**
     * 超管关闭直播间或禁用账户
     */
    public static void superStopRoom(String liveuid, String type, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.superStopRoom")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", liveuid)
                .params("type", type)
                .tag(SUPER_STOP_ROOM)
                .execute(callback);
    }

    /**
     * 举报
     */
    public static void setReport(String touid, String content, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.setReport")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("touid", touid)
                .params("content", content)
                .tag(SET_REPORT)
                .execute(callback);
    }


    /**
     * 主播开播
     *
     * @param title    直播标题
     * @param type     直播类型 普通 密码 收费等
     * @param type_val 密码 价格等
     * @param file     封面图片文件
     * @param callback
     */
    public static void createRoom(String title, String type, String type_val, File file, HttpCallback callback) {
        UserBean u = AppConfig.getInstance().getUserBean();
        PostRequest<JsonBean> request = OkGo.<JsonBean>post(HTTP_URL + "/?service=Live.createRoom")
                .params("title", title)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("user_nicename", u.getUser_nicename())
                .params("avatar", u.getAvatar())
                .params("avatar_thumb", u.getAvatar_thumb())
                .params("city", AppConfig.getInstance().getCity())
                .params("province", u.getProvince())
                .params("lat", AppConfig.getInstance().getLat())
                .params("lng", AppConfig.getInstance().getLng())
                .params("type", type)
                .params("type_val", type_val)
                .tag(CREATE_ROOM);
        if (file != null) {
            request.params("file", file);
        }
        request.execute(callback);
    }

    /**
     * 修改直播状态，把开播信息显示在主页上
     *
     * @param stream
     * @param status
     * @param callback
     */
    public static void changeLive(String stream, String status, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.changeLive")
                .params("stream", stream)
                .params("status", status)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(CHANGE_LIVE)
                .execute(callback);
    }


    /**
     * 修改直播状态，告诉服务器说，我要结束直播了
     *
     * @param stream
     * @param callback
     */
    public static void stopRoom(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.stopRoom")
                .params("stream", stream)
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(STOP_ROOM)
                .execute(callback);
    }


    /**
     * 直播结束后，获取直播收益，观看人数，时长等信息
     *
     * @param stream
     * @param callback
     */
    public static void stopLiveInfo(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.stopInfo")
                .params("stream", stream)
                .tag(STOP_LIVE_INFO)
                .execute(callback);
    }

    /**
     * 主播添加背景音乐时，搜索歌曲
     *
     * @param key      关键字
     * @param callback
     */
    public static void searchMusic(String key, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Music.searchMusic")
                .params("key", key)
                .tag(SEARCH_MUSIC)
                .execute(callback);
    }

    /**
     * 获取歌曲的地址 和歌词的地址
     *
     * @param audio_id
     * @param callback
     */
    public static void getDownMusicUrl(String audio_id, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Music.getDownurl")
                .params("audio_id", audio_id)
                .tag(GET_DOWN_MUSIC_URL)
                .execute(callback);
    }

    /**
     * 主播切换计时收费模式
     *
     * @param stream
     * @param typeVal
     * @param callback
     */
    public static void changeLiveType(String stream, String typeVal, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.changeLiveType")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("type", "3")
                .params("type_val", typeVal)
                .tag(CHANGE_LIVE_TYPE)
                .execute(callback);
    }


    /**
     * 当直播间是门票收费，计时收费或切换成计时收费的时候，观众请求这个接口
     *
     * @param liveuid
     * @param stream
     * @param callback
     */
    public static void roomCharge(String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.roomCharge")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("liveuid", liveuid)
                .tag(ROOM_CHARGE)
                .execute(callback);
    }

    /**
     * 当直播间是计时收费的时候，观众每隔一段时间请求这个接口
     *
     * @param liveuid
     * @param stream
     * @param callback
     */
    public static void timeCharge(String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.timeCharge")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("liveuid", liveuid)
                .tag(TIME_CHARGE)
                .execute(callback);
    }

    /**
     * 充值页面，我的钻石
     */
    public static void getBalance(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getBalance")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_BALANCE)
                .execute(callback);
    }


    /**
     * 用支付宝充值 的时候在服务端生成订单号
     *
     * @param money    RMB价格
     * @param changeid 要购买的钻石的id
     * @param coin     要购买的钻石的数量
     * @param callback
     */
    public static void getAliOrder(String money, String changeid, int coin, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Charge.getAliOrder")
                .params("uid", AppConfig.getInstance().getUid())
                .params("money", money)
                .params("changeid", changeid)
                .params("coin", coin)
                .tag(GET_ALI_ORDER)
                .execute(callback);
    }

    /**
     * 用微信支付充值 的时候在服务端生成订单号
     *
     * @param money    RMB价格
     * @param changeid 要购买的钻石的id
     * @param coin     要购买的钻石的数量
     * @param callback
     */
    public static void getWxOrder(String money, String changeid, int coin, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Charge.getWxOrder")
                .params("uid", AppConfig.getInstance().getUid())
                .params("money", money)
                .params("changeid", changeid)
                .params("coin", coin)
                .tag(GET_WX_ORDER)
                .execute(callback);
    }


    /**
     * 我的收益
     */
    public static void getProfit(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.getProfit")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_PROFIT)
                .execute(callback);
    }

    /**
     * 我的收益,提现
     */
    public static void getCash(String money, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.setCash")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("money", money)
                .tag(GET_CASH)
                .execute(callback);
    }


    /**
     * 首页登录奖励
     */
    public static void getBonus(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.Bonus")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_BONUS)
                .execute(callback);
    }

    /**
     * 竞拍加价
     */
    public static void setAuction(String auctionid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.setAuction")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("auctionid", auctionid)
                .tag(SET_AUCTION)
                .execute(callback);
    }

    /**
     * 竞拍结束
     */
    public static void auctionEnd(String auctionid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.auctionEnd")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("auctionid", auctionid)
                .tag(AUCTION_END)
                .execute(callback);
    }

    /**
     * 竞拍成功后付款
     */
    public static void setBidPrice(String auctionid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.setBidPrice")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("auctionid", auctionid)
                .tag(SET_BID_PRICE)
                .execute(callback);
    }

    /**
     * 获取用户钻石余额
     */
    public static void getCoin(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.getCoin")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .tag(GET_COIN)
                .execute(callback);
    }

    /**
     * 获取当前直播间的用户列表
     */
    public static void getUserList(String liveuid, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.getUserLists")
                .params("liveuid", liveuid)
                .params("stream", stream)
                .tag(GET_USER_LIST)
                .execute(callback);
    }


    //用于用户首次登录设置分销关系
    public static void setDistribut(String code, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=User.setDistribut")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("code", code)
                .tag(SET_DISTRIBUT)
                .execute(callback);
    }

    //用于用户首次登录推荐
    public static void getRecommend(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.getRecommend")
                .params("uid", AppConfig.getInstance().getUid())
                .tag(GET_RECOMMEND)
                .execute(callback);
    }

    //用于用户首次登录推荐,关注主播
    public static void attentRecommend(String touid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.attentRecommend")
                .params("uid", AppConfig.getInstance().getUid())
                .params("touid", touid)
                .tag(ATTENT_RECOMMEND)
                .execute(callback);
    }


    //排行榜  收益榜
    public static void profitList(String type, int p, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.profitList")
                .params("uid", AppConfig.getInstance().getUid())
                .params("type", type)
                .params("p", p)
                .tag(PROFIT_LIST)
                .execute(callback);
    }

    //排行榜  消费榜
    public static void consumeList(String type, int p, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Home.consumeList")
                .params("uid", AppConfig.getInstance().getUid())
                .params("type", type)
                .params("p", p)
                .tag(CONSUME_LIST)
                .execute(callback);
    }

    /**********************
     * 游戏
     *****************/


    //创建炸金花游戏
    public static void gameJinhuaCreate(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Jinhua")
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .tag(GAME_JINHUA_CREATE)
                .execute(callback);
    }


    //炸金花游戏下注
    public static void gameJinhuaBet(String gameid, String coin, String grade, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.JinhuaBet")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .tag(GAME_JINHUA_BET)
                .execute(callback);
    }


    //游戏结果出来后，观众获取自己赢到的金额
    public static void gameSettle(String gameid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.settleGame")
                .params("uid", AppConfig.getInstance().getUid())
                .params("gameid", gameid)
                .tag(GAME_SETTLE)
                .execute(callback);
    }


    //创建海盗船长游戏
    public static void gameHaidaoCreate(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Taurus")
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .tag(GAME_HAIDAO_CREATE)
                .execute(callback);
    }

    //海盗船长游戏下注
    public static void gameHaidaoBet(String gameid, String coin, String grade, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Taurus_Bet")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .tag(GAME_HAIDAO_BET)
                .execute(callback);
    }


    //创建开心牛仔游戏
    public static void gameNiuzaiCreate(String stream, String bankerid, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Cowboy")
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("bankerid", bankerid)
                .tag(GAME_NIUZAI_CREATE)
                .execute(callback);
    }

    //开心牛仔游戏下注
    public static void gameNiuzaiBet(String gameid, String coin, String grade, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Cowboy_Bet")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .tag(GAME_NIUZAI_BET)
                .execute(callback);
    }


    //开心牛仔获取庄家列表,列表第一个为当前庄家
    public static void gameNiuGetBanker(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.getBanker")
                .params("stream", stream)
                .tag(GAME_NIU_GET_BANKER)
                .execute(callback);
    }

    /**
     * 开心牛仔申请上庄
     *
     * @param stream 押金金额
     */
    public static void gameNiuSetBanker(String stream, String deposit, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.setBanker")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .params("deposit", deposit)
                .tag(GAME_NIU_SET_BANKER)
                .execute(callback);
    }

    //开心牛仔申请下庄
    public static void gameNiuQuitBanker(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.quietBanker")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .tag(GAME_NIU_QUIT_BANKER)
                .execute(callback);
    }


    //开心牛仔庄家流水
    public static void gameNiuBankerWater(String bankerId, String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.getBankerProfit")
                .params("uid", bankerId)
                .params("stream", stream)
                .tag(GAME_NIU_BANKER_WATER)
                .execute(callback);
    }

    //开心牛仔游戏胜负记录
    public static void gameNiuRecord(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.getGameRecord")
                .params("action", "4")
                .params("stream", stream)
                .tag(GAME_NIU_RECORD)
                .execute(callback);
    }

    //创建二八贝游戏
    public static void gameEbbCreate(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Cowry")
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .tag(GAME_EBB_CREATE)
                .execute(callback);
    }

    //二八贝下注
    public static void gameEbbBet(String gameid, String coin, String grade, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Cowry_Bet")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .tag(GAME_EBB_BET)
                .execute(callback);
    }

    //创建转盘游戏
    public static void gameLuckPanCreate(String stream, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Dial")
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("stream", stream)
                .tag(GAME_LUCK_PAN_CREATE)
                .execute(callback);
    }

    //转盘游戏下注
    public static void gameLuckPanBet(String gameid, String coin, String grade, HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Game.Dial_Bet")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("gameid", gameid)
                .params("coin", coin)
                .params("grade", grade)
                .tag(GAME_LUCK_PAN_BET)
                .execute(callback);
    }

    /**
     * 观众跟主播连麦时，获取自己的流地址
     */
    public static void getLinkMicStream(HttpCallback callback) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Linkmic.RequestLVBAddrForLinkMic")
                .params("uid", AppConfig.getInstance().getUid())
                .tag(GET_LINKMICSTREAM)
                .execute(callback);
    }

    /**
     * 主播连麦成功后，要把这些信息提交给服务器
     * @param touid 上麦会员ID
     * @param pull_url 连麦用户播流地址
     */
    public static void linkMicShowVideo(String touid,String pull_url) {
        OkGo.<JsonBean>get(HTTP_URL + "/?service=Live.showVideo")
                .params("uid", AppConfig.getInstance().getUid())
                .params("token", AppConfig.getInstance().getToken())
                .params("liveuid", AppConfig.getInstance().getUid())
                .params("touid",touid)
                .params("pull_url",pull_url)
                .tag(LINK_MIC_SHOW_VIDEO)
                .execute(new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {

                    }
                });
    }
}




