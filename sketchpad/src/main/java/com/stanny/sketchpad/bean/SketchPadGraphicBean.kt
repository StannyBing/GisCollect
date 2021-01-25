package com.stanny.sketchpad.bean

import android.graphics.*
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.stanny.sketchpad.R
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.SketchPointTool
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXToastUtil
import java.util.*
import kotlin.math.*

/**
 * 图形构造类
 */
data class SketchPadGraphicBean(var graphicType: GraphicType) {

    enum class GraphicType {
        RECTANGE,//矩形
        TRAPEZIUM_L,//不规则形状1 L形
        TRAPEZIUM_AO,//不规则形状2 凹型
        TRAPEZIUM_TU,//不规则形状3 凸型
        SQUARE,//正方形
        TRIANGLE,//三角形
        CIRCLE,//圆形
        TRAPEZIUM//不规则四边形
    }

    val id = UUID.randomUUID()

    val points = arrayListOf<PointF>()//点集
    @DrawableRes
    var thumbnail: Int = 0
    var offsetX: Float = 0f
    var offsetY: Float = 0f

    //    private val markArr = arrayOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩")
    private val markArr = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    var showMeters = false//显示房屋长度/米

    init {
        setGraphicInfo(arrayListOf())
    }

    fun drawSite(canvas: Canvas?){
        val textPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = 5f
            textSize = 30f
            this.color = SketchPadConstant.graphicSiteColor
        }
        points.forEachIndexed { index, pointF ->
            canvas?.drawText("JF${index+1}",pointF.x+offsetX,pointF.y+offsetY,textPaint)
        }
    }




    /**
     * 根据类型绘制图形
     * @param canvas 画笔
     * @param color 颜色
     */
    fun drawGraphic(
        canvas: Canvas?, @ColorInt color: Int = SketchPadConstant.graphicLineColor,
        withMark: Boolean = false
    ) {
        //绘制图形
        val graphicPaint = Paint().apply {
            style = Paint.Style.STROKE
            this.color = if (withMark) SketchPadConstant.graphicHightLightColor else color
            strokeWidth = SketchPadConstant.graphicLineWidth
            isAntiAlias = true
            textSize = 20f
        }
        when (graphicType) {
            GraphicType.RECTANGE, GraphicType.SQUARE, GraphicType.TRAPEZIUM_L, GraphicType.TRAPEZIUM_AO, GraphicType.TRAPEZIUM_TU -> {
                val path = Path().apply {
                    points.forEachIndexed { index, it ->
                        if (index == 0) {
                            moveTo(it.x + offsetX, it.y + offsetY)
                        } else {
                            lineTo(it.x + offsetX, it.y + offsetY)
                        }
                    }
                    close()
                }
                canvas?.drawPath(path, graphicPaint)

            }
        }
        //绘制标记
        if (withMark) {
            points.forEachIndexed { index, it ->
                if (graphicType == GraphicType.RECTANGE && index >= 2) {
                    return@forEachIndexed
                }
                if (graphicType == GraphicType.SQUARE && index >= 1) {
                    return@forEachIndexed
                }
                val lastIndex = if (index + 1 < points.size) {
                    index + 1
                } else {
                    0
                }
                val textPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    strokeWidth = 5f
                    textSize = 30f
                    this.color = SketchPadConstant.graphicMarkNumColor
                }
                val textBounds = Rect()
                textPaint.getTextBounds(markArr[index], 0, markArr[index].length, textBounds)
                canvas?.drawText(
                    markArr[index],
                    (points[index].x + points[lastIndex].x) / 2 + offsetX - textBounds.width() / 2,
                    (points[index].y + points[lastIndex].y) / 2 + offsetY + textBounds.height() / 2,
                    textPaint
                )
            }
        }
        //显示米
        if (showMeters) {
            val meters = getGraphicMetreList()
            points.forEachIndexed { index, it ->
                val lastIndex = if (index + 1 < points.size) {
                    index + 1
                } else {
                    0
                }
                val textPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    strokeWidth = 5f
                    textSize = 30f
                    this.color = SketchPadConstant.graphicMarkNumColor
                }
                val textBounds = Rect()
//                textPaint.getTextBounds(meters[index], 0, meters[index].length, textBounds)
            }
        }
    }

    /**
     * 判断手指是否点击该图形
     * @param x
     * @param y
     * @return boolean
     */
    fun isGraphicInTouch(x: Float, y: Float): Boolean {
        return SketchPointTool.isPtInPoly(PointF(x - offsetX, y - offsetY), points)
    }

    /**
     * 判断点是否在图形内
     */
    fun isGraphicContainsPoint(x:Float,y: Float):Boolean{
        return SketchPointTool.isPolygonContainsPoint(PointF(x-offsetX,y-offsetY),points)
    }

    /**
     * 获取图形的边长列表（米）
     */
    fun getGraphicMetreList(): List<Float> {
        val metreList = arrayListOf<Float>()
        points.forEachIndexed { index, it ->
            if (graphicType == GraphicType.RECTANGE && index >= 2) {
                return@forEachIndexed
            }
            if (graphicType == GraphicType.SQUARE && index >= 1) {
                return@forEachIndexed
            }
            val lastIndex = if (index + 1 < points.size) {
                index + 1
            } else {
                0
            }
            metreList.add(
                sqrt(
                    ((points[index].x - points[lastIndex].x).toDouble().pow(2.0)
                            + (points[index].y - points[lastIndex].y).toDouble().pow(2.0))
                ).toFloat() / SketchPadConstant.backgroundGridSpace * SketchPadConstant.graphicRatio
            )
        }
        return metreList
    }

    /**
     * 设置图形数据
     */
    fun setGraphicInfo(graphicMetres: List<Float>) {
        when (graphicType) {
            GraphicType.RECTANGE -> {
                initRectange(graphicMetres)
            }
            GraphicType.SQUARE -> {
                initSquare(graphicMetres)
            }
            GraphicType.TRAPEZIUM_L -> {
                initTrapeziumL(graphicMetres)
            }
            GraphicType.TRAPEZIUM_AO -> {
                initTrapeziumAO(graphicMetres)
            }
            GraphicType.TRAPEZIUM_TU -> {
                initTrapeziumTU(graphicMetres)
            }
        }
    }

    /**
     * 初始化矩形数据(如不修改请勿传，-1即为保持上一次)
     * @param widthMetre 宽：单位米
     * @param heightMetre 高：单位米
     */
    private fun initRectange(graphicMetres: List<Float>) {
        thumbnail = R.drawable.icon_graphic_rectangle
        val widthMetre = graphicMetres.getMetre(0)
        val heightMetre = graphicMetres.getMetre(1)
        if (points.isEmpty()) {
            points.add(PointF(0f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 6f.toPx()))
            points.add(PointF(0f.toPx(), 6f.toPx()))
        } else {
            if (widthMetre != -1f) {
                points[1].x = widthMetre.toPx()
                points[2].x = widthMetre.toPx()
            }
            if (heightMetre != -1f) {
                points[2].y = heightMetre.toPx()
                points[3].y = heightMetre.toPx()
            }
        }
    }

    /**
     * 初始化正方形数据
     * @param widthMetre 边长
     */
    private fun initSquare(graphicMetres: List<Float>) {
        val widthMetre = graphicMetres.getMetre(0)
        thumbnail = R.drawable.icon_graphic_square
        if (points.isEmpty()) {
            points.add(PointF(0f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 8f.toPx()))
            points.add(PointF(0f.toPx(), 8f.toPx()))
        } else {
            if (widthMetre != -1f) {
                points[1].x = widthMetre.toPx()
                points[2].x = widthMetre.toPx()
                points[2].y = widthMetre.toPx()
                points[3].y = widthMetre.toPx()
            }
        }
    }

    /**
     * 初始化L型(不规则形状按边数设置，从左上角顺时针开始)
     *
     */
    private fun initTrapeziumL(graphicMetres: List<Float>) {
        thumbnail = R.drawable.icon_graphic_trapezium_l
        val line1Metre = graphicMetres.getMetre(0)
        val line2Metre = graphicMetres.getMetre(1)
        val line3Metre = graphicMetres.getMetre(2)
        val line4Metre = graphicMetres.getMetre(3)
        val line5Metre = graphicMetres.getMetre(4)
        val line6Metre = graphicMetres.getMetre(5)
        if (points.isEmpty()) {
            points.add(PointF(0f.toPx(), 0f.toPx()))
            points.add(PointF(4f.toPx(), 0f.toPx()))
            points.add(PointF(4f.toPx(), 4f.toPx()))
            points.add(PointF(8f.toPx(), 4f.toPx()))
            points.add(PointF(8f.toPx(), 8f.toPx()))
            points.add(PointF(0f.toPx(), 8f.toPx()))
        } else {
            val line1Change =
                if (line1Metre == -1f) 0f else (line1Metre.toPx() - (points[1].x - points[0].x))
            val line2Change =
                if (line2Metre == -1f) 0f else (line2Metre.toPx() - (points[2].y - points[1].y))
            val line3Change =
                if (line3Metre == -1f) 0f else (line3Metre.toPx() - (points[3].x - points[2].x))
            val line4Change =
                if (line4Metre == -1f) 0f else (line4Metre.toPx() - (points[4].y - points[3].y))
            val line5Change =
                if (line5Metre == -1f) 0f else (line5Metre.toPx() - (points[4].x - points[0].x))
            val line6Change =
                if (line6Metre == -1f) 0f else (line6Metre.toPx() - (points[5].y - points[0].y))

            points[1].x += line1Change
            points[2].x += line1Change
            points[2].y += line2Change
            points[3].x += line1Change + line3Change + line5Change
            points[3].y += line2Change
            points[4].x += line1Change + line3Change + line5Change
            points[4].y += line2Change + line4Change + line6Change
            points[5].y += line2Change + line4Change + line6Change
        }
    }

    /**
     * 初始化凹型(不规则形状按边数设置，从左上角顺时针开始)
     *
     */
    private fun initTrapeziumAO(graphicMetres: List<Float>) {
        thumbnail = R.drawable.icon_graphic_trapezium_ao
        val line1Metre = graphicMetres.getMetre(0)
        val line2Metre = graphicMetres.getMetre(1)
        val line3Metre = graphicMetres.getMetre(2)
        val line4Metre = graphicMetres.getMetre(3)
        val line5Metre = graphicMetres.getMetre(4)
        val line6Metre = graphicMetres.getMetre(5)
        val line7Metre = graphicMetres.getMetre(6)
        val line8Metre = graphicMetres.getMetre(7)
        if (points.isEmpty()) {
            points.add(PointF(0f.toPx(), 0f.toPx()))
            points.add(PointF(2.5f.toPx(), 0f.toPx()))
            points.add(PointF(2.5f.toPx(), 3f.toPx()))
            points.add(PointF(5.5f.toPx(), 3f.toPx()))
            points.add(PointF(5.5f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 0f.toPx()))
            points.add(PointF(8f.toPx(), 6f.toPx()))
            points.add(PointF(0f.toPx(), 6f.toPx()))
        } else {
            val line1Change =
                if (line1Metre == -1f) 0f else (line1Metre.toPx() - (points[1].x - points[0].x))
            val line2Change =
                if (line2Metre == -1f) 0f else (line2Metre.toPx() - (points[2].y - points[1].y))
            val line3Change =
                if (line3Metre == -1f) 0f else (line3Metre.toPx() - (points[3].x - points[2].x))
            val line4Change =
                if (line4Metre == -1f) 0f else (line4Metre.toPx() - (points[3].y - points[4].y))
            val line5Change =
                if (line5Metre == -1f) 0f else (line5Metre.toPx() - (points[5].x - points[4].x))
            val line6Change =
                if (line6Metre == -1f) 0f else (line6Metre.toPx() - (points[6].y - points[5].y))
            val line7Change =
                if (line7Metre == -1f) 0f else (line7Metre.toPx() - (points[6].x - points[0].x))
            val line8Change =
                if (line8Metre == -1f) 0f else (line8Metre.toPx() - (points[7].y - points[0].y))

            points[1].x += line1Change
            points[2].x += line1Change
            points[2].y += max(line2Change, line4Change)
            points[3].x += line1Change + line3Change
            points[3].y += max(line2Change, line4Change)
            points[4].x += line1Change + line3Change
            points[5].x += line1Change + line3Change + line5Change + line7Change
            points[6].x += line1Change + line3Change + line5Change + line7Change
            points[6].y += max(line6Change, line8Change)//凹陷部位不影响整体高度
            points[7].y += max(line6Change, line8Change)//凹陷部位不影响整体高度
        }
    }

    /**
     * 初始化凸型(不规则形状按边数设置，从左上角顺时针开始)
     *
     */
    private fun initTrapeziumTU(graphicMetres: List<Float>) {
        thumbnail = R.drawable.icon_graphic_trapezium_tu
        val line1Metre = graphicMetres.getMetre(0)
        val line2Metre = graphicMetres.getMetre(1)
        val line3Metre = graphicMetres.getMetre(2)
        val line4Metre = graphicMetres.getMetre(3)
        val line5Metre = graphicMetres.getMetre(4)
        val line6Metre = graphicMetres.getMetre(5)
        val line7Metre = graphicMetres.getMetre(6)
        val line8Metre = graphicMetres.getMetre(7)
        if (points.isEmpty()) {
            points.add(PointF(0f.toPx(), 3f.toPx()))
            points.add(PointF(2.5f.toPx(), 3f.toPx()))
            points.add(PointF(2.5f.toPx(), 0f.toPx()))
            points.add(PointF(5.5f.toPx(), 0f.toPx()))
            points.add(PointF(5.5f.toPx(), 3f.toPx()))
            points.add(PointF(8f.toPx(), 3f.toPx()))
            points.add(PointF(8f.toPx(), 6f.toPx()))
            points.add(PointF(0f.toPx(), 6f.toPx()))
        } else {
            val line1Change =
                if (line1Metre == -1f) 0f else (line1Metre.toPx() - (points[1].x - points[0].x))
            val line2Change =
                if (line2Metre == -1f) 0f else (line2Metre.toPx() - (points[1].y - points[2].y))
            val line3Change =
                if (line3Metre == -1f) 0f else (line3Metre.toPx() - (points[3].x - points[2].x))
            val line4Change =
                if (line4Metre == -1f) 0f else (line4Metre.toPx() - (points[4].y - points[3].y))
            val line5Change =
                if (line5Metre == -1f) 0f else (line5Metre.toPx() - (points[5].x - points[4].x))
            val line6Change =
                if (line6Metre == -1f) 0f else (line6Metre.toPx() - (points[6].y - points[5].y))
            val line7Change =
                if (line7Metre == -1f) 0f else (line7Metre.toPx() - (points[6].x - points[0].x))
            val line8Change =
                if (line8Metre == -1f) 0f else (line8Metre.toPx() - (points[7].y - points[0].y))

            points[1].x += line1Change
            points[2].x += line1Change
            points[2].y -= max(line2Change, line4Change)//向上变化
            points[3].x += line1Change + line3Change
            points[3].y -= max(line2Change, line4Change)//向上变化
            points[4].x += line1Change + line3Change
            points[5].x += line1Change + line3Change + line5Change + line7Change
            points[6].x += line1Change + line3Change + line5Change + line7Change
            points[6].y += max(line6Change, line8Change)
            points[7].y += max(line6Change, line8Change)
        }
    }

    fun doAutoWeltPoint(graphicList: List<SketchPadGraphicBean>): Boolean {
        val nearlyList = arrayListOf<Pair<SketchPadGraphicBean, Float>>()
        var weltX: Float? = null
        var weltY: Float? = null
        graphicList.forEach { temp ->
            if (temp.id != this.id) {
                temp.points.forEachIndexed { index1, point1 ->
                    val lastIndex1 = if (index1 == temp.points.size - 1) 0 else index1 + 1
                    this.points.forEachIndexed { index2, point2 ->
                        val lastIndex2 = if (index2 == this.points.size - 1) 0 else index2 + 1
//                        //判断是否平行
//                        if (SketchPointTool.isLineParallel(
//                                temp.points[index1],
//                                temp.points[lastIndex1],
//                                this.points[index2],
//                                this.points[lastIndex2]
//                            )
//                        ) {
                        //如果平行线之间的距离小于自动贴边阈值
                        SketchPointTool.getLinesDistanceSimple(
                            temp.points[index1].x + temp.offsetX,
                            temp.points[index1].y + temp.offsetY,
                            temp.points[lastIndex1].x + temp.offsetX,
                            temp.points[lastIndex1].y + temp.offsetY,
                            this.points[index2].x + this.offsetX,
                            this.points[index2].y + this.offsetY,
                            this.points[lastIndex2].x + this.offsetX,
                            this.points[lastIndex2].y + this.offsetY
                        ) { x, y ->
                            if (x != null && abs(x) < SketchPadConstant.autoWeltLimit) {
                                weltX = if (weltX != null && abs(weltX!!) < abs(x)) weltX else x
                            }
                            if (y != null && abs(y) < SketchPadConstant.autoWeltLimit) {
                                weltY = if (weltY != null && abs(weltY!!) < abs(y)) weltY else y
                            }
                        }
//                        if (distance != null && abs(distance) < SketchPadConstant.autoWeltLimit) {
//                            //添加进自动贴边列表
//                            nearlyList.add(temp to distance)
//                        }
                    }
//                    }
                }
            }
        }
        this.points.forEach {
            if (weltX != null) it.x += weltX!!
            if (weltY != null) it.y += weltY!!
        }

//        nearlyList.sortBy {
//            it.second
//        }
//        nearlyList.reverse()
//        nearlyList.forEach {
//            ZXLogUtil.loge("distance : ${it.second}")
//        }
        return nearlyList.isNotEmpty()
    }

    /**
     * 米数计算像素
     */
    private fun Float.toPx(): Float {
        return this * SketchPadConstant.backgroundGridSpace / SketchPadConstant.graphicRatio
    }

    /**
     * 获取数据
     */
    private fun List<Float>.getMetre(index: Int): Float {
        if (index >= size) {
            return -1f
        } else {
            return get(index)
        }
    }
}
