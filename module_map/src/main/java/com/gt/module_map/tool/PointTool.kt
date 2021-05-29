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

    var x_PI = 3.14159265358979324 * 3000.0 / 180.0
    var PI = 3.1415926535897932384626
    var a = 6378245.0
    var ee = 0.00669342162296594323

    /**
     * WGS84转GCj02
     * @param lng
     * @param lat
     * @returns {*[]}
     */
    fun wgs84togcj02(
        lng: Double,
        lat: Double
    ): Point? {
        var dlat: Double = transformlat(lng - 105.0, lat - 35.0)
        var dlng: Double = transformlng(lng - 105.0, lat - 35.0)
        val radlat: Double = lat / 180.0 * PI
        var magic = Math.sin(radlat)
        magic = 1 - ee * magic * magic
        val sqrtmagic = Math.sqrt(magic)
        dlat = dlat * 180.0 / (a * (1 - ee) / (magic * sqrtmagic) * PI)
        dlng = dlng * 180.0 / (a / sqrtmagic * Math.cos(radlat) * PI)
        val mglat = lat + dlat
        val mglng = lng + dlng
        return Point(mglng, mglat)
    }

    private fun transformlat(lng: Double, lat: Double): Double {
        var ret =
            -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(
                Math.abs(lng)
            )
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformlng(lng: Double, lat: Double): Double {
        var ret =
            300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(
                Math.abs(lng)
            )
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

}