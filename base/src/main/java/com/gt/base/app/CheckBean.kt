package com.gt.base.app

import org.json.JSONObject
import java.lang.Exception

class CheckBean(
    var id: String,
    var collector: String,
    var collectId: String,
    var checker: String,
    var created: String,
    var gpkginfo: String,
    var fileJson: String,
    var note: String,
    var status: String,
    var layerName: String,
    var catalogId : String,
    var templateId : String
) {

    fun getFilePath(): String {
        try {
            if (!gpkginfo.isNullOrEmpty()) {
                val json = JSONObject(gpkginfo)
                return json.getString("gpkgPath")
            }
            if (!fileJson.isNullOrEmpty()) {
                val json = JSONObject(fileJson)
                return json.getString("gpkgPath")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getFileName(): String {
        if (getFilePath().isNotEmpty()) {
            if (getFilePath().contains("\\")) {
                return getFilePath().substring(getFilePath().lastIndexOf("\\")).replace("\\", "")
            } else if (getFilePath().contains("/")) {
                return getFilePath().substring(getFilePath().lastIndexOf("/")).replace("/", "")
            } else {
                return getFilePath().replace("\\", "")
            }
        }
        return ""
    }
}