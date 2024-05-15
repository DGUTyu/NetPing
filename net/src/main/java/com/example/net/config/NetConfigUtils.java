package com.example.net.config;

import com.example.net.BuildConfig;

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
     * 获取接入方 APP 的安装时间
     *
     * @return
     */
    public static String getAppInstallTime() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppInstallTime();
    }

    /**
     * 获取接入方 APP 的最近更新时间
     *
     * @return
     */
    public static String getAppUpdateTime() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppUpdateTime();
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
     * 获取接入方 APP 的 MD5
     *
     * @return
     */
    public static String getAppMd5() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppMd5();
    }

    /**
     * 获取接入方 APP 的 SHA1
     *
     * @return
     */
    public static String getAppSHA1() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppSHA1();
    }

    /**
     * 获取接入方 APP 的 SHA256
     *
     * @return
     */
    public static String getAppSHA256() {
        if (!isInitialized()) {
            return NetConfig.NOT_SET;
        }
        return netConfig.getAppSHA256();
    }
}
