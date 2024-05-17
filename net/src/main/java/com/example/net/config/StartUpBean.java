package com.example.net.config;

import com.example.net.R;

import java.io.Serializable;

/**
 * 实体类，用于存放启动网络检测页面的一些参数
 */
public class StartUpBean implements Serializable {
    public static final int NOT_LAYOUT_ID = -1;
    //顶部标题栏布局文件Id
    private int titleBarLayoutId;
    //顶部标题栏返回按钮Id
    private int backId;

    public StartUpBean() {
        this.titleBarLayoutId = R.layout.default_title_bar_layout;
        this.backId = R.id.iv_titleBarLayout_back;
    }

    public StartUpBean(int titleBarLayoutId, int backId) {
        this.titleBarLayoutId = titleBarLayoutId;
        this.backId = backId;
    }

    public int getTitleBarLayoutId() {
        return titleBarLayoutId;
    }

    public void setTitleBarLayoutId(int titleBarLayoutId) {
        this.titleBarLayoutId = titleBarLayoutId;
    }

    public int getBackId() {
        return backId;
    }

    public void setBackId(int backId) {
        this.backId = backId;
    }

    public void setNoTitleBar() {
        this.titleBarLayoutId = NOT_LAYOUT_ID;
    }
}
