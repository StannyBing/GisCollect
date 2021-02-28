package com.gt.entrypad.module.project.bean

import android.graphics.PointF
import java.util.*
import kotlin.collections.ArrayList

data class SiteBean(var id: String = UUID.randomUUID().toString(), var point:PointF=PointF(), var title:String="", var rtkList:ArrayList<RtkPointBean>?=arrayListOf())