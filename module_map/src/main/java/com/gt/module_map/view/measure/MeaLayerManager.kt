package com.gt.module_map.view.measure

import android.content.Context
import android.graphics.Color
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol

/**
 * 元素绘制管理器
 */
class MeaLayerManager(private var context: Context, private var meaListener: MeaListener) {

    var meaLayer: GraphicsOverlay = GraphicsOverlay()
    private var polygonSymbol: SimpleFillSymbol = SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#6055A4F1"), null)
    private var polylineSymbol: SimpleLineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2f)
    private var pointSymbol: SimpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10f)

    /**
     * 清除graphic
     */
    fun clear() {
        meaLayer.graphics.clear()
    }

    /**
     * 开启平移
     */
    fun startTranslate() {
        polylineSymbol.color = Color.parseColor("#6055A4F1")
        pointSymbol.color = Color.parseColor("#6055A4F1")
        updateGraphic()
    }

    /**
     * 结束平移
     */
    fun stopTranslate() {
        polylineSymbol.color = Color.RED
        pointSymbol.color = Color.RED
        updateGraphic()
    }

    /**
     * 更新graphic
     */
    fun updateGraphic() {
        clear()
        if (meaListener.getPointList().isNotEmpty()) {
            //绘制面
            if (meaListener.getPointList().size > 2 && meaListener.getGeoType() == MeasureView.GeoType.POLYGON) {
                val polygon = Polygon(PointCollection(meaListener.getPointList()))
                meaLayer.graphics.add(Graphic(polygon).apply {
                    symbol = polygonSymbol
                })

            }
            //绘制线-为了插入等操作，面必须一条一条的加
            if (meaListener.getPointList().size > 1) {
                for (i in 0 until meaListener.getPointList().size) {
                    //面模式，需要加上最后一个点
                    val polyline =
                        if (meaListener.getGeoType() == MeasureView.GeoType.POLYGON && i == meaListener.getPointList().size - 1 && meaListener.getPointList().size > 2) {
                            Polyline(
                                PointCollection(
                                    arrayListOf(
                                        meaListener.getPointList()[i],
                                        meaListener.getPointList()[0]
                                    )
                                )
                            )
                        } else if (i < meaListener.getPointList().size - 1) {
                            Polyline(
                                PointCollection(
                                    arrayListOf(
                                        meaListener.getPointList()[i],
                                        meaListener.getPointList()[i + 1]
                                    )
                                )
                            )
                        } else {
                            null
                        }
                    if (polyline != null) {
                        val graphic = Graphic(polyline)
                        graphic.attributes["lineIndex"] = i
                        graphic.symbol = polylineSymbol
                        meaLayer.graphics.add(graphic)
                    }
                }
            }
            //绘制点
            meaListener.getPointList().forEachIndexed { index, it ->
                val graphic = Graphic(it)
                graphic.symbol = pointSymbol
                graphic.attributes["pointIndex"] = index
                meaLayer.graphics.add(graphic)
            }
            if (!meaListener.getMapView().graphicsOverlays.contains(meaLayer)) {
                meaListener.getMapView().graphicsOverlays.add(meaLayer)
            }
            meaListener.onPointChange()
        }
    }

}