package com.gt.base.tool

import android.content.Context
import org.json.JSONException
import org.json.JSONObject

object MyUtil{
    /**
     * json转map
     *
     * @param jsonObject
     * @return
     */
     fun jsonToLinkedHashMap(jsonObject: JSONObject): LinkedHashMap<String, String> {
        val result = LinkedHashMap<String, String>()
        val iterator = jsonObject.keys()
        var key: String
        var value = ""
        while (iterator.hasNext()) {
            key = iterator.next()
            try {
                value = jsonObject.getString(key)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            result[key.toLowerCase()] = value
        }
        return result
    }
    /** 判断手机中是否安装指定包名的软件  */
    fun isInstallApk(context: Context, name: String): Boolean {
        val packages = context.packageManager.getInstalledPackages(0)
        for (i in packages.indices) {
            val packageInfo = packages[i]
            return if (packageInfo.packageName == name) {
                true
            } else {
                continue
            }
        }
        return false
    }
}