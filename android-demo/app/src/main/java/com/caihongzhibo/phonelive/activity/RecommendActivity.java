package com.caihongzhibo.phonelive.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.adapter.RecommendAdapter;
import com.caihongzhibo.phonelive.bean.RecommendBean;
import com.caihongzhibo.phonelive.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/10/23.
 * 首次登陆  推荐关注
 */

public class RecommendActivity extends AbsActivity {

    private RecyclerView mRecyclerView;
    private RecommendAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recommend;
    }

    @Override
    protected void main() {
        initView();
        initData();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
        initData();
    }

    private void initData() {
        String recommend = getIntent().getStringExtra("recommend");
        List<RecommendBean> list = JSON.parseArray(recommend, RecommendBean.class);
        mAdapter = new RecommendAdapter(mContext, list);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void attention() {
        if (mAdapter == null) {
            forwawrdMainActivity();
        } else {
            String s = mAdapter.getCheckList();
            if ("".equals(s)) {
                forwawrdMainActivity();
            } else {
                HttpUtil.attentRecommend(s, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            forwawrdMainActivity();
                        }
                    }
                });
            }
        }
    }

    public void recommendClick(View v) {
        switch (v.getId()) {
            case R.id.btn_enter:
                attention();
                break;
            case R.id.btn_skip:
                forwawrdMainActivity();
                break;
        }
    }

    private void forwawrdMainActivity() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.ATTENT_RECOMMEND);
        super.onDestroy();
    }
}
