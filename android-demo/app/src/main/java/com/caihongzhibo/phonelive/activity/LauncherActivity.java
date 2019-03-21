package com.caihongzhibo.phonelive.activity;

import android.util.Log;
import com.caihongzhibo.phonelive.bean.IParray;
import com.google.gson.Gson;
import com.lzy.okgo.model.Response;
import com.caihongzhibo.phonelive.AppConfig;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.R;
import com.caihongzhibo.phonelive.http.HttpCallback;
import com.caihongzhibo.phonelive.http.HttpUtil;
import com.caihongzhibo.phonelive.http.JsonBean;
import com.caihongzhibo.phonelive.im.IMUtil;
import com.caihongzhibo.phonelive.utils.IntervalCountDown;
import com.caihongzhibo.phonelive.utils.JPushUtil;
import com.caihongzhibo.phonelive.utils.L;
import com.caihongzhibo.phonelive.utils.LoginUtil;
import com.caihongzhibo.phonelive.utils.SharedPreferencesUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cn.sharesdk.framework.ShareSDK;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 启动页面
 */
public class LauncherActivity extends AbsActivity {
    List<String> list= new ArrayList<>();
    IParray  iParray;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    protected void main() {
        //获取ip集合
        getIparrist();

    }

    private void getIparrist() {
        String url = "http://api.dd7666.com/index.php?r=index/getapi";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("123", "onFailure: ");
            }
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
              String s=response.body().string();
              iParray=new Gson().fromJson(s,IParray.class);

                for (int i=0;i<iParray.getData().size();i++){
                    list.add(iParray.getData().get(i)+"/api/public/index.php?service=Home.apiTest");
                }
                for (int m=0;m<list.size();m++){
                    sendHttpRequest(list.get(m),m);
                }
            }


        });

        }


    public SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//设置日期格式
    String time_string = "";
    //请求网址响应
    public String sendHttpRequest(final String address, final int i) {

        new Thread(new Runnable() {
            long between = 0;
            String date2 = "";
            public void run() {

                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(9999);
                    connection.setReadTimeout(9999);
                    final String date1 = dfs.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        date2 = dfs.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
                        Date begin = dfs.parse(date1);
                        Date end = dfs.parse(date2);
                        between = Math.abs((end.getTime() - begin.getTime()));// 得到两者的毫秒数
                        Log.e("between",between+"");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AppConfig.HOST=iParray.getData().get(i);
                                //开启倒计时
                                startCountDown();
                                //初始化http
                                HttpUtil.init();
                                //初始化极光推送
                                JPushUtil.init();
                                //初始化sharedSdk
                                ShareSDK.initSDK(AppContext.sInstance);
                                //初始化IM
                                IMUtil.getInstance().init(IMUtil.JIM);
                                AppConfig.getInstance().setLaunched(true);
                            }
                        });




                    } else {
                        Log.e("between","失敗了。。。。。。");
                    }

                } catch (Exception e) {
                    Log.e("between","發生錯誤了。。。。。。"+e.toString());
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }

                }
            }
        }).start();
        return time_string;
    }




    /**
     * 启动定时器，3秒后跳转
     */
    private void startCountDown() {
        final int targetCount = 2;
        new IntervalCountDown(targetCount, new IntervalCountDown.Callback() {
            @Override
            public void callback(int count) {
                L.e("LauncherActivity 定时器-->" + count);
                if (count == targetCount) {
                    readUidAndToken();
                }
            }
        }).start();
    }

    /**
     * 从SharedPreferences中读取用户uid和token，
     * 如果有，验证uid和token
     * 如果没有，则跳转到登录页面
     */
    private void readUidAndToken() {
        String[] uidAndToken = SharedPreferencesUtil.getInstance().readUidAndToken();
        if (uidAndToken != null) {
            validateUidAndToken(uidAndToken[0], uidAndToken[1]);
        } else {
            L.e("不存在用户信息-->跳转到登录页面");
            LoginUtil.forwardLogin();
        }
    }

    /**
     * 验证uid和token是否过期
     */
    private void validateUidAndToken(final String uid, final String token) {
        HttpUtil.ifToken(uid, token, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    //token没有过期
                    AppConfig.getInstance().setUid(uid);
                    AppConfig.getInstance().setToken(token);
                    LoginUtil.startThridLibray();//启动三方库 IM 极光等
                    MainActivity.startMainActivity(LauncherActivity.this, LauncherActivity.this.getIntent().getBundleExtra("jpusheventBundle"));
                    finish();
                }
            }

            @Override
            public void onError(Response<JsonBean> response) {
                super.onError(response);
                AppConfig.getInstance().setUid(uid);
                AppConfig.getInstance().setToken(token);
                MainActivity.startMainActivity(LauncherActivity.this, LauncherActivity.this.getIntent().getBundleExtra("jpusheventBundle"));
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        HttpUtil.cancel(HttpUtil.IF_TOKEN);
        super.onDestroy();

    }
}
