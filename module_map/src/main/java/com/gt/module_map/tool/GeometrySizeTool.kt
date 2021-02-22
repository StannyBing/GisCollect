package com.gt.module_map.tool

import com.esri.arcgisruntime.geometry.*
import java.math.BigDecimal

object GeometrySizeTool {

    fun getLength(geometry: Geometry, wkid: Int = 0): BigDecimal {
        if (geometry.isEmpty){
            return 0.toBigDecimal()
        }
        return GeometryEngine.length(GeometryEngine.project(geometry, SpatialReference.create(if (wkid == 0) 4524 else wkid)) as Polyline).toBigDecimal().abs()
    }

    fun getArea(geometry: Geometry, wkid: Int = 0): BigDecimal {
        if (geometry.isEmpty){
            return 0.toBigDecimal()
        }
        return GeometryEngine.area(GeometryEngine.project(geometry, SpatialReference.create(if (wkid == 0) 4524 else wkid)) as Polygon).toBigDecimal().abs()
//        return GeometryEngine.area(geometry as Polygon).toBigDecimal().abs()
    }

}