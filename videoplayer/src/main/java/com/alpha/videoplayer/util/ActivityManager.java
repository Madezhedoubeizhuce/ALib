package com.alpha.videoplayer.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    private static final String TAG = "ActivityManager";

    private static float sNoncompatDensity;
    private static float sNoncompatScaledDensity;
    private static boolean firstAdjust = false;
    public static float scale = 1.0f;

    private ActivityManager() {
    }

    private static ActivityManager instance = new ActivityManager();
    private static List<WeakReference<Activity>> activityStack = new ArrayList<>();

    public static ActivityManager getInstance() {
        return instance;
    }

    public void addActivity(Activity aty) {
        activityStack.add(new WeakReference<>(aty));
    }

    public void removeActivity(Activity aty) {
        for (WeakReference<Activity> temp : activityStack) {
            if (null != temp.get() && temp.get() == aty) {
                activityStack.remove(temp);
                break;
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            WeakReference<Activity> reference = activityStack.get(i);
            if (null != reference && null != reference.get()) {
                reference.get().finish();
            }
        }
        activityStack.clear();
    }

    /**
     * 调整density值，进行屏幕适配，详细原理见此文章https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA
     *
     * @param activity
     * @param application
     */
    public void setCustomDensity(Activity activity, final Application application) {
        final DisplayMetrics appDispalyMetrics = application.getResources().getDisplayMetrics();

        Log.d(TAG, "setCustomDensity: width: " + appDispalyMetrics.widthPixels + ", height: " + appDispalyMetrics.heightPixels);
        Log.d(TAG, "setCustomDensity: ori densityDpi: " + appDispalyMetrics.densityDpi);
        Log.d(TAG, "setCustomDensity: ori density: " + appDispalyMetrics.density);

        // 监听字体变化，避免在系统设置中切换字体，再返回应用，字体并没有变化的问题
        if (sNoncompatDensity == 0) {
            sNoncompatDensity = appDispalyMetrics.density;
            sNoncompatScaledDensity = appDispalyMetrics.scaledDensity;

            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sNoncompatScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
        }

        // px = density * dp;
        //density = dpi / 160;
        //px = dp * (dpi / 160) = dp * density;
        // 以分辨率1920px * 1080px来设计，以density为3来标注，也就是屏幕其实是640dp * 360dp
        // 以宽适配，即以宽为360dp进行适配，根据px = dp * density，要确保dp相同，在px变化时，density要同步修改，故进行如下修改
        final float targetDensity = appDispalyMetrics.widthPixels / 360f;
        final int targetDensityDpi = (int) (160 * targetDensity);
        final float targetScaleDensity = targetDensity * (sNoncompatScaledDensity / sNoncompatDensity);

        if (!firstAdjust) {
            firstAdjust = true;
            scale = appDispalyMetrics.density / targetDensity;
            Log.d(TAG, "setCustomDensity: scale " + scale);
        }

        appDispalyMetrics.density = targetDensity;
        appDispalyMetrics.scaledDensity = targetScaleDensity;
        appDispalyMetrics.densityDpi = targetDensityDpi;

        final DisplayMetrics activityDiaplayMetrics = activity.getResources().getDisplayMetrics();
        activityDiaplayMetrics.density = targetDensity;
        activityDiaplayMetrics.scaledDensity = targetScaleDensity;
        activityDiaplayMetrics.densityDpi = targetDensityDpi;

        Log.d(TAG, "setCustomDensity: adjust densityDpi: " + appDispalyMetrics.densityDpi);
        Log.d(TAG, "setCustomDensity: adjust density: " + appDispalyMetrics.density);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     *
     * @param activity
     */
    private int ori;

    public void hideNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            ori = decorView.getSystemUiVisibility();
            Log.d(TAG, "hideNavigationBar: ori visibility " + decorView.getSystemUiVisibility());
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            Log.d(TAG, "hideNavigationBar: uiOption " + Integer.toBinaryString(uiOptions));
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void restoreActivity(Activity activity) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = activity.getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = activity.getWindow().getDecorView();
            Log.d(TAG, "hideNavigationBar: ori uiOption " + Integer.toHexString(ori));

            decorView.setSystemUiVisibility(ori);
        }
    }
}
