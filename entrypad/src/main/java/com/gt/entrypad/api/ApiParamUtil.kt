package com.gt.entrypad.api

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody

object ApiParamUtil {
    fun toJson(map: Map<String, Any>): RequestBody {
        val json = Gson().toJson(map)
        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), json)
    }
    fun scanIdCardParam(img:String,url:String,figure:Boolean):RequestBody{
        val map = hashMapOf<String,Any>()
        map["img"]=img
        map["url"]=url
        map["figure"]=figure
        return toJson(map)
    }
}