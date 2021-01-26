package com.stanny.sketchpad.listener

import android.graphics.PointF
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import java.util.*

interface SketchPadListener {

    fun graphicInsert(graphicBean: SketchPadGraphicBean)

    fun graphicEdit(graphicBean: SketchPadGraphicBean)

    fun closeEdit()

    fun refreshGraphic()

    fun resetCenter()

    fun drawLabel()

    fun saveGraphicInfo()

    fun showSite(isCheck: Boolean)

    fun floorSetting(isCheck: Boolean)

    fun finish()

    fun showSizeInfo(checked: Boolean)

    fun deleteGraphic(id: UUID)
}