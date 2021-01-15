package com.gt.giscollect.module.main.func.tool

import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.GeoPackageFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.symbology.Renderer
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.base.AppInfoManager
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream

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