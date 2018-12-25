package com.caihongzhibo.phonelive2.fragment;

import android.view.View;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.game.GameEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by cxf on 2017/10/9.
 */

public class LiveAnchorBottomFragment extends LiveBottomFragment {

    private View GameBtn;//关闭游戏的按钮

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bottom_anchor;
    }

    @Override
    protected void main() {
        super.main();
        GameBtn = mRootView.findViewById(R.id.btn_close_game);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameEvent(GameEvent e) {
        switch (e.getStatus()) {
            case GameEvent.OPEN:
                if (GameBtn.getVisibility() == View.GONE) {
                    GameBtn.setVisibility(View.VISIBLE);
                }
                break;
            case GameEvent.CLOSE:
                if (GameBtn.getVisibility() == View.VISIBLE) {
                    GameBtn.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
