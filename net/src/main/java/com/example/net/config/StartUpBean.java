package com.example.net.config;

import com.example.net.R;

import java.io.Serializable;

/**
 * 实体类，用于存放启动网络检测页面的一些参数。
 * titleBarLayoutId / backId 必须是合法资源 ID；无标题栏时二者均为 0（禁止使用 -1 参与 inflate/findViewById）。
 */
public class StartUpBean implements Serializable {
    /** @deprecated 保留兼容；请用 0 表示无标题栏，避免 0xffffffff 资源风暴 */
    public static final int NOT_LAYOUT_ID = -1;
    /** 无标题栏 */
    public static final int NO_TITLE_BAR = 0;

    //顶部标题栏布局文件Id
    private int titleBarLayoutId;
    //顶部标题栏返回按钮Id
    private int backId;

    public StartUpBean() {
        this.titleBarLayoutId = R.layout.default_title_bar_layout;
        this.backId = R.id.iv_titleBarLayout_back;
    }

    public StartUpBean(int titleBarLayoutId, int backId) {
        this.titleBarLayoutId = sanitize(titleBarLayoutId);
        this.backId = sanitize(backId);
    }

    public int getTitleBarLayoutId() {
        return sanitize(titleBarLayoutId);
    }

    public void setTitleBarLayoutId(int titleBarLayoutId) {
        this.titleBarLayoutId = sanitize(titleBarLayoutId);
    }

    public int getBackId() {
        return sanitize(backId);
    }

    public void setBackId(int backId) {
        this.backId = sanitize(backId);
    }

    public void setNoTitleBar() {
        this.titleBarLayoutId = NO_TITLE_BAR;
        this.backId = NO_TITLE_BAR;
    }

    public boolean hasTitleBar() {
        return titleBarLayoutId > 0;
    }

    /** 将 -1 等非法 ID 规整为 0，杜绝资源解析 0xffffffff */
    private static int sanitize(int resId) {
        if (resId == NOT_LAYOUT_ID || resId < 0) {
            return NO_TITLE_BAR;
        }
        return resId;
    }
}
