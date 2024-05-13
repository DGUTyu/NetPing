package com.example.net.util;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.util.UUID;


@SuppressLint("NewApi")
public class AppHelper {
    private static final String TAG = "AppHelper";

    public static boolean isSdcardExist() {
        return TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED);
    }


    public static final String getNetState(Context context) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connectionManager.getActiveNetworkInfo();
        if (networkinfo != null && networkinfo.isConnected()) {
            return networkinfo.getTypeName();
        }
        return "no network";
    }

    public static int getAndroidSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static String getAndroidSDKVersionName() {
        return Build.VERSION.RELEASE;
    }


    public static String getUUID() {

        String serial;

        String mSzDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +

                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +

                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +

                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +

                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +

                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +

                Build.USER.length() % 10; //13 位

        try {
            serial = Build.SERIAL;
            //API>=29 使用serial号
            return new UUID(mSzDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            // 随便一个初始化
            serial = "serial";
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(mSzDevIDShort.hashCode(), serial.hashCode()).toString();
    }


    /**
     * 手机品牌跟
     */
    public static String getBrandAndModel() {
        return Build.BRAND + " " + Build.MODEL;
    }

    /**
     * 实现文本复制功能
     *
     * @param text
     */
    public static void copy(String text, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(text);
    }


    /**
     * 实现粘贴功能
     *
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String paste(Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

}
