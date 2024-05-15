package com.example.aos.app;

import android.app.Application;
import android.content.Context;

import com.example.net.util.CommonUtils;
import com.example.net.config.NetConfig;
import com.example.net.config.NetConfigUtils;
import com.example.netping.BuildConfig;

public class MyApplication extends Application {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        //自定义NetPing默认域名
        NetConfigUtils.init(new NetConfig() {
            @Override
            public String getDefaultPingUrl() {
                //return super.getDefaultPingUrl();
                //return "https://www.baidu.com";
                return BuildConfig.H5_BASE_URL;
            }

            @Override
            public String getAppInstallTime() {
                return CommonUtils.getAppInstallTime(context);
            }

            @Override
            public String getAppUpdateTime() {
                return CommonUtils.getAppUpdateTime(context);
            }

            @Override
            public String getAppVersion() {
                return BuildConfig.VERSION_NAME;
            }

            @Override
            public String getAppMd5() {
                return CommonUtils.getAppMD5(context);
            }

            @Override
            public String getAppSHA1() {
                return CommonUtils.getAppSHA1(context);
            }

            @Override
            public String getAppSHA256() {
                return CommonUtils.getAppSHA256(context);
            }
        });
    }
}
