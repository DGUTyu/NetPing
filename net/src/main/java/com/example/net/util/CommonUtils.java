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
     * 获取应用的安装时间和最近更新时间
     *
     * @param context 应用的 Context
     * @return 包含应用的安装时间和最近更新时间的字符串数组，
     *         第一个元素是安装时间，第二个元素是更新时间，
     *         如果获取失败，则返回空字符串
     */
    public static String[] getAppTimeInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        String[] timeInfo = new String[2];
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long installTimeMillis = packageInfo.firstInstallTime;
            long updateTimeMillis = packageInfo.lastUpdateTime;
            // 格式化安装时间和更新时间，并放入数组中
            timeInfo[0] = formatDate(installTimeMillis);
            timeInfo[1] = formatDate(updateTimeMillis);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return timeInfo;
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
     * 获取应用 APK 文件的 MD5、SHA-1 和 SHA-256 消息摘要
     *
     * @param context 应用的 Context
     * @return 包含应用 APK 文件的 MD5、SHA-1 和 SHA-256 消息摘要的字符串数组，
     *         分别对应数组的第 0、1、2 个元素，
     *         如果获取失败，则对应元素为 ""
     */
    public static String[] getAppDigest(Context context) {
        // 获取应用 APK 文件的路径
        String apkFilePath = context.getPackageCodePath();
        File apkFile = new File(apkFilePath);

        // 调用 getFileDigest 方法获取文件的 MD5、SHA-1 和 SHA-256 摘要
        return getFileDigest(apkFile);
    }

    /**
     * 获取文件的 MD5、SHA-1 和 SHA-256 消息摘要
     *
     * @param file 要计算消息摘要的文件
     * @return 包含 MD5、SHA-1 和 SHA-256 消息摘要的字符串数组，
     *         分别对应数组的第 0、1、2 个元素，
     *         如果获取失败，则对应元素为 ""
     */
    public static String[] getFileDigest(File file) {
        String[] digests = new String[3];
        digests[0] = getFileDigest(file, "MD5");
        digests[1] = getFileDigest(file, "SHA-1");
        digests[2] = getFileDigest(file, "SHA-256");
        return digests;
    }

    /**
     * 获取文件的消息摘要
     *
     * @param file      要计算摘要的文件
     * @param algorithm 摘要算法（"MD5"、"SHA-1" 或 "SHA-256"）
     * @return 文件的消息摘要
     */
    private static String getFileDigest(File file, String algorithm) {
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