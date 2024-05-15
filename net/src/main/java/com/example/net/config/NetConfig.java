package com.example.net.config;

import com.example.net.BuildConfig;
import com.example.net.R;

/**
 * 抽象类，用于访问 调用者的 BuildConfigField 变量
 */
public abstract class NetConfig {
    public static final String NOT_SET = "Not set";
    public static final int NOT_LAYOUT_ID = -1;

    public String getDefaultPingUrl() {
        return BuildConfig.BASE_URL;
    }

    public String getAppVersion() {
        return NOT_SET;
    }

    public String[] getAppTimeInfo() {
        return new String[]{NOT_SET, NOT_SET};
    }

    public String[] getAppDigest() {
        return new String[]{NOT_SET, NOT_SET, NOT_SET};
    }

    public int getTitleBarLayoutId() {
        return R.layout.default_title_bar_layout;
    }
}
