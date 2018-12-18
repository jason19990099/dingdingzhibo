package com.caihongzhibo.phonelive.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.bean.ConfigBean;
import com.caihongzhibo.phonelive.bean.LiveBean;
import com.caihongzhibo.phonelive.bean.RewardBean;
import com.caihongzhibo.phonelive.custom.reward.LoginRewardWindow;
import com.caihongzhibo.phonelive.event.IgnoreUnReadEvent;
import com.caihongzhibo.phonelive.event.JIMLoginEvent;
import com.caihongzhibo.phonelive.event.JPushEvent;
import com.caihongzhibo.phonelive.fragment.ExitFragment;
import com.caihongzhibo.phonelive.fragment.HomeFragment;
import com.caihongzhibo.phonelive.fragment.HomeListFragment;
import com.caihongzhibo.phonelive.fragment.HomeNearFragment;
import com.caihongzhibo.phonelive.fragment.InviteFragment;
import com.caihongzhibo.phonelive.fragment.UserFragment;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.im.IM;
import com.caihongzhibo.phonelive.im.JIM;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.interfaces.MainEventListener;
import com.caihongzhibo.phonelive.presenter.CheckLivePresenter;
import com.caihongzhibo.phonelive.utils.DialogUitl;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.LocationUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;
import com.caihongzhibo.phonelive.utils.VersionUtil;
import com.caihongzhibo.phonelive.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.jpush.im.android.api.model.Message;

/**
 * Created by cxf on 2017/8/8.
 * 主页面
 */

public class MainActivity extends AbsActivity {

    public static final int HOME = 0;
    public static final int NEAR = 1;
    public static final int LIST = 2;
    public static final int USER = 3;
    private static final int REQUEST_LOCATION_PERMISSION = 100;//请求定位权限的请求码
    private static final int REQUEST_READ_PERMISSION = 101;//请求文件读写权限的请求码
    private RelativeLayout mRootViewGroup;
    private View mBtnMe;
    private FragmentManager mFragmentManager;
    private SparseArray<Fragment> mMap;
    private int mCurFragmentKey;
    private HomeFragment mHomeFragment;
    private HomeNearFragment mNearFragment;
    private HomeListFragment mListFragment;
    private UserFragment mUserFragment;
    private int mUnReadCount;//未读消息数量
    private CheckLivePresenter mCheckLivePresenter;
    private IM mIM;

