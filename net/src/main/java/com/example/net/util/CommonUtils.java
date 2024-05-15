package com.example.net.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommonUtils {
    //字符串判空
    public static boolean isEmptyOrNull(String str) {
        if (null == str || "".equals(str) || " ".equals(str) || "null".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * 重启APP
     *
     * @param context
     */
    public static void restartApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName(context.getPackageName(),
                getLauncherActivity(context, context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 获取指定包名 pkg 的启动器活动的完整类名。方法返回一个字符串，该字符串是启动器活动的完整类名。
     *
     * @param context
     * @param pkg
     * @return
     */
    public static String getLauncherActivity(Context context, String pkg) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(pkg);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        return info.get(0).activityInfo.name;
    }

    /**
     * 获取应用的安装时间
     *
     * @param context 应用的 Context
     * @return 应用的安装时间（字符串格式：yyyy-MM-dd HH:mm:ss）
     */
    public static String getAppInstallTime(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long installTimeMillis = packageInfo.firstInstallTime;
            return formatDate(installTimeMillis);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取应用的最近更新时间
     *
     * @param context 应用的 Context
     * @return 应用的最近更新时间（字符串格式：yyyy-MM-dd HH:mm:ss）
     */
    public static String getAppUpdateTime(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long updateTimeMillis = packageInfo.lastUpdateTime;
            return formatDate(updateTimeMillis);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 返回字符串格式的时间
     *
     * @param millis
     * @return
     */
    private static String formatDate(long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(millis));
    }

    /**
     * 获取APP自身的 MD5
     *
     * @param context
     * @return
     */
    public static String getAppMD5(Context context) {
        String apkFilePath = context.getPackageCodePath();
        File apkFile = new File(apkFilePath);
        return getFileMD5(apkFile);
    }

    /**
     * 获取文件的 MD5
     *
     * @param file
     * @return
     */
    private static String getFileMD5(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            fis.close();

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * 获取应用自身 APK 文件的 SHA-1 值
     *
     * @param context 应用的 Context
     * @return 应用自身 APK 文件的 SHA-1 值
     */
    public static String getAppSHA1(Context context) {
        String apkFilePath = context.getPackageCodePath();
        File apkFile = new File(apkFilePath);
        return getFileSHA(apkFile, "SHA-1");
    }

    /**
     * 获取应用自身 APK 文件的 SHA-256 值
     *
     * @param context 应用的 Context
     * @return 应用自身 APK 文件的 SHA-256 值
     */
    public static String getAppSHA256(Context context) {
        String apkFilePath = context.getPackageCodePath();
        File apkFile = new File(apkFilePath);
        return getFileSHA(apkFile, "SHA-256");
    }

    /**
     * 获取文件的 SHA 值
     *
     * @param file      要计算 SHA 值的文件
     * @param algorithm SHA 算法（"SHA-1" 或 "SHA-256"）
     * @return 文件的 SHA 值
     */
    private static String getFileSHA(File file, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }

            fis.close();

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}