package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        if (null != context && null != context.getResources() && null != context.getResources().getDisplayMetrics()) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
        return 0;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        if (null != context && context.getResources() != null && context.getResources().getDisplayMetrics() != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }

        return (int) pxValue;
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
     */
    public static int sp2px(Context context, float spValue) {
        if (null != context && null != context.getResources() && null != context.getResources().getDisplayMetrics()) {
            final float scale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * scale + 0.5f);
        }
        return 0;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取手机密度
     *
     * @return
     */
    public static float getDensity(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        double width = metric.widthPixels; // 屏幕宽度（像素）
        double height = metric.heightPixels; // 屏幕高度（像素）
        float density = metric.density; // 屏幕密度
        return density;
    }

    /**
     * 获取手机宽度
     *
     * @return
     */
    public static double getMetricsWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);
        double width = metric.widthPixels; // 屏幕宽度（像素）
//        double height = metric.heightPixels; // 屏幕高度（像素）
//        float density = metric.density; // 屏幕密度
        return width;
    }
}
