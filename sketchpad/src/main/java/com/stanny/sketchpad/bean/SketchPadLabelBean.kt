package com.stanny.sketchpad.bean

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.SketchPointTool
import java.util.*

data class SketchPadLabelBean(var name: String, var pointF: PointF) {

    var offsetX: Float = 0f
    var offsetY: Float = 0f

    /**
     * 判断手指是否点击该标注
     * @param x
     * @param y
     * @return boolean
     */
    fun isLabelInTouch(x: Float, y: Float): Boolean {
        return SketchPointTool.isPtInPoly(
            PointF(x - offsetX, y - offsetY),
            arrayListOf<PointF>().apply {
                add(PointF(pointF.x - 50, pointF.y - 30))
                add(PointF(pointF.x + 50, pointF.y - 30))
                add(PointF(pointF.x + 50, pointF.y + 30))
                add(PointF(pointF.x - 50, pointF.y + 30))
            })
    }

    fun drawLabel(canvas: Canvas?) {
        val textPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 5f
            textSize = 30f
            this.color = SketchPadConstant.graphicMarkNumColor
        }
        val textBounds = Rect()
        textPaint.getTextBounds(name, 0, name.length, textBounds)
        canvas?.drawText(
            name,
            pointF.x + offsetX - textBounds.width() / 2,
            pointF.y + offsetY + textBounds.height() / 2,
            textPaint
        )
    }
}