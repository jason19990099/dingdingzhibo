package com.caihongzhibo.phonelive.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.LiveActivity;
import com.caihongzhibo.phonelive.activity.UserInfoActivity;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.event.LiveSettingCloseEvent;
import com.caihongzhibo.phonelive.glide.ImgLoader;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.socket.SocketUtil;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.IconUitl;
import com.caihongzhibo.phonelive.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2017/8/25.
 */

public class LiveUserInfoFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private ImageView mAnchorLevel;
    private ImageView mLevel;
    private TextView mID;
    private TextView mCity;
    private TextView mAttention;
    private TextView mFans;
    private TextView mCharge;
    private TextView mHarvest;
    private TextView mBtnAttention;
    private View mBtnChat;
    private View mBtnHome;
    private View mBtnSetting;
    private View mBtnReport;
    private View mLoading;
    private String mTouid;
    private String mLiveuid;
    private UserBean mUserBean;
    private int mAction;
    private int mIsAttention;
    private TextView tv_knick, tv_shutup;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_live_user, null, false);
        dialog.setContentView(mRootView);
//        Window window = dialog.getWindow();
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.width = DpUtil.dp2px(280);
//        params.height = DpUtil.dp2px(450);
//        params.gravity = Gravity.CENTER;
//        window.setAttributes(params);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        mAvatar = (ImageView) mRootView.findViewById(R.id.avatar);
        mName = (TextView) mRootView.findViewById(R.id.name);
        mSex = (ImageView) mRootView.findViewById(R.id.sex);
        mAnchorLevel = (ImageView) mRootView.findViewById(R.id.anchor_level);
        mLevel = (ImageView) mRootView.findViewById(R.id.user_level);
        mID = (TextView) mRootView.findViewById(R.id.id_value);
        mCity = (TextView) mRootView.findViewById(R.id.city);
        mAttention = (TextView) mRootView.findViewById(R.id.attention);
        mFans = (TextView) mRootView.findViewById(R.id.fans);
        mCharge = (TextView) mRootView.findViewById(R.id.charge);
        mHarvest = (TextView) mRootView.findViewById(R.id.harvest);
        mBtnAttention = (TextView) mRootView.findViewById(R.id.btn_attention);
        mBtnChat = mRootView.findViewById(R.id.btn_chat);
        mBtnHome = mRootView.findViewById(R.id.btn_home);
        mBtnReport = mRootView.findViewById(R.id.btn_report);
        mBtnSetting = mRootView.findViewById(R.id.btn_setting);
        mLoading = mRootView.findViewById(R.id.loading);
        mBtnAttention.setOnClickListener(this);
        mBtnChat.setOnClickListener(this);
        mBtnHome.setOnClickListener(this);
        mBtnReport.setOnClickListener(this);
        mBtnSetting.setOnClickListener(this);

        tv_knick = (TextView) mRootView.findViewById(R.id.tv_knick);
        tv_knick.setOnClickListener(this);
        tv_shutup = (TextView) mRootView.findViewById(R.id.tv_shutup);
        tv_shutup.setOnClickListener(this);


        mRootView.findViewById(R.id.btn_close).setOnClickListener(this);
        Bundle bundle = getArguments();
        mTouid = bundle.getString("touid");
        mLiveuid = bundle.getString("liveuid");
        if (mTouid.equals(AppConfig.getInstance().getUid())) {
            mBtnAttention.setVisibility(View.GONE);
            mBtnChat.setVisibility(View.GONE);
            tv_knick.setVisibility(View.GONE);
            tv_shutup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        HttpUtil.getPop(mTouid, mLiveuid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    UserBean bean = JSON.toJavaObject(obj, UserBean.class);
                    mUserBean = bean;
                    ImgLoader.display(bean.getAvatar(), mAvatar);
                    mName.setText(bean.getUser_nicename());
                    mSex.setImageResource(IconUitl.getSexDrawable(bean.getSex()));
                    mAnchorLevel.setImageResource(IconUitl.getAnchorLiveDrawable(bean.getLevel_anchor()));
                    mLevel.setImageResource(IconUitl.getAudienceDrawable(bean.getLevel()));
                    String liangNum = bean.getLiang().getName();
                    if (!"0".equals(liangNum)) {
                        mID.setText(mContext.getString(R.string.liang) + ":" + liangNum);
                    } else {
                        mID.setText("ID:" + bean.getId());
                    }
                    mCity.setText(bean.getCity());
                    mAttention.setText(mContext.getString(R.string.attention2) + ":" + bean.getFollows());
                    mFans.setText(mContext.getString(R.string.fans) + ":" + bean.getFans());
                    mCharge.setText(mContext.getString(R.string.songchu) + "：" + bean.getConsumption());
                    mHarvest.setText(mContext.getString(R.string.shouru) + "：" + bean.getVotestotal());
                    mIsAttention = obj.getIntValue("isattention");
                    if (mIsAttention == 1) {
                        mBtnAttention.setText(mContext.getString(R.string.attention));
                    } else if (mIsAttention == 0) {
                        mBtnAttention.setText(mContext.getString(R.string.attention2));
                    }
                    mAction = obj.getIntValue("action");
                    switch (mAction) {
                        case 0://自己点自己
                            //什么也不做
                            break;
                        case 30://普通其他用户,显示举报
                            mBtnReport.setVisibility(View.VISIBLE);
                            break;
                        case 40://自己是房间管理员,显示设置
                            mBtnSetting.setVisibility(View.VISIBLE);
                            break;
                        case 60://超管管理主播
                            mBtnSetting.setVisibility(View.VISIBLE);
                            break;
                        case 501://主播操作普通用户
                        case 502://主播操作房间管理员
                            mBtnSetting.setVisibility(View.VISIBLE);
                            break;
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public void onFinish() {
                mLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_attention:
                setAttention();
                break;
            case R.id.btn_chat:
                openMsgWindow();
                break;
            case R.id.btn_home:
                forwardHomePage();
                break;
            case R.id.btn_report:
                report();
                break;
            case R.id.btn_setting:
                openSettingWindow();
                break;
            case R.id.btn_close:
                dismiss();
                break;
            case R.id.tv_knick: //踢线
                kick();
                break;
            case R.id.tv_shutup: //禁言
                setShutUp();
                break;
        }
    }


    private void setAttention() {
        if (mIsAttention == 0) {
            HttpUtil.setAttention(mTouid, mAttentionCallback);
        }
    }

    private CommonCallback<Integer> mAttentionCallback = new CommonCallback<Integer>() {
        @Override
        public void callback(Integer isAttention) {
            mIsAttention = isAttention;
            if (isAttention == 1) {
                mBtnAttention.setText(getString(R.string.attention));
                if (mTouid.equals(mLiveuid)) {
                    SocketUtil.getInstance().sendSystemMessage(AppConfig.getInstance().getUserBean().getUser_nicename() + getString(R.string.attention_anchor));
                }
            } else if (isAttention == 0) {
                mBtnAttention.setText(getString(R.string.attention2));
            }
        }
    };

    private void forwardHomePage() {
        dismiss();
        Intent intent = new Intent(mContext, UserInfoActivity.class);
        intent.putExtra("touid", mTouid);
        mContext.startActivity(intent);
    }

    private void openMsgWindow() {
        if (mUserBean != null) {
            dismiss();
            ((LiveActivity) mContext).openChatRoomWindow(mUserBean, mIsAttention);
        }
    }

    private void openSettingWindow() {
        if (mUserBean != null) {
            ((LiveActivity) mContext).openSettingWindow(mAction, mUserBean);
        }
    }

    private void report() {
        if (mUserBean != null) {
            DialogUitl.confirmDialog(mContext, mContext.getString(R.string.tip), mContext.getString(R.string.confrim_report), new DialogUitl.Callback() {
                @Override
                public void confirm(Dialog dialog) {
                    dialog.dismiss();
                    HttpUtil.setReport(mTouid, mContext.getString(R.string.invalid), mReportCallback);
                }

                @Override
                public void cancel(Dialog dialog) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private HttpCallback mReportCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
        }
    };

    /**
     * 踢人
     */
    private void kick() {
        HttpUtil.kicking(mUserBean.getId(), mLiveuid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    SocketUtil.getInstance().kickUser(mUserBean.getId(), mUserBean.getUser_nicename());
                    //发送这个消息是为了让个人信息弹窗关闭
                    EventBus.getDefault().post(new LiveSettingCloseEvent());
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 禁言
     */
    private void setShutUp() {
        HttpUtil.setShutUp(mUserBean.getId(), mLiveuid, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    SocketUtil.getInstance().shutUpUser(mUserBean.getId(), mUserBean.getUser_nicename(), "") ;
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        HttpUtil.cancel(HttpUtil.SET_ATTENTION);
        HttpUtil.cancel(HttpUtil.GET_POP);
        HttpUtil.cancel(HttpUtil.SET_REPORT);
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingDismiss(LiveSettingCloseEvent e) {
        dismiss();
    }
}
