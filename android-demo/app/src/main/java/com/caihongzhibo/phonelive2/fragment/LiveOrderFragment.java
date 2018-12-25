package com.caihongzhibo.phonelive2.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.caihongzhibo.phonelive2.AppConfig;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.custom.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2017/9/26.
 */

public class LiveOrderFragment extends DialogFragment implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private int mType;// 1是dialog  2是fragment

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_order, null);
        Dialog dialog = new Dialog(mContext, R.style.dialog3);
        dialog.setContentView(mRootView);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setWindowAnimations(R.style.leftToRightAnim);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mRootView = inflater.inflate(R.layout.fragment_order, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        String touid = bundle.getString("touid");
        mType = bundle.getInt("type");
        ViewPagerIndicator indicator = (ViewPagerIndicator) mRootView.findViewById(R.id.indicator);
        indicator.setTitles(new String[]{mContext.getString(R.string.week_order), mContext.getString(R.string.all_order)});
        indicator.setChangeColor(false);
        indicator.setChangeSize(false);
        indicator.setVisibleChildCount(2);
        final List<WebView> list = new ArrayList<>();
        list.add(createWebView(AppConfig.HOST + "/index.php?g=appapi&m=Contribute&a=order&type=week&uid=" + touid));
        list.add(createWebView(AppConfig.HOST + "/index.php?g=appapi&m=Contribute&a=order&type=all&uid=" + touid));
        ViewPager viewPager = (ViewPager) mRootView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View v = list.get(position);
                container.addView(v);
                return v;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(list.get(position));
            }
        });
        indicator.setViewPager(viewPager);
        mRootView.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    private WebView createWebView(String url) {
        WebView webView = new WebView(mContext);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        return webView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if (mType == 1) {
                    dismiss();
                } else if (mType == 2) {
                    getActivity().onBackPressed();
                }
                break;
        }
    }
}
