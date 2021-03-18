package com.gt.base.tool

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
}