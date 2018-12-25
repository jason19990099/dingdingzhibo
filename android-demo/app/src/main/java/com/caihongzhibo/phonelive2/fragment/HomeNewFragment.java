package com.caihongzhibo.phonelive2.fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.model.Response;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.activity.MainActivity;
import com.caihongzhibo.phonelive2.adapter.HomeNewAdapter;
import com.caihongzhibo.phonelive2.bean.LiveBean;
import com.caihongzhibo.phonelive2.custom.RefreshLayout;
import com.caihongzhibo.phonelive2.glide.ItemDecoration;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.http.JsonBean;
import com.caihongzhibo.phonelive2.interfaces.MainEventListener;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2017/8/9.
 */

public class HomeNewFragment extends AbsFragment implements RefreshLayout.OnRefreshListener, MainEventListener {

    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private View mNoZhubo;//没有主播
    private View mLoadFailure;//加载失败
    private HomeNewAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_hot;
    }

    @Override
    protected void main() {
        initView();
    }

    private void initView() {
        mRefreshLayout = (RefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        //网格的分割线
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 4, 4);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRecyclerView.addItemDecoration(decoration);
        mNoZhubo = mRootView.findViewById(R.id.no_zhubo);
        mLoadFailure = mRootView.findViewById(R.id.load_failure);
        mRefreshLayout.setScorllView(mRecyclerView);
    }

    private void initData() {
        HttpUtil.getNew(mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (mLoadFailure != null && mLoadFailure.getVisibility() == View.VISIBLE) {
                mLoadFailure.setVisibility(View.GONE);
            }
            List<LiveBean> liveList = JSON.parseArray(Arrays.toString(info), LiveBean.class);
            if (liveList.size() > 0) {
                if (mNoZhubo != null && mNoZhubo.getVisibility() == View.VISIBLE) {
                    mNoZhubo.setVisibility(View.GONE);
                }
            } else {
                if (mNoZhubo != null && mNoZhubo.getVisibility() == View.GONE) {
                    mNoZhubo.setVisibility(View.VISIBLE);
                }
            }
            if (mAdapter == null) {
                mAdapter = new HomeNewAdapter(mContext, liveList);
                mAdapter.setOnItemClickListener(new OnItemClickListener<LiveBean>() {
                    @Override
                    public void onItemClick(LiveBean item, int position) {
                        ((MainActivity) getActivity()).watchLive(item);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setData(liveList);
            }
        }

        @Override
        public void onError(Response<JsonBean> response) {
            super.onError(response);
            if (mAdapter != null) {
                mAdapter.clearData();
            }
            if (mNoZhubo != null && mNoZhubo.getVisibility() == View.VISIBLE) {
                mNoZhubo.setVisibility(View.GONE);
            }
            if (mLoadFailure != null && mLoadFailure.getVisibility() == View.GONE) {
                mLoadFailure.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFinish() {
            if (mRefreshLayout != null) {
                mRefreshLayout.completeRefresh();
            }
        }
    };

    @Override
    public void onRefresh() {
        initData();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void loadData() {
        initData();
    }

    @Override
    public void onDestroyView() {
        HttpUtil.cancel(HttpUtil.GET_NEW);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
