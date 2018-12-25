package com.caihongzhibo.phonelive2.fragment;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.glide.ImgLoader;

/**
 * Created by cxf on 2017/8/31.
 */

public class LiveAudienceEndFragment extends AbsFragment implements View.OnClickListener {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live_audience_end;
    }

    @Override
    protected void main() {
        ImageView bg = (ImageView) mRootView.findViewById(R.id.bg);
        String anchorAvatar = getArguments().getString("anchorAvatar");
        ImgLoader.displayBlur(anchorAvatar, bg);
        mRootView.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                ((Activity) mContext).finish();
                break;
        }
    }
}
