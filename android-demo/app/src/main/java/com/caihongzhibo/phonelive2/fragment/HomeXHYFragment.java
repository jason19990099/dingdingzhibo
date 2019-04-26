package com.caihongzhibo.phonelive2.fragment;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.caihongzhibo.phonelive2.R;
import com.caihongzhibo.phonelive2.bean.XhyBean;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HomeXHYFragment extends AbsFragment  {
    private WebView webview ;
    // h5 地址
    private String reurl = "";
    // 用来显示视频的布局
    private FrameLayout mLayout;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_xhy;
    }

    @Override
    protected void main() {
        initView();
    }

    private void initView() {
        mLayout = (FrameLayout) mRootView.findViewById(R.id.fl_video);
        webview = (WebView) mRootView.findViewById(R.id.webview);
        initWebView();
        getIparrist();
    }


    private  void getIparrist() {
        String url = "http://dd7666.com/api/public/index.php?service=Home.getXhyLink";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("123", "onFailure: ");
            }
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String s=response.body().string();
                XhyBean XhyBean=new Gson().fromJson(s,XhyBean.class);
                reurl=XhyBean.getData().getApi();
                Log.e("===reurl==",""+reurl);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webview.loadUrl(reurl);
                    }
                });

            }
        });

    }

    /**
     * 设置webView 相关属性
     */

    private void initWebView() {
        WebSettings setting= webview.getSettings();
        setting.setJavaScriptEnabled(true);// 设置支持javascript脚本
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE);//设置缓存模式
        webview.setVerticalScrollBarEnabled(false); // 取消Vertical ScrollBar显示
        webview.setHorizontalScrollBarEnabled(false); // 取消Horizontal ScrollBar显示
        //设置自适应屏幕，两者合用
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);

        setting.setAllowFileAccess(true);// 允许访问文件
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.setFocusable(false); // 去掉超链接的外边框
        setting.setMediaPlaybackRequiresUserGesture(false);
        mLayout.setVisibility(View.VISIBLE);
        webview.setVisibility(View.VISIBLE);

        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        webview.destroy();
        webview = null;
    }


}
