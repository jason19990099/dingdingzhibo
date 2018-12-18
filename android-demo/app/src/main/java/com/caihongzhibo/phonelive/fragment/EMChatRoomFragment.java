package com.caihongzhibo.phonelive.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.activity.LiveActivity;
import com.caihongzhibo.phonelive.adapter.EMChatRoomAdapter;
import com.caihongzhibo.phonelive.bean.UserBean;
import com.caihongzhibo.phonelive.custom.MyLinearLayoutManger;
import com.caihongzhibo.phonelive.custom.NoAlphaItemAnimator;
import com.caihongzhibo.phonelive.event.EMChatExitEvent;
import com.caihongzhibo.phonelive.event.VisibleHeightEvent;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.im.JIM;
import com.caihongzhibo.phonelive.utils.DateUtil;
import com.caihongzhibo.phonelive.utils.DpUtil;
import com.caihongzhibo.phonelive.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;


/**
 * Created by cxf on 2017/8/10.
 */

public class EMChatRoomFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private TextView mTitleView;
    private EditText mEditText;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private int mFrom;//0 是直播间外打开聊天页面 1是直播间内打开聊天页面
    private boolean mFromPop;//是否是用弹窗打开的
    private UserBean mUser; //自己的个人信息
    private UserBean mToUser; //对方的个人信息
    private int mIsAttention;//1  已关注 0 未关注
    private EMChatRoomAdapter<Conversation, Message, JIM> mAdapter;
    private List<Message> mList;
    private long mLastTime = 0;
    private String mCurMsg = "";
    private int mOriginHeight;//原始高度
    private int mCurHeight;//当前高度
    private JIM mIM;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_chat_room, null, false);
        dialog.setContentView(mRootView);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        mOriginHeight = DpUtil.dp2px(300);
        mCurHeight = mOriginHeight;
        params.height = mCurHeight;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = inflater.inflate(R.layout.fragment_chat_room, container, false);
        return mRootView;
    }

    public void setIM(JIM im) {
        mIM = im;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        EventBus.getDefault().register(this);
    }

    public void initView() {
        mTitleView = (TextView) mRootView.findViewById(R.id.title);
        mEditText = (EditText) mRootView.findViewById(R.id.input);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recylcerView);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressbar);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new MyLinearLayoutManger(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
        mRootView.findViewById(R.id.btn_send).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    public void initData() {
        mUser = AppConfig.getInstance().getUserBean();
        Bundle bundle = getArguments();
        mFrom = bundle.getInt("from");
        if (mFrom == 1) {
            ((LiveActivity) mContext).addLayoutListener();
            ((LiveActivity) mContext).setChatRoomFragmentShowed(true);
        }
        mToUser = bundle.getParcelable("touser");
        mIsAttention = bundle.getInt("isAttention");
        mFromPop = bundle.getBoolean("fromPop", false);
        mTitleView.setText(mToUser.getUser_nicename());
        Conversation conversation = mIM.getConversation(mToUser.getId());
        mProgressBar.setVisibility(View.GONE);
        if (conversation == null) {
            return;
        }
        List<Message> currentList = mIM.getAllMessages(conversation);
        int size = currentList.size();
        if (size < 20) {
            mList = mIM.loadHistoryMessage(conversation, 20 - size);
            mList.addAll(currentList);
        } else {
            mList = currentList.subList(size - 20, size);
        }
        mAdapter = new EMChatRoomAdapter<Conversation, Message, JIM>(mContext, mList, mToUser, mUser, mIM);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.smoothScrollToPosition(mList.size() - 1);

    }

    /**
     * 当可视区高度变化时执行
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVisibleHeightChanged(VisibleHeightEvent event) {
        int visibleHeight = event.getVisibleHeight();
        if (mCurHeight > visibleHeight) {
            resize(visibleHeight);
        } else if (mCurHeight < visibleHeight && mCurHeight != mOriginHeight) {
            resize(mOriginHeight);
        }
    }

    private void resize(int targetHeight) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            mCurHeight = targetHeight;
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.height = mCurHeight;
            window.setAttributes(params);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendMessage();
                break;
            case R.id.btn_back:
                if (mFrom == 0) {
                    getActivity().onBackPressed();
                } else {
                    dismiss();
                }
                break;
        }
    }


    //发送私信
    private void sendMessage() {
        if (!AppConfig.getInstance().isIMLogined()) {
            ToastUtil.show("聊天服务器未接入");
            return;
        }
        //判断是否操作频繁
        if ((System.currentTimeMillis() - mLastTime) < 1500 && mLastTime != 0) {
            ToastUtil.show(getString(R.string.option_too_much));
            return;
        }
        mLastTime = System.currentTimeMillis();
        mCurMsg = mEditText.getText().toString();
        if ("".equals(mCurMsg)) {
            ToastUtil.show(getString(R.string.content_empty));
            return;
        }
        HttpUtil.checkBlack(mToUser.getId(), mCallback);
    }

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0) {
                if(info.length>0){
                    int t2u = JSON.parseObject(info[0]).getIntValue("t2u");
                    if (1 == t2u) {
                        ToastUtil.show(getString(R.string.you_are_blacked));
                    } else {
                        Message message = mIM.sendMessage(mCurMsg, mToUser.getId());
                        mEditText.setText("");
                        insertItem(message);
                    }
                }
            } else {
                ToastUtil.show(msg);
            }
        }

        @Override
        public void onStart() {
            if (mProgressBar.getVisibility() == View.GONE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFinish() {
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    private void insertItem(Message message) {
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(message);
        int position = mList.size() - 1;
        if (mAdapter == null) {
            mAdapter = new EMChatRoomAdapter<Conversation, Message, JIM>(mContext, mList, mToUser, mUser, mIM);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyItemInserted(position);
        }
        mRecyclerView.smoothScrollToPosition(position);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(Message message) {
        if (mIM.getFrom(message).equals(mToUser.getId())) {
            insertItem(message);
        }
    }

    @Override
    public void onDestroyView() {
        HttpUtil.cancel(HttpUtil.CHECK_BLACK);
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFrom == 1) {
            ((LiveActivity) mContext).removeLayoutListener();
            ((LiveActivity) mContext).setChatRoomFragmentShowed(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String touid = mToUser.getId();
        Conversation conversation = mIM.getConversation(touid);
        if (conversation != null) {
            mIM.markAllMessagesAsRead(conversation);
        }
        if (mFromPop) {
            ((LiveActivity) mContext).showUnReadCount();
        } else {
            onBack(touid);
        }
    }

    private void onBack(String touid) {
        if (mList != null && mList.size() > 0) {
            Message message = mList.get(mList.size() - 1);
            String lastMsg = mIM.getContent(message);
            String lastTime = DateUtil.getDateString(mIM.getMessageTime(message));
            EMChatExitEvent e = new EMChatExitEvent(lastMsg, lastTime, touid, mIsAttention);
            EventBus.getDefault().post(e);
        }
    }

}
