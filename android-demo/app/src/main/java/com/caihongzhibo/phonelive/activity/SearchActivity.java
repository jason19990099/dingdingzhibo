package com.caihongzhibo.phonelive.activity;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.adapter.AttentionAdapter;
import com.caihongzhibo.phonelive.bean.AttentionBean;
import com.caihongzhibo.phonelive.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.ToastUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/8/10.
 * 主页搜索页面
 */

public class SearchActivity extends AbsActivity implements View.OnClickListener {

    private EditText mEditText;
    private View mClear;
    private View mNoResult;//没有结果
    private RecyclerView mRecyclerView;
    private AttentionAdapter mAdapter;
    private InputMethodManager imm;
    private boolean isSearching;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void main() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mEditText = (EditText) findViewById(R.id.search_input);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                    search();
                    return true;
                }
                return false;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (mClear.getVisibility() == View.GONE) {
                        mClear.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mClear.getVisibility() == View.VISIBLE) {
                        mClear.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mClear = findViewById(R.id.btn_clear);
        mClear.setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
        mNoResult = findViewById(R.id.no_result);
    }

    @Override
    public void onBackPressed() {
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        mEditText.setText("");
        if(mAdapter!=null){
            mAdapter.clear();
        }
        if (mNoResult.getVisibility() == View.GONE) {
            mNoResult.setVisibility(View.VISIBLE);
        }
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        mEditText.requestFocus();
    }

    private void search() {
        if(isSearching){
            return;
        }
        isSearching=true;
        String key = mEditText.getText().toString();
        if ("".equals(key)) {
            return;
        }
        HttpUtil.search(key, mHttpCallback);
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
                ToastUtil.show(getString(R.string.search_no_result));
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            isSearching=false;
        }

        @Override
        public Dialog createLoadingDialog() {
            return DialogUitl.loadingDialog(mContext,getString(R.string.search_ing));
        }

        @Override
        public boolean showLoadingDialog() {
            return true;
        }
    };

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.SET_ATTENTION);
        HttpUtil.cancel(HttpUtil.SEARCH);
        super.onDestroy();
        mAdapter=null;
        mRecyclerView=null;
        imm=null;
    }

}
