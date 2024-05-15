package com.example.net.config;

import com.example.net.BuildConfig;

/**
 * 抽象类，用于访问 调用者的 BuildConfigField 变量
 */
public abstract class NetConfig {
    public static final String NOT_SET = "Not set";

    public String getDefaultPingUrl() {
        return BuildConfig.BASE_URL;
    }

    public String getAppInstallTime() {
        return NOT_SET;
    }

    public String getAppUpdateTime() {
        return NOT_SET;
    }

    public String getAppVersion() {
        return NOT_SET;
    }

    public String getAppMd5() {
        return NOT_SET;
    }

    public String getAppSHA1() {
        return NOT_SET;
    }

    public String getAppSHA256() {
        return NOT_SET;
    }
}
