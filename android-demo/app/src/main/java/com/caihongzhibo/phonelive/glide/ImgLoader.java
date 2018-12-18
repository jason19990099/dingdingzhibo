package com.caihongzhibo.phonelive.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.caihongzhibo.phonelive.AppContext;
import com.caihongzhibo.phonelive.interfaces.CommonCallback;
import com.caihongzhibo.phonelive.utils.DpUtil;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by cxf on 2017/8/9.
 */

public class ImgLoader {
    private static RequestManager sManager;
    private static CircleImageTransformation sTransformation1;
    private static CircleImageTransformation sTransformation2;
    private static CircleImageTransformation sTransformation3;
    private static BlurTransformation sTransformation4;

    static {
        sManager = Glide.with(AppContext.sInstance);
        sTransformation1 = new CircleImageTransformation(AppContext.sInstance);
        sTransformation2 = new CircleImageTransformation(AppContext.sInstance, 0xffffffff, DpUtil.dp2px(1));
        sTransformation3 = new CircleImageTransformation(AppContext.sInstance, 0xffffd350, DpUtil.dp2px(1));
        sTransformation4 = new BlurTransformation(AppContext.sInstance, 20);
    }

    //正常显示图片
    public static void display(String url, ImageView imageView) {
        sManager.load(url).into(imageView);
    }

    //正常显示图片,不带动画
    public static void displayNoAnimate(String url, ImageView imageView) {
        sManager.load(url).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).skipMemoryCache(false).into(imageView);
    }


    //正常显示图片
    public static void display(File file, ImageView imageView) {
        sManager.load(file).into(imageView);
    }


    //加载原图，即没有压缩 裁剪 变换之前的图
    public static void displaySource(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }

    //正常显示图片，带有占位图
    public static void display(String url, ImageView imageView, int placeholderRes) {
        sManager.load(url).placeholder(placeholderRes).into(imageView);
    }

    //加载圆形头像
    public static void displayCircle(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(sTransformation1)
                .into(imageView);
    }

    //加载圆形头像
    public static void displayCircle(File file, ImageView imageView) {
        sManager.load(file)
                .bitmapTransform(sTransformation1)
                .into(imageView);
    }

    //加载圆形头像,带白色 1dp边框的
    public static void displayCircleWhiteBorder(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(sTransformation2)
                .into(imageView);
    }

    //加载圆形头像,带橙色 1dp边框的
    public static void displayCircleOrangeBorder(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(sTransformation3)
                .into(imageView);
    }


    //显示模糊的毛玻璃图片
    public static void displayBlur(String url, ImageView imageView) {
        sManager.load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .bitmapTransform(sTransformation4)
                .into(imageView);
    }

    public static void displayBitmap(String url, final BitmapCallback bitmapCallback) {
        sManager.load(url).asBitmap().skipMemoryCache(true).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                if (bitmapCallback != null) {
                    bitmapCallback.callback(bitmap);
                }
            }
        });
    }

    public interface BitmapCallback {
        void callback(Bitmap bitmap);
    }
}
