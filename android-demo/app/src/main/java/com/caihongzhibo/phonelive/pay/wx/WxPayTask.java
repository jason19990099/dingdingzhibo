package com.caihongzhibo.phonelive.pay.wx;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.bean.ChargeBean;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.pay.PayCallback;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2017/9/22.
 */

public class WxPayTask {

    public static String sAppId;
    private IWXAPI mApi;
    private Context mContext;
    private ChargeBean mBean;
    private PayCallback mPayCallback;

    public WxPayTask(Context context, ChargeBean bean, PayCallback callback) {
        mContext = context;
        mBean = bean;
        mPayCallback = callback;
        mApi = WXAPIFactory.createWXAPI(AppContext.sInstance, sAppId);
        mApi.registerApp(sAppId);
        EventBus.getDefault().register(this);
    }


    public void getOrder() {
        if(TextUtils.isEmpty(sAppId)){
            ToastUtil.show("微信支付未接入");
            return;
        }
        HttpUtil.getWxOrder(mBean.getMoney(), mBean.getId(), mBean.getCoin(), new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                JSONObject obj = JSON.parseObject(info[0]);
                PayReq req = new PayReq();
                req.appId = sAppId;
                req.partnerId = obj.getString("partnerid");
                req.prepayId = obj.getString("prepayid");
                req.packageValue = "Sign=WXPay";
                req.nonceStr = obj.getString("noncestr");
                req.timeStamp = obj.getString("timestamp");
                req.sign = obj.getString("sign");
                boolean result = mApi.sendReq(req);
                if (!result) {
                    ToastUtil.show("充值失败");
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
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPayResponse(BaseResp resp) {
        L.e("resp---微信支付回调---->" + resp.errCode);
        if (mPayCallback != null) {
            if (0 == resp.errCode) {//支付成功
                mPayCallback.onSuccess(mBean.getCoin() + mBean.getGive());
            } else {//支付失败
                mPayCallback.onFailuer();
            }
        }
        mContext=null;
        mPayCallback=null;
        EventBus.getDefault().unregister(this);
    }


}
