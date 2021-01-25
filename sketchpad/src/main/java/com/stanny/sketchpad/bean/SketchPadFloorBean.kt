package com.stanny.sketchpad.bean

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import com.stanny.sketchpad.tool.SketchPadConstant

data class SketchPadFloorBean(var id:String="",var name:String="",var area:String="",var sketchPadGraphicBean: SketchPadGraphicBean?=null,var isChecked:Boolean=false){
    /**
     * 绘制图形填充颜色
     * @param canvas 画笔
     * @param color 颜色
     */
    fun drawFill(canvas: Canvas?, @ColorInt color: Int= SketchPadConstant.graphicFillColor){
        //绘制图形
        val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            this.color =color
            isAntiAlias = true
        }
        val path = Path().apply {
            val points = sketchPadGraphicBean?.points
            val offsetX = sketchPadGraphicBean?.offsetX ?: 0f
            val offsetY= sketchPadGraphicBean?.offsetY?:0f
            points?.forEachIndexed { index, it ->
                if (index == 0) {
                    moveTo(it.x + offsetX, it.y + offsetY)
                } else {
                    lineTo(it.x + offsetX, it.y + offsetY)
                }
            }
            close()
        }
        canvas?.drawPath(path, fillPaint)
    }
}