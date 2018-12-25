package com.caihongzhibo.phonelive2.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.lzy.okgo.model.Response;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.adapter.OrderListAdapter;
import com.caihongzhibo.phonelive2.bean.ListBean;
import com.caihongzhibo.phonelive2.custom.RefreshLayout;
import com.caihongzhibo.phonelive2.event.AttentionEvent;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.http.JsonBean;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive2.utils.ToastUtil;
import com.caihongzhibo.phonelive2.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/2/2.
 */

public class OrderListFragment extends AbsFragment implements RefreshLayout.OnRefreshListener, OnItemClickListener<ListBean>, View.OnClickListener {

    //type:参数类型，day表示日榜，week表示周榜，month代表月榜，total代表总榜
    private static final String DAY = "day";
    private static final String WEEK = "week";
    private static final String MONTH = "month";
    private static final String TOTAL = "total";
    public static final int PROFIT = 0;//收益榜
    public static final int CONSUME = 1;//消费榜
    public static final String LIST_TYPE = "listType";//类型
    private int mListType;//列表类型  是收入榜还是消费榜
    private String mType = DAY;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private View mNoZhubo;//没有主播
    private View mLoadFailure;//加载失败
    private OrderListAdapter mAdapter;
    private int mPage;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_order_list;
    }

    @Override
    protected void main() {
        mListType = getArguments().getInt(LIST_TYPE, PROFIT);
        mRefreshLayout = (RefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mNoZhubo = mRootView.findViewById(R.id.no_zhubo);
        mLoadFailure = mRootView.findViewById(R.id.load_failure);
        mRefreshLayout.setScorllView(mRecyclerView);
        mRootView.findViewById(R.id.btn_list_day).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_list_week).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_list_month).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_list_all).setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onClick(View v) {
        String type = "";
        switch (v.getId()) {
            case R.id.btn_list_day:
                type = DAY;
                break;
            case R.id.btn_list_week:
                type = WEEK;
                break;
            case R.id.btn_list_month:
                type = MONTH;
                break;
            case R.id.btn_list_all:
                type = TOTAL;
                break;
        }
        if (!mType.equals(type)) {
            mType = type;
            initData();
        }
    }

    public void initData() {
        mPage = 1;
        if (mListType == PROFIT) {
            HttpUtil.profitList(mType, mPage, mRefreshCallback);
        } else if (mListType == CONSUME) {
            HttpUtil.consumeList(mType, mPage, mRefreshCallback);
        }
    }

    private HttpCallback mRefreshCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (mLoadFailure != null && mLoadFailure.getVisibility() == View.VISIBLE) {
                mLoadFailure.setVisibility(View.GONE);
            }
            if (info.length > 0) {
                List<ListBean> list = JSON.parseArray(Arrays.toString(info), ListBean.class);
                if (list.size() > 0) {
                    if (mNoZhubo != null && mNoZhubo.getVisibility() == View.VISIBLE) {
                        mNoZhubo.setVisibility(View.GONE);
                    }
                } else {
                    if (mAdapter != null) {
                        mAdapter.clear();
                    }
                    if (mNoZhubo != null && mNoZhubo.getVisibility() == View.GONE) {
                        mNoZhubo.setVisibility(View.VISIBLE);
                    }
                }
                if (mAdapter == null) {
                    mAdapter = new OrderListAdapter(list, mListType);
                    mAdapter.setItemClickListener(OrderListFragment.this);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    mAdapter.setData(list);
                }
            } else {
                if (mAdapter != null) {
                    mAdapter.clear();
                }
                if (mNoZhubo != null && mNoZhubo.getVisibility() == View.GONE) {
                    mNoZhubo.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        public void onError(Response<JsonBean> response) {
            super.onError(response);
            if (mAdapter != null) {
                mAdapter.clear();
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

    private HttpCallback mLoadMoreCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                List<ListBean> list = JSON.parseArray(Arrays.toString(info), ListBean.class);
                if (list.size() > 0) {
                    if (mAdapter != null) {
                        mAdapter.insertList(list);
                    }
                } else {
                    ToastUtil.show(WordUtil.getString(R.string.no_more_data));
                    mPage--;
                }
            } else {
                ToastUtil.show(msg);
            }
        }

        @Override
        public void onFinish() {
            if (mRefreshLayout != null) {
                mRefreshLayout.completeLoadMore();
            }
        }
    };


    @Override
    public void onRefresh() {
        initData();
    }

    @Override
    public void onLoadMore() {
        mPage++;
        if (mListType == PROFIT) {
            HttpUtil.profitList(mType, mPage, mLoadMoreCallback);
        } else if (mListType == CONSUME) {
            HttpUtil.consumeList(mType, mPage, mLoadMoreCallback);
        }
    }


    @Override
    public void onItemClick(ListBean item, int position) {
        if (item.getUid().equals(AppConfig.getInstance().getUid())) {
            ToastUtil.show(WordUtil.getString(R.string.cannot_attention_self));
            return;
        }
        HttpUtil.setAttention(item.getUid(), null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAttentionEvent(AttentionEvent e) {
        if(mAdapter!=null){
            mAdapter.setAttention(e.getTouid(), e.getIsAttention());
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        HttpUtil.cancel(HttpUtil.PROFIT_LIST);
        HttpUtil.cancel(HttpUtil.CONSUME_LIST);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
