package com.caihongzhibo.phonelive.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caihongzhibo.phonelive.AppContext;

/**
 * Created by cxf on 2017/8/8.
 */

public abstract class AbsFragment extends Fragment {

    protected View mRootView;
    protected Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = inflater.inflate(getLayoutId(), container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        main();
    }

    protected abstract int getLayoutId();

    protected abstract void main();

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AppContext.sRefWatcher.watch(this);
    }
}
