package com.gt.giscollect.module.collect.func.tool

import com.esri.arcgisruntime.geometry.*
import com.zx.zxutils.util.ZXLogUtil
import java.math.BigDecimal

object GeometrySizeTool {

    fun getLength(geometry: Geometry, wkid: Int = 0): BigDecimal {
        return GeometryEngine.length(GeometryEngine.project(geometry, SpatialReference.create(if (wkid == 0) 4524 else wkid)) as Polyline).toBigDecimal().abs()
    }

    fun getArea(geometry: Geometry, wkid: Int = 0): BigDecimal {
        return GeometryEngine.area(GeometryEngine.project(geometry, SpatialReference.create(if (wkid == 0) 4524 else wkid)) as Polygon).toBigDecimal().abs()
//        return GeometryEngine.area(geometry as Polygon).toBigDecimal().abs()
    }

}