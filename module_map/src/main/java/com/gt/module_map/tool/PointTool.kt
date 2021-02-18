package com.gt.module_map.tool

import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference

object PointTool {

    fun change4326To3857(point: Point, wkid: Int = 0): Point {
        return GeometryEngine.project(
            point,
            SpatialReference.create(if (wkid == 0) 3857 else wkid)
        ) as Point
    }

}