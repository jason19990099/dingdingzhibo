package com.caihongzhibo.phonelive.presenter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.LiveAudienceActivity;
import com.caihongzhibo.phonelive.bean.LiveBean;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.MD5Util;
import com.caihongzhibo.phonelive.utils.ToastUtil;

/**
 * Created by cxf on 2017/9/29.
 */

public class CheckLivePresenter {

    private Context mContext;
    private LiveBean mSelectLiveBean;//选中的直播间信息
    private int mLiveType;//直播间的类型  普通 密码 门票 计时等
    private int mLiveTypeVal;//收费价格等
    private String mLiveTypeMsg;//直播间提示信息或房间密码

    public CheckLivePresenter(Context context) {
        mContext = context;
    }

    public void setSelectLiveBean(LiveBean bean) {
        mSelectLiveBean = bean;
    }

    /**
     * 观众 观看直播
     */
    public void watchLive() {
        HttpUtil.checkLive(mSelectLiveBean.getUid(), mSelectLiveBean.getStream(), mCheckLiveCallback);
    }

    private HttpCallback mCheckLiveCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                JSONObject obj = JSON.parseObject(info[0]);

                mLiveType = obj.getIntValue("type");
                mLiveTypeVal = obj.getIntValue("type_val");
                mLiveTypeMsg = obj.getString("type_msg");
                if (mLiveType == 1) {//密码房间
                    DialogUitl.inputDialog(mContext, mContext.getString(R.string.please_input_room_password), new DialogUitl.Callback3() {
                        @Override
                        public void confirm(Dialog dialog, String text) {
                            if ("".equals(text)) {
                                ToastUtil.show(mContext.getString(R.string.please_input_password));
                            } else {
                                String password = MD5Util.getMD5(text);
                                L.e("密码房间----password---->" + password);
                                if (mLiveTypeMsg.equalsIgnoreCase(password)) {
                                    dialog.dismiss();
                                    forwardLiveAudienceActivity();
                                } else {
                                    ToastUtil.show(mContext.getString(R.string.password_error));
                                }
                            }
                        }
                    }).show();
                } else if (mLiveType == 2 || mLiveType == 3) {//门票收费房间或计时收费房间
                    DialogUitl.confirmDialog(mContext, mContext.getString(R.string.tip), mLiveTypeMsg, new DialogUitl.Callback() {
                        @Override
                        public void confirm(Dialog dialog) {
                            dialog.dismiss();
                            roomCharge();
                        }

                        @Override
                        public void cancel(Dialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();

                } else {//普通房间
                    forwardLiveAudienceActivity();
                }
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
            return DialogUitl.loadingDialog(mContext);
        }
    };

    public void roomCharge() {
        HttpUtil.roomCharge(mSelectLiveBean.getUid(), mSelectLiveBean.getStream(), mRoomChargeCallback);
    }

    private HttpCallback mRoomChargeCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                forwardLiveAudienceActivity();
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
            return DialogUitl.loadingDialog(mContext);
        }
    };

    /**
     * 跳转到直播间
     */
    private void forwardLiveAudienceActivity() {
        Intent intent = new Intent(mContext, LiveAudienceActivity.class);
        intent.putExtra("liveBean", mSelectLiveBean);
        intent.putExtra("type", mLiveType);
        intent.putExtra("typeVal", mLiveTypeVal);
        mContext.startActivity(intent);
    }
}
