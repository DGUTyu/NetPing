package com.example.net.util

import android.app.Activity
import android.util.Log

/**
 * 宿主若接入 AndroidAutoSize，进页前取消对本 Activity 的密度适配，
 * 避免联迪 POS 上二次改 density 触发整树重测 / 资源解析异常。
 * 无 AutoSize 依赖时静默跳过。
 */
object AutoSizeGuard {

    private const val TAG = "NetPing.AutoSize"

    @JvmStatic
    fun cancelAdapt(activity: Activity) {
        try {
            val clazz = Class.forName("me.jessyan.autosize.AutoSize")
            val method = clazz.getMethod("cancelAdapt", Activity::class.java)
            method.invoke(null, activity)
        } catch (ignored: Throwable) {
            Log.d(TAG, "AutoSize not present or cancelAdapt skipped")
        }
    }
}
