package com.example.net.config;

import com.example.net.BuildConfig;
import com.example.net.R;

public class NetConfigUtils {
    private static NetConfig netConfig;

    public static synchronized boolean init(NetConfig config) {
        if (netConfig != null) {
            //throw new IllegalStateException("ImageViewPro has already been initialized");
            return false;
        }

        if (!validateAppConfig(config)) {
            //throw new IllegalArgumentException("Invalid AppConfig object");
            return false;
        }

        netConfig = config;
        return true;
    }

    private static boolean isInitialized() {
        return netConfig != null;
    }

    private static boolean validateAppConfig(NetConfig config) {
        if (config == null) {
            return false;
        }
        try {
            // 尝试调用方法以验证是否存在
            config.getDefaultPingUrl();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取默认的ping url
     *
     * @return
     */
    public static String getDefaultPingUrl() {
        if (!isInitialized()) {
            return BuildConfig.BASE_URL;
        }
        return netConfig.getDefaultPingUrl();
    }

    /**
     * 获取接入方 APP 的安装时间和最近更新时间
     *
     * @return
     */
    public static String[] getAppTimeInfo() {
        if (!isInitialized()) {
            return new String[]{NetConfig.NOT_SET, NetConfig.NOT_SET};
        }
        return netConfig.getAppTimeInfo();
    }

    /**
     * 获取接入方 APP 的 MD5、SHA-1 和 SHA-256
     *
     * @return
     */
    public static String[] getAppDigest() {
        if (!isInitialized()) {
            return new String[]{NetConfig.NOT_SET, NetConfig.NOT_SET, NetConfig.NOT_SET};
        }
        return netConfig.getAppDigest();
    }

    /**
     * 获取接入方 APP 的版本号
     *
     * @return
     */
    public static String getAppVersion() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppVersion();
    }

    /**
     * 获取顶部titleBar的布局文件ID
     *
     * @return
     */
    public static int getTitleBarLayoutId() {
        if (!isInitialized()) {
            return R.layout.default_title_bar_layout;
        }
        return netConfig.getTitleBarLayoutId();
    }
}