    public static void startMainActivity(Context context, Bundle bundle){
        Intent intent=new Intent(context,MainActivity.class);
        intent.putExtra("jpusheventBundle",bundle);
        context.startActivity(intent);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void main() {
        mRootViewGroup = (RelativeLayout) findViewById(R.id.rootLayout);
        mBtnMe = findViewById(R.id.btn_me);
        mMap = new SparseArray<>();
        mHomeFragment = new HomeFragment();
        mNearFragment = new HomeNearFragment();
        mListFragment = new HomeListFragment();
        mUserFragment = new UserFragment();
        mMap.put(HOME, mHomeFragment);
        mMap.put(NEAR, mNearFragment);
        mMap.put(LIST, mListFragment);
        mMap.put(USER, mUserFragment);
        mCurFragmentKey = 0;
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        for (int i = 0; i < mMap.size(); i++) {
            Fragment f = mMap.valueAt(i);
            ft.add(R.id.replaced, f);
            if (mCurFragmentKey == mMap.keyAt(i)) {
                ft.show(f);
            } else {
                ft.hide(f);
            }
        }
        ft.commit();

        mIM = new JIM();

        EventBus.getDefault().register(this);
        startLocation();
        getConfig();
        getLoginReward();
        AppConfig.getInstance().refreshUserInfo();
        String isReg = getIntent().getStringExtra("isreg");
        if ("1".equals(isReg)) {
            showInviteDialog();
        }
        initJPushMsg();
    }

    public void mainClick(View v) {
        switch (v.getId()) {
            case R.id.btn_home://主页
                toggleFragment(HOME);
                break;
            case R.id.btn_near://附近
                toggleFragment(NEAR);
                break;
            case R.id.btn_list://排行榜
                toggleFragment(LIST);
                break;
            case R.id.btn_me://个人页
                toggleFragment(USER);
                break;
            case R.id.live_btn://开始直播
                startLive();
                break;
            case R.id.btn_search://搜索
                startActivity(new Intent(mContext, SearchActivity.class));
                break;
            case R.id.btn_message://私信
                startActivity(new Intent(mContext, EMChatActivity.class));
                break;

        }
    }

    /**
     * 切换fragment
     */
    private void toggleFragment(int key) {
        try {
            if (mCurFragmentKey == key) {
                return;
            }
            mCurFragmentKey = key;
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            for (int i = 0; i < mMap.size(); i++) {
                Fragment f = mMap.valueAt(i);
                if (mCurFragmentKey == mMap.keyAt(i)) {
                    ft.show(f);
                } else {
                    ft.hide(f);
                }
            }
            ft.commit();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUnReadCount();
    }

    public void onChildResume(int key) {
        if (key == mCurFragmentKey) {
            ((MainEventListener) mMap.get(mCurFragmentKey)).loadData();
        }
    }


    private void getConfig() {
        HttpUtil.getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean bean) {
                checkVersion();
                checkMaintain();
            }
        });
    }


    private void checkVersion() {
        VersionUtil.checkVersion(AppConfig.getInstance().getConfig(), mContext, null);
    }

    /**
     * 是否维护
     */
    private void checkMaintain() {
        ConfigBean bean = AppConfig.getInstance().getConfig();
        if ("1".equals(bean.getMaintain_switch())) {
            DialogUitl.messageDialog(mContext, getString(R.string.maintain_tip), bean.getMaintain_tips(), null).show();
        }
    }

    /**
     * 显示邀请码信息弹窗
     */
    private void showInviteDialog() {
        InviteFragment fragment = new InviteFragment();
        Bundle bundle = getIntent().getExtras();
        fragment.setArguments(bundle);
        fragment.show(mFragmentManager, "InviteFragment");
    }


    /**
     * 显示未读消息数量
     */
    private void showUnReadCount() {
        try {
            mUnReadCount = mIM.getAllUnReadCount();
            L.e("IM", "未读消息数量---->" + mUnReadCount);
            mHomeFragment.setUnReadCount(mUnReadCount);
            mNearFragment.setUnReadCount(mUnReadCount);
        } catch (Exception e) {
            L.e("showUnReadCount--->" + e.getClass() + "--->" + e.getMessage());
            DialogUitl.messageDialog(mContext, WordUtil.getString(R.string.tip), WordUtil.getString(R.string.temp_var_clear) + WordUtil.getString(R.string.app_name), new DialogUitl.Callback2() {
                @Override
                public void confirm(Dialog dialog) {
                    dialog.dismiss();
                    Intent intent = new Intent(mContext, LauncherActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ignoreUnRead(IgnoreUnReadEvent e) {
        mUnReadCount = 0;
        mHomeFragment.setUnReadCount(mUnReadCount);
        mNearFragment.setUnReadCount(mUnReadCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(Message message) {
        mUnReadCount++;
        mHomeFragment.setUnReadCount(mUnReadCount);
        mNearFragment.setUnReadCount(mUnReadCount);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJIMLoginEvent(JIMLoginEvent e) {
        //极光IM登录成功
        showUnReadCount();
    }

    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.GET_CONFIG);
        HttpUtil.cancel(HttpUtil.CHECK_LIVE);
        HttpUtil.cancel(HttpUtil.ROOM_CHARGE);
        HttpUtil.cancel(HttpUtil.GET_BONUS);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        AppConfig.getInstance().setLaunched(false);
        AppConfig.getInstance().setConfig(null);
        LocationUtil.getInstance().stopLocation();
    }

    /**
     * 主播 开启直播
     */
    private void startLive() {
        Intent intent = new Intent(mContext, LiveReadyActivity.class);
        startActivity(intent);
    }

    /**
     * 观众 观看直播
     */
    public void watchLive(LiveBean bean) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
            } else {
                forwardLiveActivity(bean);
            }
        } else {
            forwardLiveActivity(bean);
        }
    }

    private void forwardLiveActivity(LiveBean bean) {
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new CheckLivePresenter(mContext);
        }
        mCheckLivePresenter.setSelectLiveBean(bean);
        mCheckLivePresenter.watchLive();
    }

    /**
     * 开启定位
     */
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                LocationUtil.getInstance().startLocation();
            }
        } else {
            LocationUtil.getInstance().startLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults==null||grantResults.length==0)
        {
            ToastUtil.show(getString(R.string.permission_refused));
            return;
        }
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationUtil.getInstance().startLocation();
                } else {
                    ToastUtil.show(getString(R.string.location_permission_refused));
                }
                break;
            case REQUEST_READ_PERMISSION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ToastUtil.show(getString(R.string.storage_permission_refused));
                }
                break;
        }
    }

    /**
     * 获取登录奖励
     */
    private void getLoginReward() {
        HttpUtil.getBonus(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    int bonusDay = obj.getIntValue("bonus_day");
                    if (bonusDay > 0) {
                        List<RewardBean> list = JSON.parseArray(obj.getString("bonus_list"), RewardBean.class);
                        LoginRewardWindow window = new LoginRewardWindow(mContext, mRootViewGroup);
                        window.show(list, bonusDay - 1, new LoginRewardWindow.Target() {
                            @Override
                            public int[] getPosition() {
                                int[] p = new int[2];
                                mBtnMe.getLocationOnScreen(p);
                                return p;
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        ExitFragment fragment = new ExitFragment();
        fragment.show(mFragmentManager, "ExitFragment");
    }

    /**
     * 加载推送消息是否进入直播间
     */
    private void initJPushMsg(){
        if(getIntent().getBundleExtra("jpusheventBundle")!=null){
            LiveBean jPushEvent=getIntent().getBundleExtra("jpusheventBundle").getParcelable("jpushevent");
            if(jPushEvent!=null){//判断是否是推送进入房间
                CheckLivePresenter mCheckLivePresenter = new CheckLivePresenter(mContext);
                mCheckLivePresenter.setSelectLiveBean(jPushEvent);
                mCheckLivePresenter.watchLive();
            }
        }
    }
}
