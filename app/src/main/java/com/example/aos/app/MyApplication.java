package com.example.aos.app;

import android.app.Application;
import android.content.Context;

import com.example.net.util.CommonUtils;
import com.example.net.config.NetConfig;
import com.example.net.config.NetConfigUtils;
import com.example.netping.BuildConfig;
import com.example.netping.R;

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

//            @Override
//            public int getTitleBarLayoutId() {
//                //默认TitleBarLayout
//                //return super.getTitleBarLayoutId();
//                //取消TitleBarLayout
//                //return NetConfig.NOT_LAYOUT_ID;
//                //自定义TitleBarLayout
//                //return R.layout.title_bar_layout;
//            }
        });
    }
}
