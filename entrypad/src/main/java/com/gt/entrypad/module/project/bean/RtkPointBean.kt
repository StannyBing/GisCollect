package com.gt.entrypad.module.project.bean

import android.graphics.PointF
import java.util.*

data class RtkPointBean(var id:String=UUID.randomUUID().toString(), var parentId:String = "",var title:String="", var distance:Double=0.0, var point:PointF=PointF()){
    var result = ""
    get() {
        field = if (point.x.toDouble()==0.0&&point.y.toDouble()==0.0) "" else "${point.x},${point.y}"
        return field
    }
    var resultDistance = ""
        get() {
            field = if (distance==0.0) "" else "$distance"
            return field
        }
}