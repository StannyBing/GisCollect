package com.stanny.sketchpad.view

import android.content.Context
import android.graphics.*
import android.os.Vibrator
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import com.google.gson.Gson
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.bean.SketchPadLabelBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchLabelTool
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.SketchPointTool
import com.stanny.sketchpad.tool.algorithm.OffsetAlgorithm
import com.stanny.sketchpad.tool.algorithm.entity.Point
import com.zx.zxutils.util.*
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min


/**
 * 画板内容
 */
class SketchPadContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private var backgroundPaint = Paint()//背景画笔
    private var graphicPaint = Paint()//图形画笔

    private val scaleGestureDetector = ScaleGestureDetector(context, MyScalGestureListener())//双指
    private val gestureListener = GestureDetector(context, MyGestureListener())//单指

    private var contentScale = 1f//缩放比例
    private var scalePosX = 0f
    private var scalePosY = 0f
    private var contentTransX = 0f//X轴移动
    private var contentTransY = 0f//Y轴移动

    private var selectGraphic: SketchPadGraphicBean? = null//选中图形
    private var editGraphic: SketchPadGraphicBean? = null//编辑图形

    private var graphicList = arrayListOf<SketchPadGraphicBean>()

    private var drawSite = false //界址
    private var showMeters: Boolean = false//尺寸
    private var drawHighlight = false//是否高亮
    private var sketchPadFloorBean: SketchPadFloorBean? = null
    private var sitePoints = arrayListOf<PointF>()

    private var sketchLabelTool: SketchLabelTool

    init {
        setWillNotDraw(false)

        setBackgroundColor(Color.WHITE)

        //初始化背景画笔
        backgroundPaint.apply {
            style = Paint.Style.STROKE
            color = SketchPadConstant.backgroundGridColor
            strokeWidth = 1f
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(15f, 4f, 15f, 4f), 1f)
        }

        //初始化图形画笔
        graphicPaint.apply {
            style = Paint.Style.STROKE
            color = SketchPadConstant.graphicLineColor
            strokeWidth = SketchPadConstant.graphicLineWidth
            isAntiAlias = true
        }

        sketchLabelTool = SketchLabelTool(context, object : SketchLabelTool.LabelListener {
            override fun getContentTransX() = contentTransX
            override fun getContentTransY() = contentTransY
            override fun getGraphicList() = graphicList
            override fun refreshGraphic() {
                this@SketchPadContentView.refreshGraphic()
            }
        })
    }

    override fun onDraw(canvas: Canvas?) {
        //绘制网格
        drawBackground(canvas)
        //绘制绘图区域
        drawContent(canvas)
        super.onDraw(canvas)
    }

    /**
     * 绘制网格
     */
    private fun drawBackground(canvas: Canvas?) {
        //绘制竖线
        val widthCount = (width / SketchPadConstant.backgroundGridSpace).toInt()
        for (w in 0..widthCount) {
            canvas?.drawLine(
                w * SketchPadConstant.backgroundGridSpace,
                0f,
                w * SketchPadConstant.backgroundGridSpace,
                height.toFloat(),
                backgroundPaint
            )
        }
        //绘制横线
        val heightCount = (height / SketchPadConstant.backgroundGridSpace).toInt()
        for (h in 0..heightCount) {
            canvas?.drawLine(
                0f,
                h * SketchPadConstant.backgroundGridSpace,
                width.toFloat(),
                h * SketchPadConstant.backgroundGridSpace,
                backgroundPaint
            )
        }
    }

    /**
     * 绘制绘图区域
     */
    private fun drawContent(canvas: Canvas?) {
        canvas?.save()
        //        canvas?.scale(contentScale, contentScale, scalePosX, scalePosY)
        canvas?.scale(contentScale, contentScale, width / 2f, height / 2f)
        canvas?.translate(contentTransX, contentTransY)
        graphicList.forEach {
            it.drawGraphic(canvas, withMark = it.id == editGraphic?.id)
        }
        sketchLabelTool.labelList.forEach {
            it.drawLabel(canvas)
        }
        //绘制界址点
        if (drawSite) {
            var points = arrayListOf<PointF>()
            graphicList.forEach {
                it.points.forEachIndexed { index, pointF ->
                    points.add(PointF(pointF.x + it.offsetX, pointF.y + it.offsetY))
                }
            }
            drawSite(points, canvas)
        }
        //高亮显示
        if (drawHighlight) {
            graphicList.forEach {
                it.drawFill(canvas, SketchPadConstant.graphicTransparentColor)
            }
            sketchPadFloorBean?.sketchList?.forEach {
                it.drawFill(canvas)
            }
            //保存图层信息
            sketchPadFloorBean?.let {
                sketchPadListener?.saveFloor(it)
            }
        }
        canvas?.restore()
    }

    /**
     * 绘制界址点
     */
    private fun drawSite(points: ArrayList<PointF>, canvas: Canvas?) {
        sitePoints.clear()
        //求出界址点个数
        if (points.size >= 3) {
            for (index in 0 until points.size) {
                var point0 = if (index == 0) {
                    points[points.size - 1]
                } else {
                    points[index - 1]
                }
                var point1 = if (index == points.size - 1) {
                    points[0]
                } else {
                    points[index + 1]
                }

                val degree = SketchPointTool.excuteDegree(
                    points[index].x.toDouble(),
                    points[index].y.toDouble(),
                    point0.x.toDouble(),
                    point0.y.toDouble(),
                    point1.x.toDouble(),
                    point1.y.toDouble()
                )
                if (Math.abs(degree) != 0.0 && Math.abs(degree) != 180.0) {
                    sitePoints.add(points[index])
                }
            }
            val textPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                strokeWidth = 1f
                textSize = 20f
                this.color = SketchPadConstant.graphicSiteColor
            }
            var tempSite = arrayListOf<Point>()
            sitePoints.forEachIndexed { index, pointF ->
                tempSite.add(Point(pointF.x.toDouble(), pointF.y.toDouble()))
            }
            OffsetAlgorithm.offsetAlgorithm(tempSite, -20.0)?.apply {
                if (!isNullOrEmpty()) {
                    this[0].forEachIndexed { index, point ->
                        canvas?.drawText("J$index", point.x.toFloat(), point.y.toFloat(), textPaint)
                    }
                }
            }
        }
    }

    /**
     * 触摸事件
     * 包含普通触摸、双指缩放、单指移动、松手贴边
     */
    private var isTouchUp = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouchUp = false
                if (sketchLabelTool.drawLabel) {
                    return sketchLabelTool.excuteLabelDraw(event)
                }
                selectGraphic = null
                sketchLabelTool.selectLabel = null
                sketchLabelTool.excuteLabelTouch(event)
                graphicList.forEach {
                    if (it.isGraphicInTouch(event.x - contentTransX, event.y - contentTransY)) {
                        selectGraphic = it
                        var tempItem: SketchPadGraphicBean? = null
                        sketchPadFloorBean?.sketchList?.forEach {
                            if (it.id == selectGraphic!!.id) {
                                tempItem = it
                                return@forEach
                            }
                        }
                        if (tempItem == null) {
                            sketchPadFloorBean?.sketchList?.add(selectGraphic!!)
                        } else {
                            sketchPadFloorBean?.sketchList?.remove(tempItem!!)
                        }
                        if (drawHighlight) {
                            refreshGraphic()
                        }
                        return@forEach
                    }
                }


            }
