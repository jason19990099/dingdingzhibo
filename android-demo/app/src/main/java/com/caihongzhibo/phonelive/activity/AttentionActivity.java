package com.caihongzhibo.phonelive.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.adapter.AttentionAdapter;
import com.caihongzhibo.phonelive.bean.AttentionBean;
import com.caihongzhibo.phonelive.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.DialogUitl;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/8/12.
 * 关注 粉丝 页面
 */

public class AttentionActivity extends AbsActivity {

    private String mTouid;
    private String mType;//0是关注 1是粉丝
    private View mNoResult;
    private int mSex;//对方的性别，1男  2女
    private RecyclerView mRecyclerView;
    private AttentionAdapter mAdapter;
    private TextView mResultTextView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_attention;
    }

    @Override
    protected void main() {
        Intent intent = getIntent();
        mTouid = intent.getStringExtra("touid");
        mType = intent.getStringExtra("type");
        mSex = intent.getIntExtra("sex", 0);
        if ("0".equals(mType)) {
            setTitle(getString(R.string.attention2));
        } else if ("1".equals(mType)) {
            setTitle(getString(R.string.fans));
        }
        initView();
        initData();
    }

    private void initView() {
        mNoResult = findViewById(R.id.no_result);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
        mResultTextView = (TextView) findViewById(R.id.result_text);
        if ("0".equals(mType)) {
            mResultTextView.setText(mSex == 1 ? getString(R.string.he_not_have_attention) : getString(R.string.she_not_have_attention));
        } else if ("1".equals(mType)) {
            mResultTextView.setText(mSex == 1 ? getString(R.string.he_not_have_fans) : getString(R.string.she_not_have_fans));
        }
    }

    private void initData() {
        if ("0".equals(mType)) {
            HttpUtil.getFollowsList(mTouid, mHttpCallback);
        } else if ("1".equals(mType)) {
            HttpUtil.getFansList(mTouid, mHttpCallback);
        }

    }

    private HttpCallback mHttpCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            List<AttentionBean> list = JSON.parseArray(Arrays.toString(info), AttentionBean.class);
            if (list.size() > 0) {
                if (mNoResult.getVisibility() == View.VISIBLE) {
                    mNoResult.setVisibility(View.GONE);
                }
                if (mAdapter == null) {
                    mAdapter = new AttentionAdapter(mContext, list);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setList(list);
                }
            } else {
                if (mAdapter != null) {
                    mAdapter.clear();
                }
                if (mNoResult.getVisibility() == View.GONE) {
                    mNoResult.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public Dialog createLoadingDialog() {
            return DialogUitl.loadingDialog(mContext);
        }

        @Override
        public boolean showLoadingDialog() {
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.SET_ATTENTION);
        HttpUtil.cancel(HttpUtil.GET_FOLLOWS_LIST);
        HttpUtil.cancel(HttpUtil.GET_FANS_LIST);
        super.onDestroy();
        mAdapter=null;
        mRecyclerView=null;
    }
}
