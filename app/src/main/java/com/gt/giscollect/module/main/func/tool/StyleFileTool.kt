package com.gt.giscollect.module.main.func.tool

import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.gt.base.app.AppInfoManager
import org.json.JSONObject

/**
 * 图层配置文件读取工具
 */
object StyleFileTool {

    fun loadRenderer(layer: FeatureLayer) {
        try {
            AppInfoManager.appInfo?.layerstyle?.forEach {
                val obj = JSONObject(it)
                if (obj.has("symbol")) {
                    if (obj.getString("itemName") == layer.featureTable.tableName) {
                        layer.renderer = SimpleRenderer.fromJson(
                            it
                        )
                        return@forEach
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}