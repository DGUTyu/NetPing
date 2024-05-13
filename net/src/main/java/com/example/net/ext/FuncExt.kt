package com.example.net.ext

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * 将时间戳转换成日期字符串（默认格式为yyyy-MM-dd HH:mm:ss.SSS）
 * @param format 传入格式化样式
 * @return 若时间戳为空，则返回空字符串。
 */
fun Long?.formatDate(format: String = "yyyy-MM-dd HH:mm:ss.SSS"): String = this?.let {
    val date = Date(it)
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    sdf.format(date)
} ?: ""


fun Long.toSPayRsJson(): String {
    val json = JSONObject()
    json.put("spayRs", this.toString())
    return json.toString()
}