//            MotionEvent.ACTION_MOVE -> {
//                if (isInsertGraphic) {
//                    return true
//                }
//            }
            MotionEvent.ACTION_UP -> {
                isTouchUp = true
                //自动贴边
                selectGraphic?.doAutoWeltPoint(graphicList)
                refreshGraphic()
//                if (isInsertGraphic) {
//                    insertGraphic(insertGraphic!!)
//                    return true
//                }
                return true
            }
        }
        //TODO 双指缩放 隐藏
//        selectGraphic == null && !scaleGestureDetector.onTouchEvent(event) && return true
        //单指移动 标注不允许移动
        if (!sketchLabelTool.drawLabel && !drawHighlight) !gestureListener.onTouchEvent(event) && return true
        return false
    }

    /**
     * 插入图形
     */
    fun insertGraphic(graphicBean: SketchPadGraphicBean) {
        graphicList.add(SketchPadGraphicBean(graphicBean.graphicType).apply {
            offsetX = -contentTransX + 50
            offsetY = -contentTransY + 50
            this.showMeters = this@SketchPadContentView.showMeters
        })
        invalidate()
    }

    /**
     * 关闭编辑
     */
    fun closeEdit() {
        editGraphic = null
        invalidate()
    }

    /**
     * 刷新图形列表
     */
    fun refreshGraphic() {
        invalidate()
    }

    /**
     * 重置到中央
     */
    fun resetCenter() {
        val center = SketchPointTool.getCenter(graphicList)
        contentTransX = width / 2 - center.x
        contentTransY = height / 2 - center.y
        invalidate()
    }

    /**
     * 绘制标注
     */
    fun drawLabel() {
        sketchLabelTool.drawLabel = true
        ZXToastUtil.showToast("点击画板添加标注")
    }

    /**
     * 展示界址
     */
    fun showSite(check: Boolean) {
        drawSite = check
        //判断此坐标集合的界址点个数
        invalidate()
    }


    /**
     * 显示尺寸
     */
    fun showSizeInfo(isCheck: Boolean) {
        this.showMeters = isCheck
        graphicList.forEach {
            it.showMeters = isCheck
        }
        refreshGraphic()
    }

    /**
     * 删除
     */
    fun deleteGraphic(id: UUID) {
        graphicList.removeAll { it.id == id }
        refreshGraphic()
    }

    /**
     * 楼层编辑
     */
    fun floorEdit(sketchPadFloorBean: SketchPadFloorBean) {
        this.sketchPadFloorBean = sketchPadFloorBean
        drawHighlight = true
        refreshGraphic()
    }

    /**
     * 关闭楼层编辑
     */
    fun stopFloorEdit() {
        this.sketchPadFloorBean = null
        drawHighlight = false
        refreshGraphic()
    }

    /**
     * 保存图形
     */
    fun saveGraphicInfo(callBack: () -> Unit) {
        if (graphicList.isNotEmpty()) {
            showSizeInfo(true)
            showSite(true)
            resetCenter()
            val minPoint = SketchPointTool.getDrawMin(graphicList, sketchLabelTool, contentTransX, contentTransY)
            val maxPoint = SketchPointTool.getDrawMax(graphicList, sketchLabelTool, contentTransX, contentTransY)
            val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            draw(Canvas(tempBitmap))
            val drawBitmap = Bitmap.createBitmap(
                tempBitmap,
                minPoint.x.toInt() - SketchPadConstant.backgroundGridSpace.toInt(),
                minPoint.y.toInt() - SketchPadConstant.backgroundGridSpace.toInt(),
                (maxPoint.x - minPoint.x).toInt() + SketchPadConstant.backgroundGridSpace.toInt() * 2,
                (maxPoint.y - minPoint.y).toInt() + SketchPadConstant.backgroundGridSpace.toInt() * 2
            )
            val ivDraw = ImageView(context)
            ivDraw.setImageBitmap(drawBitmap)
//            ZXDialogUtil.showCustomViewDialog(
//                context,
//                "",
//                ivDraw
//            ) { dialog: DialogInterface?, which: Int ->
//                ZXBitmapUtil.bitmapToFile(
//                    drawBitmap,
//                    File(ZXSystemUtil.getSDCardPath() + "test.jpg")
//                )
//            }
            val file = context.filesDir.path
            //ZXTimeUtil.getTime(System.currentTimeMillis(), SimpleDateFormat("yyyyMMdd_HHmmss"))
            val s = "$file/sketch/draw.jpg"
            try {
                Runnable {
                    drawBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        FileOutputStream(ZXFileUtil.createNewFile(s))
                    )
                }.run()
            } catch (e: FileNotFoundException) {

            }
            //ZXTimeUtil.getTime(System.currentTimeMillis(), SimpleDateFormat("yyyyMMdd_HHmmss"))
            ZXSharedPrefUtil().putString("graphicList", Gson().toJson(sitePoints))
            callBack()
        } else {
            ZXToastUtil.showToast("请绘制草图")
        }
    }

    /**
     * 单指移动事件
     */
    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (sketchLabelTool.selectLabel != null) {
                sketchLabelTool.selectLabel!!.offsetX -= distanceX / contentScale
                sketchLabelTool.selectLabel!!.offsetY -= distanceY / contentScale
            } else if (selectGraphic != null) {
                selectGraphic!!.offsetX -= distanceX / contentScale
                selectGraphic!!.offsetY -= distanceY / contentScale
            } else {
                contentTransX -= distanceX / contentScale
                contentTransY -= distanceY / contentScale
            }
//            }
            invalidate()
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onLongPress(event: MotionEvent) {
            if (isTouchUp){
                return
            }
            graphicList.forEach {
                if (it.isGraphicInTouch(event.x - contentTransX, event.y - contentTransY)) {
                    graphicList.forEach {
                        it.showMeters = false
                    }
                    editGraphic = it
                    sketchPadListener?.graphicEdit(editGraphic!!)
                    invalidate()
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(100L)
                    return
                }
            }
        }
    }

    /**
     * 双指缩放事件
     */
    private inner class MyScalGestureListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var lastScale = 1.0f
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastScale = contentScale
            scalePosX = detector.focusX
            scalePosY = detector.focusY
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            contentScale = lastScale * detector.scaleFactor
            contentScale = max(contentScale, 0.3f)
            contentScale = min(contentScale, 3f)
            invalidate()
            return super.onScale(detector)
        }
    }

}