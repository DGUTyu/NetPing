package com.example.net.util

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.net.R
import com.example.net.config.StartUpBean

/**
 * 将 StartUpBean 指定的自定义 titleBar 挂到诊断/Ping 页 root 顶部。
 * 严禁对非法 resourceId（含 -1 / 0xffffffff）执行 inflate / findViewById。
 */
object TitleBarBinder {

    private const val TAG = "NetPing.TitleBar"

    fun attach(activity: Activity, startUpBean: StartUpBean, rootLayoutId: Int) {
        val layoutId = startUpBean.titleBarLayoutId
        // 未配置 titleBar，或非法 layoutId 时跳过（含 NOT_LAYOUT_ID=-1）
        if (!isValidResId(layoutId)) {
            return
        }
        if (!canResolveRes(activity, layoutId)) {
            Log.w(TAG, "skip titleBar, unresolved layoutId=$layoutId, fallback default")
            attachDefault(activity, rootLayoutId)
            return
        }
        val customTitleBarLayout = try {
            LayoutInflater.from(activity).inflate(layoutId, null)
        } catch (e: Exception) {
            Log.w(TAG, "inflate titleBar failed layoutId=$layoutId", e)
            attachDefault(activity, rootLayoutId)
            return
        }
        val rootView = activity.findViewById<LinearLayout>(rootLayoutId) ?: return
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.addView(customTitleBarLayout, 0, layoutParams)
        bindBackClick(activity, customTitleBarLayout, startUpBean.backId)
    }

    /**
     * 始终使用库内默认标题栏（库 R），避免 Intent 反序列化后的脏 resourceId。
     */
    fun attachDefault(activity: Activity, rootLayoutId: Int) {
        val rootView = activity.findViewById<LinearLayout>(rootLayoutId) ?: return
        val customTitleBarLayout = try {
            LayoutInflater.from(activity).inflate(R.layout.default_title_bar_layout, null)
        } catch (e: Exception) {
            Log.w(TAG, "inflate default titleBar failed", e)
            return
        }
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.addView(customTitleBarLayout, 0, layoutParams)
        bindBackClick(activity, customTitleBarLayout, R.id.iv_titleBarLayout_back)
    }

    private fun bindBackClick(activity: Activity, titleBar: View, backId: Int) {
        val backView = if (isValidResId(backId)) {
            try {
                titleBar.findViewById<View>(backId)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        (backView ?: titleBar).setOnClickListener {
            activity.finish()
        }
    }

    /** 合法 Android 资源 ID：正数且含包 ID；排除 0 / -1 */
    fun isValidResId(resId: Int): Boolean {
        return resId > 0 && (resId ushr 24) != 0
    }

    private fun canResolveRes(activity: Activity, resId: Int): Boolean {
        return try {
            activity.resources.getResourceName(resId)
            true
        } catch (e: Resources.NotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
