package com.caihongzhibo.phonelive.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.adapter.SharedSdkAdapter;
import com.caihongzhibo.phonelive.bean.ConfigBean;
import com.caihongzhibo.phonelive.bean.LiveBean;
import com.caihongzhibo.phonelive.bean.SharedSdkBean;
import com.caihongzhibo.phonelive.interfaces.OnItemClickListener;
import com.caihongzhibo.phonelive.utils.SharedSdkUitl;

/**
 * Created by cxf on 2017/9/28.
 */

public class LiveShareFragment extends DialogFragment implements OnItemClickListener<SharedSdkBean> {

    private Context mContext;
    private View mRootView;
    private ConfigBean mConfigBean;
    private LiveBean mLiveBean;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = LayoutInflater.from(AppContext.sInstance).inflate(R.layout.fragment_share, null);
        Dialog dialog = new Dialog(mContext, R.style.dialog2);
        dialog.setContentView(mRootView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        window.setWindowAnimations(R.style.bottomToTopAnim);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mLiveBean==null){
            mLiveBean=getArguments().getParcelable("live");
        }
        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mConfigBean = AppConfig.getInstance().getConfig();
        SharedSdkAdapter adapter = new SharedSdkAdapter(mConfigBean.getShare_type(), true, false);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(SharedSdkBean item, int position) {
        String url= mConfigBean.getApp_android();
        if(item.getType().equals(SharedSdkBean.WX)||item.getType().equals(SharedSdkBean.WX_PYQ)){
            url=mConfigBean.getWx_siteurl()+mLiveBean.getUid();
        }
        SharedSdkUitl.getInstance().share(item.getType(),
                mConfigBean.getShare_title(),
                mLiveBean.getUser_nicename() + mConfigBean.getShare_des(),
                mLiveBean.getAvatar(),url, null);
    }


}
