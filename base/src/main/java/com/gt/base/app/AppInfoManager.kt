package com.gt.base.app

import com.gt.base.bean.GisServiceBean
import com.zx.zxutils.util.ZXSharedPrefUtil
import org.json.JSONObject

/**
 * Created by Xiangb on 2019/3/5.
 * 功能：配置管理器
 */
object AppInfoManager {
    val mSharedPrefUtil = ZXSharedPrefUtil()
    var gisService:List<GisServiceBean>?=null
    get() {
        if (field == null) {
            val sharedPref = mSharedPrefUtil
            return sharedPref.getList("gisService")
        }
        return field
    }
    set(value) {
        val sharedPref = mSharedPrefUtil
        sharedPref.putList("gisService", value)
        field = value
    }
    var appInfo: AppInfoBean? = null
        get() {
            if (field == null) {
                val sharedPref = mSharedPrefUtil
                return sharedPref.getObject("appInfoBean")
            }
            return field
        }
        set(value) {
            val sharedPref = mSharedPrefUtil
            sharedPref.putObject("appInfoBean", value)
            field = value
        }
    fun setData(appInfo: String) {
        try {
            val obj = JSONObject(appInfo)
            val appInfoBean = AppInfoBean()
            if (obj.has("appInfo")) {//app信息
                try {
                    val info = obj.getJSONObject("appInfo")
                    appInfoBean.appInfo = AppInfoBean.AppInfo(info.optString("enName"), info.optString("name"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (obj.has("layerstyle")) {//图层style--图层
                try {
                    val layerstyleList = arrayListOf<String>()
                    obj.getJSONObject("layerstyle").keys().forEach {
                        layerstyleList.add(obj.getJSONObject("layerstyle").getJSONObject(it).apply {
                            put("itemName", it)
                        }.toString())
                    }
                    appInfoBean.layerstyle = layerstyleList
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (obj.has("layerstyle")) {//图层style--要素
                try {
                    val identifystyle = arrayListOf<String>()
                    obj.getJSONObject("identifystyle").keys().forEach {
                        identifystyle.add(obj.getJSONObject("identifystyle").getJSONObject(it).apply {
                            put("itemName", it)
                        }.toString())
                    }
                    appInfoBean.identifystyle = identifystyle
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (obj.has("editService")){//可编辑服务
                try {
                    val editService = arrayListOf<String>()
                    obj.getJSONObject("editService").keys().forEach {
                        editService.add(obj.getJSONObject("editService").getJSONObject(it).apply {
                            put("itemName", it)
                        }.toString())
                        appInfoBean.editService = editService
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            if (obj.has("onlineService")) {//在线图层配置
                try {
                    val onlineService = arrayListOf<String>()
                    obj.getJSONObject("onlineService").keys().forEach {
                        onlineService.add(obj.getJSONObject("onlineService").getJSONObject(it).apply {
                            put("itemName", it)
                        }.toString())
                    }
                    appInfoBean.onlineService = onlineService
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (obj.has("apiInfo")) {//服务器配置
                try {
                    val apiInfo = arrayListOf<String>()
                    obj.getJSONObject("apiInfo").keys().forEach {
                        apiInfo.add(obj.getJSONObject("apiInfo").getJSONObject(it).apply {
                            put("itemName", it)
                        }.toString())
                    }
                    appInfoBean.apiInfo = apiInfo
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            AppInfoManager.appInfo = appInfoBean
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}