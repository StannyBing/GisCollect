package com.gt.base.bean

import android.graphics.Paint
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 * Created by Xiangb on 2019/8/1.
 * 功能：
 */
fun <K, Y> Map<K, Y>.toJson(): RequestBody {
    val json = Gson().toJson(this)
    return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
}

fun Paint.getBaseline(): Float {
    return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
}
