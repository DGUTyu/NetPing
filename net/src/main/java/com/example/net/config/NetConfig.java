package com.example.net.config;

import com.example.net.BuildConfig;

/**
 * 抽象类，用于访问 调用者的 BuildConfigField 变量
 */
public abstract class NetConfig {
    public String getDefaultPingUrl() {
        return BuildConfig.BASE_URL;
    }
}
