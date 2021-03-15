package com.gt.module_map.tool

import android.graphics.Color
import android.util.Log
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.gt.module_map.R
import com.zx.zxutils.util.ZXToastUtil

object HighLightLayerTool {

    private val highLightLayer = GraphicsOverlay()

    fun showHighLight(feature: Feature) {
        clearHighLight()
        if (feature.geometry == null) return
        MapTool.mapListener?.getMapView()?.setViewpointGeometryAsync(feature.geometry, 80.0)
        val symbol = when (feature.geometry.geometryType) {
            GeometryType.POINT, GeometryType.MULTIPOINT -> {
                SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 5f)
            }
            GeometryType.POLYLINE -> {
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
            }
            GeometryType.POLYGON -> {
                SimpleFillSymbol(
                    SimpleFillSymbol.Style.SOLID,
                    Color.parseColor("#300000FF"),
                    SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
                )
            }
            else -> {
                R.color.blue
                SimpleFillSymbol(
                    SimpleFillSymbol.Style.SOLID,
                    Color.parseColor("#300000FF"),
                    SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
                )
            }
        }
        highLightLayer.graphics.add(Graphic(feature.geometry, symbol))
        try {
            if (MapTool.mapListener?.getMapView()?.graphicsOverlays != null
                && MapTool.mapListener?.getMapView()?.graphicsOverlays?.contains(highLightLayer) == false
            ) {
                MapTool.mapListener?.getMapView()?.graphicsOverlays?.add(highLightLayer)
            }
        } catch (e: Exception) {

        }
    }

    fun clearHighLight() {
        highLightLayer.graphics.clear()
    }

}