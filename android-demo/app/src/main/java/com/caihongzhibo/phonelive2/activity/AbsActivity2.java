package com.caihongzhibo.phonelive2.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.event.JPushEvent;
import com.caihongzhibo.phonelive2.presenter.CheckLivePresenter;
import com.caihongzhibo.phonelive2.utils.FixFocusedViewLeakUtil;
import com.caihongzhibo.phonelive2.utils.L;
import com.caihongzhibo.phonelive2.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2017/8/3.
 */

public abstract class AbsActivity2 extends AppCompatActivity {

    protected static final int ACTIVITYTYPE_ORTHER = 0;
    protected static final int ACTIVITYTYPE_ANCHOR = 1;
    protected static final int ACTIVITYTYPE_AUDIENCE = 2;

    protected Context mContext;
    private boolean isRegistered;
    protected int mActivityType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mContext = this;
        main();
        isRegistered = EventBus.getDefault().isRegistered(this);
        if (!isRegistered) {
            EventBus.getDefault().register(this);
        }
    }

    protected abstract int getLayoutId();

    protected abstract void main();

    protected void setTitle(String title) {
        TextView titleView = (TextView) findViewById(R.id.title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    public void back(View view) {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FixFocusedViewLeakUtil.fixFocusedViewLeak();
        if (!isRegistered) {
            EventBus.getDefault().unregister(this);
        }
        //AppContext.sRefWatcher.watch(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void jpushEvent(JPushEvent e) {
        if(!isTopActivity(this)){
            return;
        }
        switch (mActivityType) {
            case ACTIVITYTYPE_ORTHER:
                CheckLivePresenter mCheckLivePresenter = new CheckLivePresenter(mContext);
                mCheckLivePresenter.setSelectLiveBean(e);
                mCheckLivePresenter.watchLive();
                break;
            case ACTIVITYTYPE_ANCHOR:
                ToastUtil.show(getString(R.string.live_tips));
                break;
            case ACTIVITYTYPE_AUDIENCE:

                break;

        }
        L.e(">>>>收到推送----" + e.toString());
    }

    private boolean isTopActivity(Activity activity)
    {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        L.e(">>>>>>>>最上层的activity-----"+cn.getClassName()+"---"+activity.getLocalClassName());
        return cn.getClassName().contains(activity.getLocalClassName());
    }
}
