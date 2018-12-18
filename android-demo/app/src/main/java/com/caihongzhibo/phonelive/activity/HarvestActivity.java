package com.caihongzhibo.phonelive.activity;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.ConfigBean;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.DialogUitl;

/**
 * Created by cxf on 2017/9/22.
 * 我的收益
 */

public class HarvestActivity extends AbsActivity {

    private TextView mCoinName;
    private TextView mVotes;
    private TextView mTotal;
    private TextView mToday;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_harvest;
    }

    @Override
    protected void main() {
        setTitle(getString(R.string.my_harvest));
        mCoinName = (TextView) findViewById(R.id.coin_name);
        ConfigBean config = AppConfig.getInstance().getConfig();
        if (config != null) {
            mCoinName.setText(config.getName_votes());
        }
        mVotes = (TextView) findViewById(R.id.votes);
        mTotal = (TextView) findViewById(R.id.total);
        mToday = (TextView) findViewById(R.id.taday);
        initData();
    }

    public void harvestClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cash://提现
                getCash();
                break;
            case R.id.btn_problem://常见问题
                forwardHtml();
                break;
        }
    }

    private void initData() {
        HttpUtil.getProfit(mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            JSONObject obj = JSON.parseObject(info[0]);
            mVotes.setText(obj.getString("votes"));
            mTotal.setText(obj.getString("total"));
            mToday.setText(obj.getString("todaycash"));
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
     * 跳转到H5页面
     */
    private void forwardHtml() {
        String url = AppConfig.HOST + "/index.php?g=portal&m=page&a=newslist&uid="
                + AppConfig.getInstance().getUid() + "&version=" + android.os.Build.VERSION.RELEASE + "&model=" + android.os.Build.MODEL;
        Intent intent = new Intent(mContext, WebActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void getCash() {
        HttpUtil.getCash("", mCashCallback);
    }

    private HttpCallback mCashCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                msg = JSON.parseObject(info[0]).getString("msg");
                initData();
            }
            DialogUitl.messageDialog(mContext,getString(R.string.tip),msg,null).show();
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


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_PROFIT);
        HttpUtil.cancel(HttpUtil.GET_CASH);
        super.onDestroy();
    }
}
