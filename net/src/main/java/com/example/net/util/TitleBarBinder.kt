package com.example.net.util

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.net.config.StartUpBean

/**
 * 将 StartUpBean 指定的自定义 titleBar 挂到诊断/Ping 页 root 顶部。
 */
object TitleBarBinder {

    fun attach(activity: Activity, startUpBean: StartUpBean, rootLayoutId: Int) {
        val layoutId = startUpBean.titleBarLayoutId
        // 未配置 titleBar，或非法 layoutId 时跳过
        if (layoutId == StartUpBean.NOT_LAYOUT_ID || layoutId <= 0) {
            return
        }
        // 将自定义的 titleBarLayout 添加到布局中
        val customTitleBarLayout = LayoutInflater.from(activity).inflate(layoutId, null)
        val rootView = activity.findViewById<LinearLayout>(rootLayoutId) ?: return
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // 添加在第一个位置
        rootView.addView(customTitleBarLayout, 0, layoutParams)
        // backId <= 0 时不 findViewById(-1)，避免 No package ID ff / 0xffffffff
        val backId = startUpBean.backId
        val backView = if (backId > 0) {
            customTitleBarLayout.findViewById<View>(backId)
        } else {
            null
        }
        // 设置点击事件，如果 backView 为空则设置 customTitleBarLayout 的点击事件，否则设置 backView 的点击事件
        (backView ?: customTitleBarLayout).setOnClickListener {
            // 处理点击事件，finish当前页面
            activity.finish()
        }
    }
}
