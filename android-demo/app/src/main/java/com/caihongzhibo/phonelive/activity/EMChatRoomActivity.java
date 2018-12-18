package com.caihongzhibo.phonelive.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.fragment.EMChatRoomFragment;
import com.caihongzhibo.phonelive.im.JIM;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by cxf on 2017/8/14.
 * 环信聊天页面
 */

public class EMChatRoomActivity extends AbsActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_fragment;
    }

    @Override
    protected void main() {
        EMChatRoomFragment fragment = new EMChatRoomFragment();
        fragment.setIM(new JIM());
        fragment.setArguments(getIntent().getExtras());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.replaced, fragment);
        ft.commit();
    }

}
