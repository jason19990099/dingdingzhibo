package com.caihongzhibo.phonelive.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.fragment.EMChatFragment;
import com.caihongzhibo.phonelive.im.EMIM;
import com.caihongzhibo.phonelive.im.IM;
import com.caihongzhibo.phonelive.im.JIM;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by cxf on 2017/8/10.
 */

public class EMChatActivity extends AbsActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_fragment;
    }

    @Override
    protected void main() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        EMChatFragment fragment = new EMChatFragment();
        fragment.setIM(new JIM());
        Bundle bundle = new Bundle();
        bundle.putInt("from", 0);
        fragment.setArguments(bundle);
        ft.replace(R.id.replaced, fragment);
        ft.commit();
    }
}
