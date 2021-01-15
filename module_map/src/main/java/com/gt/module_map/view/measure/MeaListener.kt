package com.gt.module_map.view.measure

import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.MapView

interface MeaListener {

    fun getMapView(): MapView

    fun updateGraphic()

    fun onPointChange()

    fun onMapReAdd()

    fun getGeoType(): MeasureView.GeoType

    fun getPointList(): ArrayList<Point>

    fun startTranslate()

    fun stopTranslate()

    fun isTranslate(): Boolean
}