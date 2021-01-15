package com.gt.giscollect.module.main.func.listener

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.gt.module_map.view.measure.MeasureView

interface MapListener {

    fun doLocation()

    fun getMapView(): MapView?

    fun getMap(): ArcGISMap?

    fun addSingleTapListener(singleTap: OnSingleTapCall)

    fun getMeasure(): MeasureView?

    interface OnSingleTapCall {
        fun onSingleTap(x: Float, y: Float)
    }

}