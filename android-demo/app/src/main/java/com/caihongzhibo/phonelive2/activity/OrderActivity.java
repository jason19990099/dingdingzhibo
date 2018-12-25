package com.caihongzhibo.phonelive2.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.fragment.LiveOrderFragment;

/**
 * Created by cxf on 2017/8/12.
 * 映票排行榜
 */

public class OrderActivity extends AbsActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_fragment;
    }

    @Override
    protected void main() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        LiveOrderFragment fragment = new LiveOrderFragment();
        fragment.setArguments(getIntent().getExtras());
        ft.replace(R.id.replaced, fragment);
        ft.commit();
    }
}
