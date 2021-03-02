package com.gt.entrypad.module.project.bean

import android.graphics.PointF
import com.esri.arcgisruntime.geometry.Point
import java.util.*

data class RtkPointBean(var id:String=UUID.randomUUID().toString(), var parentId:String = "",var title:String="", var distance:Double=0.0, var point:PointF=PointF(),var sitePoint:Point= Point(0.0,0.0),var resultSitePoint:Point= Point(0.0,0.0)){
    var result = ""
    get() {
        field = if (sitePoint.x==0.0&&sitePoint.y==0.0) "" else "${sitePoint.x},${sitePoint.y}"
        return field
    }
    var resultDistance = ""
        get() {
            field = if (distance==0.0) "" else "$distance"
            return field
        }
}