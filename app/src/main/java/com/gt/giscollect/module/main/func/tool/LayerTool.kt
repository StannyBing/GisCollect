package com.gt.giscollect.module.main.func.tool

import android.graphics.Color
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.data.VectorTileCache
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.KmlLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.ogc.kml.KmlDataset
import com.esri.arcgisruntime.ogc.kml.KmlDocument
import com.esri.arcgisruntime.ogc.kml.KmlNode
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import com.gt.base.app.AppInfoManager
import com.gt.giscollect.module.collect.ui.CollectCreateFragment
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXToastUtil
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

    fun loadLocalFile(map: ArcGISMap, path: String, callBack: () -> Unit = {}) {
        ZXToastUtil.showToast("path:${path}")
        when (path.substring(path.lastIndexOf(".") + 1)) {
            "shp" -> {
                val shapefileFeatureTable = ShapefileFeatureTable(path)
                shapefileFeatureTable.loadAsync() //异步方式读取文件
                shapefileFeatureTable.addDoneLoadingListener {
                    //                    val query = shapefileFeatureTable.queryFeaturesAsync(QueryParameters().apply {
//                        whereClause = "1=1"
//                    })
//                    query.addDoneListener {
//                        query.get().forEach {
//                            ZXLogUtil.loge(it.attributes.toString())
//                        }
//                    }
                    //数据加载完毕后，添加到地图
                    val mainShapefileLayer = FeatureLayer(shapefileFeatureTable)
//                    mainShapefileLayer.renderer = UniqueValueRenderer().apply {
//                        defaultSymbol = when (mainShapefileLayer.featureTable.geometryType) {
//                            GeometryType.POINT, GeometryType.MULTIPOINT -> {
//                                SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, null)
//                            }
//                            GeometryType.POLYLINE -> {
//                                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
//                            }
//                            GeometryType.POLYGON -> {
//                                SimpleFillSymbol(
//                                    SimpleFillSymbol.Style.SOLID,
//                                    Color.parseColor("#50FF0000"),
//                                    null
//                                )
//                            }
//                            else -> {
//                                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
//                            }
//                        }
//                    }
                    MapTool.postLayerChange(
                        "localfile",
                        mainShapefileLayer,
                        MapTool.ChangeType.BaseAdd
                    )
//                    map.basemap.baseLayers.add(mainShapefileLayer)
                    callBack()
                }
            }
            "kml" -> {
                val kmlDataSet = KmlDataset(path)
                val kmlLayer = KmlLayer(kmlDataSet)
                MapTool.postLayerChange(
                    "localfile",
                    kmlLayer,
                    MapTool.ChangeType.BaseAdd
                )
                callBack()
            }
        }
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
                                    if (obj.getString("itemName") == table.tableName && (!obj.has("enable") || obj.getBoolean(
                                            "enable"
                                        ))
                                    ) {
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