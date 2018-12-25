package com.caihongzhibo.phonelive2.fragment;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.activity.AttentionActivity;
import com.caihongzhibo.phonelive2.activity.ChargeActivity;
import com.caihongzhibo.phonelive2.activity.EditProfileActivity;
import com.caihongzhibo.phonelive2.activity.EquipActivity;
import com.caihongzhibo.phonelive2.activity.HarvestActivity;
import com.caihongzhibo.phonelive2.activity.LiveRecordListActivity;
import com.caihongzhibo.phonelive2.activity.MainActivity;
import com.caihongzhibo.phonelive2.activity.SettingActivity;
import com.caihongzhibo.phonelive2.activity.ShopActivity;
import com.caihongzhibo.phonelive2.activity.WebActivity;
import com.caihongzhibo.phonelive2.activity.WebAuthActivity;
import com.caihongzhibo.phonelive2.activity.WebFenXiaoActivity;
import com.caihongzhibo.phonelive2.adapter.UserFunctionAdapter;
import com.caihongzhibo.phonelive2.bean.UserBean;
import com.caihongzhibo.phonelive2.bean.UserFunctionBean;
import com.caihongzhibo.phonelive2.http.HttpCallback;
import com.caihongzhibo.phonelive2.http.HttpUtil;
import com.caihongzhibo.phonelive2.interfaces.MainEventListener;
import com.caihongzhibo.phonelive2.interfaces.OnItemClickListener;

import java.util.List;

/**
 * Created by cxf on 2017/8/10.
 * MainActivity 个人页面
 */

public class UserFragment extends AbsFragment implements MainEventListener, View.OnClickListener, OnItemClickListener<UserFunctionBean> {

    private RecyclerView mRecyclerView;
    private UserFunctionAdapter mAdapter;
    private UserBean mUserBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user;
    }

    @Override
    protected void main() {
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    private void showData(UserBean bean, JSONObject obj) {
        List<UserFunctionBean> list = JSON.parseArray(obj.getString("list"), UserFunctionBean.class);
        if (mAdapter == null) {
            mAdapter = new UserFunctionAdapter(mContext, bean, list);
            mAdapter.setOnClickListener(this);
            mAdapter.setOnItemClickListener(this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(bean, list);
        }
    }


    @Override
    public void loadData() {
        HttpUtil.getBaseInfo(mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            JSONObject obj = JSON.parseObject(info[0]);
            mUserBean = JSON.toJavaObject(obj, UserBean.class);
            AppConfig.getInstance().saveUserInfo(info[0]);
            showData(mUserBean, obj);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).onChildResume(3);
    }

    /**
     * 切换fragment的时候执行这个
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            loadData();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit://编辑资料
                forwardEditProfile();
                break;
            case R.id.btn_live://直播
                Intent intent = new Intent(mContext, LiveRecordListActivity.class);
                intent.putExtra("touid", mUserBean.getId());
                startActivity(intent);
                break;
            case R.id.btn_attention://关注
                forwardAttention("0");
                break;
            case R.id.btn_fans://粉丝
                forwardAttention("1");
                break;
        }
    }

    @Override
    public void onItemClick(UserFunctionBean bean, int position) {
        switch (bean.getId()) {
            case 1://我的收益
                forwardHarvest();
                break;
            case 2://我的钻石
                forwardCharge();
                break;
            case 4://在线商城
                forwardShop();
                break;
            case 5://装备中心
                forwardEquip();
                break;
            case 13://个性设置
                forwardSetting();
                break;
            case 3://我的等级
            case 6://家族中心
            case 9://竞拍管理
            case 10://我的竞拍
            case 12://关于我们
            case 14://我的明细
                forwardHtml(bean.getHref());
                break;
            case 7://家族驻地
            case 11://我的认证
                forwardAuth(bean.getHref());
                break;
            case 8://三级分销
                forwardFenXiao(bean.getHref());
                break;

        }
    }


    /**
     * 跳转到关注  粉丝
     *
     * @param type 0是关注  1是粉丝
     */
    private void forwardAttention(String type) {
        Intent intent = new Intent(mContext, AttentionActivity.class);
        intent.putExtra("touid", mUserBean.getId());
        intent.putExtra("type", type);
        intent.putExtra("sex", mUserBean.getSex());
        startActivity(intent);
    }

    /**
     * 跳转在线商城
     */
    private void forwardShop() {
        startActivity(new Intent(mContext, ShopActivity.class));
    }

    /**
     * 跳转装备中心
     */
    private void forwardEquip() {
        startActivity(new Intent(mContext, EquipActivity.class));
    }

    /**
     * 跳转到H5页面
     */
    private void forwardHtml(String href) {
        Intent intent = new Intent(mContext, WebActivity.class);
        intent.putExtra("url", href + "&uid=" + AppConfig.getInstance().getUid() + "&token=" + AppConfig.getInstance().getToken());
        startActivity(intent);
    }

    /**
     * 跳转到申请认证页面
     */
    private void forwardAuth(String href) {
        Intent intent = new Intent(mContext, WebAuthActivity.class);
        intent.putExtra("url", href + "&uid=" + AppConfig.getInstance().getUid() + "&token=" + AppConfig.getInstance().getToken());
        startActivity(intent);
    }

    /**
     * 跳转到三级分销页面
     */
    private void forwardFenXiao(String href) {
        Intent intent = new Intent(mContext, WebFenXiaoActivity.class);
        intent.putExtra("url", href + "&uid=" + AppConfig.getInstance().getUid() + "&token=" + AppConfig.getInstance().getToken());
        startActivity(intent);
    }

    /**
     * 跳转到编辑资料
     */
    private void forwardEditProfile() {
        Intent intent = new Intent(mContext, EditProfileActivity.class);
        intent.putExtra("user", mUserBean);
        startActivity(intent);
    }

    /**
     * 跳转到设置
     */
    private void forwardCharge() {
        startActivity(new Intent(mContext, ChargeActivity.class));
    }

    /**
     * 跳转到设置
     */
    private void forwardSetting() {
        startActivity(new Intent(mContext, SettingActivity.class));
    }

    /**
     * 跳转到我的收益
     */
    private void forwardHarvest() {
        startActivity(new Intent(mContext, HarvestActivity.class));
    }

    @Override
    public void onDestroyView() {
        HttpUtil.cancel(HttpUtil.GET_BASE_INFO);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
