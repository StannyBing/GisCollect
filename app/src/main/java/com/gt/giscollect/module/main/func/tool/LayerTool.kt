package com.gt.giscollect.module.main.func.tool

import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.data.VectorTileCache
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.gt.giscollect.base.AppInfoManager
import com.gt.giscollect.module.main.func.listener.MapListener
import org.json.JSONObject
import java.io.File

object LayerTool {

    fun getVtpkLayer(path: String): ArcGISVectorTiledLayer {
        val tileCache = VectorTileCache(path)
        val layer = ArcGISVectorTiledLayer(tileCache)
        layer.name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
//                        MapTool.postLayerChange(ChangeTag, localMap, MapTool.ChangeType.BaseAdd)
        return layer
    }

    fun getTpkLayer(path: String): ArcGISTiledLayer {
        val tileCache = TileCache(path)
        val layer = ArcGISTiledLayer(tileCache)
        layer.name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
        return layer
    }

    fun addLocalMapLayer(map: ArcGISMap, file: File, callBack: () -> Unit = {}) {

        when (file.path.substring(file.path.lastIndexOf(".") + 1)) {
            "vtpk" -> {
                map.basemap.baseLayers.add(LayerTool.getVtpkLayer(file.path))
                callBack()
            }
            "tpk" -> {
                map.basemap.baseLayers.add(LayerTool.getTpkLayer(file.path))
                callBack()
            }
            "gpkg" -> {
                GeoPackageTool.getTablesFromGpkg(file.path) { geoTables ->
                    AppInfoManager.appInfo?.layerstyle?.forEach {
                        try {
                            val obj = JSONObject(it)
                            if (obj.has("symbol")) {
                                geoTables.forEach { table ->
                                    if (obj.getString("itemName") == table.tableName && (!obj.has("enable") || obj.getBoolean("enable"))) {
                                        val featureLayer = FeatureLayer(table)
                                        featureLayer.renderer = SimpleRenderer.fromJson(
                                            it
                                        )
                                        featureLayer.isVisible =
                                            !obj.has("visible") || obj.getString("visible") == "true"
                                        featureLayer.loadAsync()
                                        map.basemap.baseLayers.add(featureLayer)
//                                        callBack()
//                                                    map.basemap.baseLayers.add(featureLayer)
//                                                    MapTool.postLayerChange(
//                                                        ChangeTag,
//                                                        featureLayer,
//                                                        MapTool.ChangeType.BaseAdd
//                                                    )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
//                    callBack()
                }
            }
//                    else -> {
//                        showToast("暂不支持该文件类型")
//                    }
        }
    }

}