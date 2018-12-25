package com.caihongzhibo.phonelive2.fragment;

import android.view.View;
import android.widget.ImageView;

import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;

/**
 * Created by cxf on 2017/10/9.
 */

public class LiveAudienceBottomFragment extends LiveBottomFragment {

    ImageView mBtnLinkMic;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bottom_audience;
    }

    @Override
    protected void main() {
        super.main();
        if (AppConfig.getInstance().getConfig().getShare_type().length == 0) {
            mRootView.findViewById(R.id.btn_share).setVisibility(View.GONE);
        }
        mBtnLinkMic = (ImageView) mRootView.findViewById(R.id.btn_lianmai);
    }

    public void setLinkIcon(boolean isLinkMic) {
        if (isLinkMic) {
            mBtnLinkMic.setImageResource(R.mipmap.icon_live_duanmai);
        } else {
            mBtnLinkMic.setImageResource(R.mipmap.icon_live_lianmai);
        }
    }
}
