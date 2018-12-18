package com.caihongzhibo.phonelive.interfaces;

/**
 * Created by cxf on 2017/9/10.
 */

public interface DownloadCallback {

    void onSuccess();

    void onProgress(int progress);

    void onError();
}
