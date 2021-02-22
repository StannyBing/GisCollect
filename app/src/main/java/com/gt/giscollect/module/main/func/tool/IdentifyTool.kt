package com.gt.giscollect.module.main.func.tool

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.gt.module_map.tool.HighLightLayerTool
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXToastUtil

/**
 * 要素查询
 */
object IdentifyTool {

    private var context: Context? = null
    private var defaultListener: View.OnTouchListener? = null
    private var callBack: (List<Feature>) -> Unit = {}


    fun stopQueryIdentify() {
        //还原地图点击事件
        if (defaultListener != null) {
            MapTool.mapListener?.getMapView()?.onTouchListener = defaultListener
        }
        clearAllFeatureSelect()
    }

    fun startQueryIdentify(context: Context, callback: (List<Feature>) -> Unit) {
        ZXToastUtil.showToast("请点击地图查询要素！")
        clearAllFeatureSelect()

        this.context = context
        this.callBack = callback
        this.defaultListener = MapTool.mapListener?.getMapView()?.onTouchListener
//        mapListener?.getMapView()?.isMagnifierEnabled = true
        MapTool.mapListener?.getMapView()?.onTouchListener = object : DefaultMapViewOnTouchListener(context, MapTool.mapListener?.getMapView()) {

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                identifyMapLayers(e)
                return true
            }

//            private var isOnLongPress = true
//
//            override fun onLongPress(e: MotionEvent?) {
//                isOnLongPress = true
//                super.onLongPress(e)
//            }
//
//            override fun onUp(e: MotionEvent?): Boolean {
//                if (isOnLongPress) {
//                    identifyMapLayers(e)
//                }
//                isOnLongPress = false
//                return super.onUp(e)
//            }
        }
    }

    /**
     * 查询地图要素
     */
    private fun identifyMapLayers(e: MotionEvent?) {
        if (MapTool.mapListener?.getMapView() == null || e == null) {
            return
        }
        val point = Point(e.x.toInt(), e.y.toInt())
        val identifyListenable = MapTool.mapListener?.getMapView()?.identifyLayersAsync(point, 5.0, false)
        val features = arrayListOf<Feature>()
        identifyListenable?.addDoneListener {
            //从当前地图的feature里面获取
            try {

                identifyListenable?.get()?.forEach { result ->
                    getFeatureFromReusult(features, result)
//                result.elements.forEach { element ->
//                    if (element is Feature) {
//                        features.add(element)
//                    }
//                }
                }
                selectFeature(features)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //从在线服务中获取
        //从geopackage里获取
        GeoPackageTool.getIdentifyFromGpkg(MapTool.mapListener?.getMapView()?.screenToLocation(point)!!) {
            features.addAll(it)
            selectFeature(features)
        }
    }

    private fun getFeatureFromReusult(features: ArrayList<Feature>, result: IdentifyLayerResult) {
        if (!result.elements.isNullOrEmpty()) {
            result.elements.forEach {
                if (it is Feature) {
                    features.add(it)
                }
            }
        }
        if (!result.sublayerResults.isNullOrEmpty()) {
            result.sublayerResults.forEach {
                getFeatureFromReusult(features, it)
            }
        }
    }

    /**
     * 选择查询的要素
     */
    private fun selectFeature(features: ArrayList<Feature>) {
        if (features.isEmpty()) {
//            ZXToastUtil.showToast("当前未选中任何要素")
        } else {
            callBack(features)
//            val names = arrayListOf<String>()
//            features.forEach {
//                names.add(it.featureTable.featureLayer.name)
//            }
//            ZXDialogUtil.showListDialog(context, "请选择图层要素", "取消", names) { dialog, which ->
////                mapListener?.getMapView()?.setViewpointGeometryAsync(features[which].geometry)
//                callBack(features[which])
//            }
        }
    }

    /**
     * 清空所有要素选择
     */
    private fun clearAllFeatureSelect() {
        HighLightLayerTool.clearHighLight()
//        MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
//            if (it is FeatureLayer) {
//                it.clearSelection()
//            }
//        }
//        MapTool.mapListener?.getMap()?.basemap?.baseLayers?.forEach {
//            if (it is FeatureLayer) {
//                it.clearSelection()
//            }
//        }
    }

}