package com.stanny.sketchpad.tool

import android.graphics.Point
import android.graphics.PointF
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


object SketchPointTool {

    /**
     *
     * 判断点是否在多边形内
     *
     * @param point 检测点
     *
     * @param pts 多边形的顶点
     *
     * @return 点在多边形内返回true,否则返回false
     */
    fun isPtInPoly(
        point: PointF,
        pts: List<PointF>
    ): Boolean {
        val N = pts.size
        val boundOrVertex = true //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        var intersectCount = 0 //cross points count of x
        val precision = 2e-10 //浮点类型计算时候与0比较时候的容差
        var p1: PointF
        var p2: PointF //neighbour bound vertices
        val p: PointF = point //当前点
        p1 = pts[0] //left vertex
        for (i in 1..N) { //check all rays
            if (p.equals(p1)) {
                return boundOrVertex //p is an vertex
            }
            p2 = pts[i % N] //right vertex
            if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(
                    p1.x,
                    p2.x
                )
            ) { //ray is outside of our interests
                p1 = p2
                continue  //next ray left point
            }
            if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(
                    p1.x,
                    p2.x
                )
            ) { //ray is crossing over by the algorithm (common part of)
                if (p.y <= Math.max(p1.y, p2.y)) { //x is before of ray
                    if (p1.x == p2.x && p.y >= Math.min(
                            p1.y,
                            p2.y
                        )
                    ) { //overlies on a horizontal ray
                        return boundOrVertex
                    }
                    if (p1.y == p2.y) { //ray is vertical
                        if (p1.y == p.y) { //overlies on a vertical ray
                            return boundOrVertex
                        } else { //before ray
                            ++intersectCount
                        }
                    } else { //cross point on the left side
                        val xinters: Double =
                            ((p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y).toDouble() //cross point of y
                        if (Math.abs(p.y - xinters) < precision) { //overlies on a ray
                            return boundOrVertex
                        }
                        if (p.y < xinters) { //before ray
                            ++intersectCount
                        }
                    }
                }
            } else { //special case when ray is crossing through the vertex
                if (p.x == p2.x && p.y <= p2.y) { //p crossing over p2
                    val p3: PointF = pts[(i + 1) % N] //next vertex
                    if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(
                            p1.x,
                            p3.x
                        )
                    ) { //p.x lies between p1.x & p3.x
                        ++intersectCount
                    } else {
                        intersectCount += 2
                    }
                }
            }
            p1 = p2 //next ray left point
        }
        return intersectCount % 2 != 0
    }

    /**
     * 判断两条线是否平行
     */
    fun isLineParallel(poiA1: PointF, poiA2: PointF, poiB1: PointF, poiB2: PointF): Boolean {
        //这个判断条件是为了避免有一条直线平行于y轴，因为他们此时斜率无穷大，但是如果他们都平行y轴，说明他们也是平行的
        if (poiA1.x == poiA2.x || poiB1.x == poiB2.x) {
            if (poiA1.x == poiA2.x && poiB1.x == poiB2.x) {
                return true
            }
        } else {
            val p1 = (poiA2.y - poiA1.y) / (poiA2.x - poiA1.x)
            val p2 = (poiB2.y - poiB1.y) / (poiB2.x - poiB1.x)
            if (p1 == p2) {
                return true
            }
        }
        return false
    }

    /**
     * 计算点与直线的距离（只计算水平与竖直两个方向的贴边）
     */
    fun getLinesDistanceSimple(
        poiA1x: Float,
        poiA1y: Float,
        poiA2x: Float,
        poiA2y: Float,
        poiB1x: Float,
        poiB1y: Float,
        poiB2x: Float,
        poiB2y: Float,
        call: (Float?, Float?) -> Unit
    ) {
        if (poiA1x == poiA2x && poiB1x == poiB2x) {//垂直方向平行
            if (abs(poiA1y - poiA2y) + abs(poiB1y - poiB2y) > abs(
                    arrayListOf(
                        poiA1y,
                        poiA2y,
                        poiB1y,
                        poiB2y
                    ).max()!! - arrayListOf(poiA1y, poiA2y, poiB1y, poiB2y).min()!!
                )
            ) {//两条线有空间重叠部分
                call(poiA1x - poiB1x, null)
            }
        } else if (poiA1y == poiA2y && poiB1y == poiB2y) {//水平方向平行
            if (abs(poiA1x - poiA2x) + abs(poiB1x - poiB2x) > abs(
                    arrayListOf(
                        poiA1x,
                        poiA2x,
                        poiB1x,
                        poiB2x
                    ).max()!! - arrayListOf(poiA1x, poiA2x, poiB1x, poiB2x).min()!!
                )
            ) {//两条线有空间重叠部分
                call(null, poiA1y - poiB1y)
            }
        }
    }

    data class GraphicWeltBean(var type: WeltType, var distance: Float) {
        enum class WeltType {
            X, Y
        }
    }

    /**
     * 计算点与直线的距离（计算两条平行线的距离）
     */
    fun getLinesDistance(
        poiA1x: Float,
        poiA1y: Float,
        poiA2x: Float,
        poiA2y: Float,
        poiB1x: Float,
        poiB1y: Float
    ): Float {
        val ABx = poiA2x - poiA1x
        val ABy = poiA2y - poiA1y
        val APx = poiB1x - poiA1x
        val APy = poiB1y - poiA1y

        val AB_AP = ABx * APx + ABy * APy
        val distAB2 = ABx * ABx + ABy * ABy

        var Dx = poiA1x
        var Dy = poiA1y
        if (distAB2 != 0f) {
            val t = AB_AP / distAB2
            if (t >= 1f) {
                Dx = poiA2x
                Dy = poiA2y
            } else if (t > 0f) {
                Dx = poiA1x + ABx * t
                Dy = poiA1y + ABy * t
            } else {
                Dx = poiA1x
                Dy = poiA1y
            }
        }
        val PDx = Dx - poiB1x
        val PDy = Dy - poiB1y
        return sqrt((PDx * PDx + PDy * PDy).toDouble()).toFloat()
    }

    /**
     * 返回一个点是否在一个多边形区域内
     * @param points 多边形坐标点集合
     * @param point 待判点
     * @return true 多边形包含这个点,false多边形未包含这个点
     */
    fun isPolygonContainsPoint(point: PointF, points: List<PointF>): Boolean {
        var nCross = 0
        for (index in points.indices) {
            val point1 = points[index]
            val point2 = points[(index + 1) % points.size]
            //取多边形任意一个边，做点point的水平延长线，求解与当前边的交点个数
            //p1p2是水平线段，要么没有交点，要么无限个交点
            if (point1.y == point2.y) continue
            //point 在p1p2底部--无交点
            if (point.y < Math.min(point1.y, point2.y)) continue
            //point在p1p2顶部--无交点
            if (point.y >= Math.max(point1.y, point2.y)) continue
            // point点水平线与当前p1p2交点的x坐标
            val x =
                ((point.y - point1.y)) * (point2.x - point2.x) / (point2.y - point1.y) + point1.x
            if (x > point.x) {//当x=point.x时，说明point在p1p2线段上
                nCross++ //只统计单边交点
            }
        }
        //单边交点为偶数，点在多边形之外
        return (nCross % 2 == 1)
    }

    /**
     * 返回一个点是否在一个多边形边界上
     * @param points 多边形坐标点集合
     * @param point 待判断点
     * @return true 点在多边形边上 false 点不在多边形边上
     */

    fun isPointInPolygonBoundary(point: PointF, points: List<PointF>): Boolean {
        for (index in points.indices) {
            val point1 = points[index]
            val point2 = points[(index + 1) % points.size]
            //point 在p1p2底部--无交点
            if (point.y < Math.min(point1.y, point2.y)) continue
            //point在p1p2顶部--无交点
            if (point.y >= Math.max(point1.y, point2.y)) continue
            //p1p2是水平线段,要么没有交点,要么无限个交点
            if (point1.y == point2.y) {
                val min = Math.min(point1.x, point2.x)
                val max = Math.max(point1.x, point2.x)
                //point 在水平线段p1p2上，直接return true
                if ((point.y == point1.y) && (point.x in min..max)) {
                    return true
                }
            } else { //求解交点
                val x =
                    (point.y - point1.y) * (point2.x - point1.x) / (point2.y - point1.y) + point1.x
                if (x == point.x) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 多边形外扩
     */
    fun polygonExpansion(points: List<PointF>, distance: Float): ArrayList<PointF> {
        var tempPoints = arrayListOf<PointF>()
        val range = points.size
        for (i in 0 until range) {
            var point = points[i] //p点
            var point1 = points[if (i == 0) range - 1 else i - 1]//p1点
            var point2 = points[if (i == range - 1) 0 else i + 1]//p2点
            //向量pp1
            var vectorX1 = point1.x - point.x //向量pp1横坐标
            var vectorY1 = point1.y - point.y//向量Pp1纵坐标
            var n1 = normalize(vectorX1.toDouble(), vectorY1.toDouble())
            var vectorUnitX1 = vectorX1 / n1 //向量单位化横坐标
            var vectorUnitY1 = vectorY1 / n1 //向量单位化纵坐标
            //向量pp2
            var vectorX2 = point2.x - point.x //向量pp1横坐标
            var vectorY2 = point2.y - point.y//向量Pp1纵坐标
            var n2 = normalize(vectorX2.toDouble(), vectorY2.toDouble())
            var vectorUnitX2 = vectorX2 / n2 //向量单位化横坐标
            var vectorUnitY2 = vectorY2 / n2 //向量单位化纵坐标
            //pq距离
            var vectorLen =
                Math.sqrt((1 - ((vectorUnitX1 * vectorUnitX2) + (vectorUnitY1 * vectorUnitY2)))) * distance
            //根据向量的叉乘积来判断角是凹角还是凸角
            if (((vectorX1 * vectorY2) + (-1 * vectorY1 * vectorX2)) < 0) {
                vectorUnitX2 *= -1
                vectorUnitY2 *= -1
                vectorUnitX1 *= -1
                vectorUnitY1 *= -1
            }
            //PQ的方向
            var vectorX = vectorUnitX1 + vectorUnitX2
            var vectorY = vectorUnitY1 + vectorUnitY2
            var n = vectorLen / normalize(vectorX, vectorY)
            var vectorUnitX = vectorX * n
            var vectorUnitY = vectorY * n
            tempPoints.add(
                PointF(
                    (vectorUnitX + point.x).toFloat(),
                    (vectorUnitY + point.y).toFloat()
                )
            )
        }
        return tempPoints
    }

    /**
     * 向量单位化处理
     */
    private fun normalize(x: Double, y: Double): Double {
        return Math.sqrt((x * x) + (y * y))
    }

    /**
     * 计算角度
     */
    fun excuteDegree(
        vertexPointX: Double,
        vertexPointY: Double,
        point0X: Double,
        point0Y: Double,
        point1X: Double,
        point1Y: Double
    ): Double {
        //向量的点乘
        val vector =
            (point0X - vertexPointX) * (point1X - vertexPointX) + (point0Y - vertexPointY) * (point1Y - vertexPointY)
        //向量的模乘
        var sqrt = Math.sqrt(
            (Math.abs((point0X - vertexPointX) * (point0X - vertexPointX)) + Math.abs((point0Y - vertexPointY) * (point0Y - vertexPointY))) * (
                    Math.abs((point1X - vertexPointX) * (point1X - vertexPointX)) + Math.abs((point1Y - vertexPointY) * (point1Y - vertexPointY)))
        )
        //反余弦计算弧度
        var radian = Math.acos(vector / sqrt)
        //弧度转角度制
        val cross =
            (point1X - vertexPointX) * (point0Y - vertexPointY) - (point0X - vertexPointX) * (point1Y - vertexPointY)
        if (cross < 0) {
            return -(180 * radian / Math.PI).toDouble()
        } else {
            return (180 * radian / Math.PI).toDouble()
        }
    }

    /**
     * 获取所有图形的中点
     */
    fun getCenter(graphicList: ArrayList<SketchPadGraphicBean>): PointF {
        var maxX: Float? = null
        var maxY: Float? = null
        var minX: Float? = null
        var minY: Float? = null
        graphicList.forEach { bean ->
            bean.points.forEach {
                maxX = if (maxX == null) (it.x + bean.offsetX) else max(maxX!!, it.x + bean.offsetX)
                maxY = if (maxY == null) (it.y + bean.offsetY) else max(maxY!!, it.y + bean.offsetY)
                minX = if (minX == null) (it.x + bean.offsetX) else min(minX!!, it.x + bean.offsetX)
                minY = if (minY == null) (it.y + bean.offsetY) else min(minY!!, it.y + bean.offsetY)
            }
        }
        if (maxX == null || maxY == null || minX == null || minY == null) {
            return PointF(0f, 0f)
        }
        return PointF((maxX!! + minX!!) / 2, (maxY!! + minY!!) / 2)
    }

    /**
     * 获取所有图形的左上角的点（x， y皆是最小点）
     */
    fun getDrawMin(
        graphicList: ArrayList<SketchPadGraphicBean>,
        sketchLabelTool: SketchLabelTool,
        contentTransX: Float,
        contentTransY: Float
    ): PointF {
        var minX: Float? = null
        var minY: Float? = null
        graphicList.forEach { bean ->
            bean.points.forEach {
                minX = if (minX == null) (it.x + bean.offsetX) else min(minX!!, it.x + bean.offsetX)
                minY = if (minY == null) (it.y + bean.offsetY) else min(minY!!, it.y + bean.offsetY)
            }
        }
        sketchLabelTool.labelList.forEach { bean ->
            minX = if (minX == null) (bean.pointF.x + bean.offsetX) else min(
                minX!!,
                bean.pointF.x + bean.offsetX
            )
            minY = if (minY == null) (bean.pointF.y + bean.offsetY) else min(
                minY!!,
                bean.pointF.y + bean.offsetY
            )
        }
        if (minX == null || minY == null) {
            return PointF(0f, 0f)
        }
        return PointF(minX!! + contentTransX, minY!! + contentTransY)
//        return PointF(minX!!, minY!!)
    }

    /**
     * 获取所有图形的左上角的点（x， y皆是最大点）
     */
    fun getDrawMax(
        graphicList: ArrayList<SketchPadGraphicBean>,
        sketchLabelTool: SketchLabelTool,
        contentTransX: Float,
        contentTransY: Float
    ): PointF {
        var maxX: Float? = null
        var maxY: Float? = null
        graphicList.forEach { bean ->
            bean.points.forEach {
                maxX = if (maxX == null) (it.x + bean.offsetX) else max(maxX!!, it.x + bean.offsetX)
                maxY = if (maxY == null) (it.y + bean.offsetY) else max(maxY!!, it.y + bean.offsetY)
            }
        }
        sketchLabelTool.labelList.forEach { bean ->
            maxX = if (maxX == null) (bean.pointF.x + bean.offsetX) else max(
                maxX!!,
                bean.pointF.x + bean.offsetX
            )
            maxY = if (maxY == null) (bean.pointF.y + bean.offsetY) else max(
                maxY!!,
                bean.pointF.y + bean.offsetY
            )
        }
        if (maxX == null || maxY == null) {
            return PointF(0f, 0f)
        }
        return PointF(maxX!! + contentTransX, maxY!! + contentTransY)
//        return PointF(maxX!!, maxY!!)
    }
}