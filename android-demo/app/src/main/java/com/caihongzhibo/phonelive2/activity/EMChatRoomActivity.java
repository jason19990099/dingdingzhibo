package com.caihongzhibo.phonelive2.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.fragment.EMChatRoomFragment;
import com.caihongzhibo.phonelive2.im.JIM;

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
