package com.caihongzhibo.phonelive.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by cxf on 2017/10/10.
 * 把long转成时间字符串，环信聊天用到
 */

public class DateUtil {

    private static Calendar sCalendar;
    private static SimpleDateFormat sSdf;

    static {
        sCalendar = Calendar.getInstance();
        sSdf = new SimpleDateFormat("MM-dd HH:mm");
    }

    public static String getDateString(long time) {
        sCalendar.setTimeInMillis(time);
        return sSdf.format(sCalendar.getTime());
    }
}
