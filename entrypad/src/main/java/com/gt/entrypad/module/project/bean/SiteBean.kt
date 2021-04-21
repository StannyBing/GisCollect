package com.gt.entrypad.module.project.bean

import android.graphics.PointF
import java.util.*
import kotlin.collections.ArrayList

data class SiteBean(var id: String = UUID.randomUUID().toString(), var point:PointF=PointF(), var title:String="", var rtkList:ArrayList<RtkPointBean>?=arrayListOf(),
                    //角度
                    var angle:Double=0.0){
    var status = ""
    get() {
       field =if (rtkList.isNullOrEmpty()) "未编辑" else {
           var tempStatus = "未编辑"
           run siten@{
            rtkList?.forEach {
                if (it.resultSitePoint.x==0.0||it.resultSitePoint.y==0.0){
                    return@siten
                }else{
                    tempStatus = "经度:${it.resultSitePoint.x}纬度:${it.resultSitePoint.y}"
                }
            }
           }
           tempStatus
       }
        return  field
    }

}