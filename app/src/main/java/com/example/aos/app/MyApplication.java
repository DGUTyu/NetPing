package com.example.aos.app;

import android.app.Application;

import com.example.net.config.NetConfig;
import com.example.net.config.NetConfigUtils;
import com.example.netping.BuildConfig;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //自定义NetPing默认域名
        NetConfigUtils.init(new NetConfig() {
            @Override
            public String getDefaultPingUrl() {
                //return super.getDefaultPingUrl();
                //return "https://www.baidu.com";
                return BuildConfig.H5_BASE_URL;
            }
        });
    }
}
