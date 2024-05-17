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
