package com.gt.giscollect.base

import com.google.gson.JsonObject
import org.json.JSONObject
import java.io.Serializable

data class AppInfoBean(
    var appInfo: AppInfo? = null,
    var onlineService: List<String> = arrayListOf(),
    var editService: List<String> = arrayListOf(),
    var layerstyle: List<String> = arrayListOf(),
    var identifystyle: List<String> = arrayListOf(),
    var apiInfo: List<String> = arrayListOf()
) : Serializable {

    data class AppInfo(
        val enName: String,
        val name: String
    ) : Serializable
}