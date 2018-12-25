package com.caihongzhibo.phonelive2.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.adapter.ChargeAdapter;
import com.caihongzhibo.phonelive2.bean.ChargeBean;
import com.caihongzhibo.phonelive2.fragment.ChargeFragment;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.pay.PayCallback;
import com.caihongzhibo.phonelive2.pay.ali.AliPayTask;
import com.caihongzhibo.phonelive2.pay.wx.WxPayTask;
import com.caihongzhibo.phonelive2.utils.DialogUitl;
import com.caihongzhibo.phonelive2.utils.ToastUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by cxf on 2017/9/19.
 * 充值页面
 */

public class ChargeActivity extends AbsActivity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private TextView mCoin;
    private ChargeAdapter mAdapter;
    private ChargeFragment mChargeFragment;
    private ChargeBean mSelectedBean;
    private String mTotalCoin;
    private String mFrom;
    private int mAliSwitch;//后台控制支付宝开启的开关  1开启 0 关闭
    private int mWxSwitch;//后台控制微信支付开启的开关

    @Override
    protected int getLayoutId() {
        return R.layout.activity_charge;
    }

    @Override
    protected void main() {
        setTitle(getString(R.string.my) + AppConfig.getInstance().getConfig().getName_coin());
        mListView = (ListView) findViewById(R.id.listView);
        View headView = LayoutInflater.from(mContext).inflate(R.layout.view_charge_head, mListView, false);
        mCoin = (TextView) headView.findViewById(R.id.coin);
        mListView.addHeaderView(headView);
        mListView.setOnItemClickListener(this);
        mFrom = getIntent().getStringExtra("from");
        initData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            mSelectedBean = mAdapter.getItem(position - 1);
            if (mChargeFragment == null) {
                mChargeFragment = new ChargeFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("ali", mAliSwitch);
                bundle.putInt("wx", mWxSwitch);
                mChargeFragment.setArguments(bundle);
            }
            if (!mChargeFragment.isAdded()) {
                mChargeFragment.show(getSupportFragmentManager(), "ChargeFragment");
            }
        }
    }

    private void initData() {
        HttpUtil.getBalance(mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            JSONObject obj = JSON.parseObject(info[0]);
            mTotalCoin = obj.getString("coin");
            mCoin.setText(mTotalCoin);
            List<ChargeBean> list = JSON.parseArray(obj.getString("rules"), ChargeBean.class);
            if (mAdapter == null) {
                mAdapter = new ChargeAdapter(mContext, list);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.setList(list);
            }
            mAliSwitch = obj.getIntValue("aliapp_switch");
            mWxSwitch = obj.getIntValue("wx_switch");
            AliPayTask.sPartner = obj.getString("aliapp_partner");
            AliPayTask.sSellerId = obj.getString("aliapp_seller_id");
            AliPayTask.sPrivateKey = obj.getString("aliapp_key_android");
            WxPayTask.sAppId = obj.getString("wx_appid");
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
     * 支付宝支付
     */
    public void aliPay() {
        new AliPayTask(this, mSelectedBean, mPayCallback).getOrder();
    }

    /**
     * 微信支付
     */
    public void wxPay() {
        new WxPayTask(this, mSelectedBean, mPayCallback).getOrder();
    }

    private PayCallback mPayCallback = new PayCallback() {
        @Override
        public void onSuccess(int coin) {
            BigDecimal bigDecimal = new BigDecimal(mTotalCoin);
            bigDecimal = bigDecimal.add(new BigDecimal(coin));
            mCoin.setText(bigDecimal.toString());
            ToastUtil.show(getString(R.string.charge_success));
            if ("live".equals(mFrom)) {//这是从直播间进来的充值的
                setResult(RESULT_OK);
            }
        }

        @Override
        public void onFailuer() {
            ToastUtil.show(getString(R.string.charge_failure));
        }
    };

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_ALI_ORDER);
        HttpUtil.cancel(HttpUtil.GET_WX_ORDER);
        HttpUtil.cancel(HttpUtil.GET_BALANCE);
        super.onDestroy();
    }
}
