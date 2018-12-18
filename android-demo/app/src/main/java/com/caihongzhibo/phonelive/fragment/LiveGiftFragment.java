package com.caihongzhibo.phonelive.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.ChargeActivity;
import com.caihongzhibo.phonelive.activity.LiveAudienceActivity;
import com.caihongzhibo.phonelive.adapter.GiftListAdapter;
import com.caihongzhibo.phonelive.bean.GiftBean;
import com.caihongzhibo.phonelive.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive.event.LiveRoomCloseEvent;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive.utils.DpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2017/8/19.
 * 观众直播间送礼物弹窗
 */

public class LiveGiftFragment extends DialogFragment implements OnItemClickListener<GiftBean>, View.OnClickListener {

    protected Context mContext;
    private View mRootView;
    private ViewPager mViewPager;
    private TextView mCoin;
    private TextView mSendBtn;
    private RelativeLayout mSendBtnLian;
    private View mLoadingView;
    private LayoutInflater mInflater;
    private List<List<GiftBean>> mList;

    //显示ViewPager小圆点
    private RadioGroup mIndicatorGroup;
    private GiftListAdapter[] mAdapters;
    private GiftBean mSelectBean;
    private TextView mLianText;

    private Handler mHandler;
    private int mCount;//倒计时的数字

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_gift, null, false);
        dialog.setContentView(mRootView);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = DpUtil.dp2px(240);
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        mViewPager = (ViewPager) mRootView.findViewById(R.id.viewPager);
        mInflater = LayoutInflater.from(mContext);
        mCoin = (TextView) mRootView.findViewById(R.id.coin);
        mLoadingView = mRootView.findViewById(R.id.loading);
        mIndicatorGroup = (RadioGroup) mRootView.findViewById(R.id.indicator_group);
        mSendBtn = (TextView) mRootView.findViewById(R.id.btn_send_gift);
        if (mSelectBean != null) {
            mSendBtn.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_btn_send_gift_checked));
        } else {
            mSendBtn.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_btn_send_gift_unchecked));
        }
        mSendBtn.setOnClickListener(this);
        mSendBtnLian = (RelativeLayout) mRootView.findViewById(R.id.btn_send_gift_lian);
        mSendBtnLian.setOnClickListener(this);
        mLianText = (TextView) mRootView.findViewById(R.id.lian_text);
        mRootView.findViewById(R.id.btn_charge).setOnClickListener(this);
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    mCount--;
                    if (mCount == 0) {
                        mSendBtnLian.setVisibility(View.GONE);
                        mSendBtn.setVisibility(View.VISIBLE);
                    } else {
                        mLianText.setText(String.valueOf(mCount));
                        sendEmptyMessageDelayed(0, 1000);
                    }
                }
            };
        }
        if (mList != null) {
            showGiftList();
        }
        HttpUtil.getGiftList(mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            JSONObject obj = JSON.parseObject(info[0]);
            mCoin.setText(obj.getString("coin"));
            List<GiftBean> list = JSON.parseArray(obj.getString("giftlist"), GiftBean.class);
            if (mList == null) {
                mList = new ArrayList<>();
                int page = list.size() / 8;
                int last = list.size() % 8;
                if (last != 0) {
                    page++;
                }
                int fromIndex;
                int toIndex;
                for (int i = 0; i < page; i++) {
                    fromIndex = i * 8;
                    toIndex = fromIndex + 8 <= list.size() ? fromIndex + 8 : list.size();
                    List<GiftBean> subList = new ArrayList<>();
                    for (int j = fromIndex; j < toIndex; j++) {
                        GiftBean bean = list.get(j);
                        bean.setPage(i);
                        bean.setPosition(j - i * 8);
                        subList.add(bean);
                    }
                    mList.add(subList);
                }
                showGiftList();
            }
        }

        @Override
        public void onFinish() {
            mLoadingView.setVisibility(View.GONE);
        }
    };


    private void addIndicator() {
        View v = mInflater.inflate(R.layout.view_gift_indicator, mIndicatorGroup, false);
        mIndicatorGroup.addView(v);
    }

    private void changeIndicatorColor(int position) {
        for (int i = 0; i < mIndicatorGroup.getChildCount(); i++) {
            RadioButton button = (RadioButton) mIndicatorGroup.getChildAt(i);
            if (i == position) {
                button.setChecked(true);
            } else {
                button.setChecked(false);
            }
        }
    }

    private RecyclerView getRecyclerView(int i) {
        RecyclerView recyclerView = new RecyclerView(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(params);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new NoAlphaItemAnimator());
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mAdapters[i] = new GiftListAdapter(mContext, mList.get(i));
        mAdapters[i].setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapters[i]);
        return recyclerView;
    }

    /**
     * 更新剩余的钱
     *
     * @param coin
     */
    public void updateCoin(String coin) {
        mCoin.setText(coin);
    }

    private void showGiftList() {
        int size = mList.size();
        final View[] views = new View[size];
        mAdapters = new GiftListAdapter[size];

        for (int i = 0; i < size; i++) {
            views[i] = getRecyclerView(i);
            addIndicator();
        }
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return views.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View v = views[position];
                container.addView(v);
                return v;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views[position]);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeIndicatorColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        changeIndicatorColor(0);
    }

    @Override
    public void onItemClick(GiftBean bean, int position) {
        bean.setChecked(true);
        if (mSelectBean != null) {
            if (bean.getId().equals(mSelectBean.getId())) {
                return;
            }
            mSelectBean.setChecked(false);
            mAdapters[mSelectBean.getPage()].notifyItemChanged(mSelectBean.getPosition(), GiftListAdapter.UNCHECKED);
            mAdapters[bean.getPage()].notifyItemChanged(bean.getPosition(), GiftListAdapter.CHECKED);
        } else {
            mSendBtn.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_btn_send_gift_checked));
            mAdapters[bean.getPage()].notifyItemChanged(bean.getPosition(), GiftListAdapter.CHECKED);
        }
        mSelectBean = bean;
        if (mSendBtn.getVisibility() == View.GONE) {
            mSendBtn.setVisibility(View.VISIBLE);
        }
        if (mSendBtnLian.getVisibility() == View.VISIBLE) {
            mSendBtnLian.setVisibility(View.GONE);
        }
        mHandler.removeMessages(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_gift:
                sendGift();
                break;
            case R.id.btn_send_gift_lian:
                sendGiftLian();
                break;
            case R.id.btn_charge://跳转到充值页面
                dismiss();
                startActivity(new Intent(mContext, ChargeActivity.class));
                break;
        }

    }

    private void sendGift() {
        if (mSelectBean == null) {
            return;
        }
        if (mSelectBean.getType() == 1) {
            mSendBtn.setVisibility(View.GONE);
            mSendBtnLian.setVisibility(View.VISIBLE);
            mCount = 5;
            mLianText.setText(String.valueOf(mCount));
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
        mSelectBean.setEvensend("n");
        doSendGift();
    }

    private void sendGiftLian() {
        if (mCount != 5) {
            mCount = 5;
            mLianText.setText(String.valueOf(mCount));
        }
        mHandler.removeMessages(0);
        if (mSelectBean.getType() == 1) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
        mSelectBean.setEvensend("y");
        doSendGift();
    }

    private void doSendGift() {
        ((LiveAudienceActivity) mContext).sendGift(mSelectBean);
    }

    @Override
    public void onDestroyView() {
        HttpUtil.cancel(HttpUtil.GET_GIFT_LIST);
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //接收直播间关闭事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveRoomCloseEvent(LiveRoomCloseEvent e) {
        dismiss();
    }
}
