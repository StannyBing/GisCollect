package com.stanny.sketchpad.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadLabelAdapter
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.bean.SketchPadLabelBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.algorithm.OffsetAlgorithm
import com.stanny.sketchpad.tool.algorithm.entity.Point
import com.zx.zxutils.util.*
import java.io.File
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
    private var selectLabel: SketchPadLabelBean? = null//选中标注

    private var graphicList = arrayListOf<SketchPadGraphicBean>()
    private var labelList = arrayListOf<SketchPadLabelBean>()

    private var drawLabel = false//开启标注绘制
    private var drawSite = false //界址
    private var showMeters: Boolean = false//尺寸
    private var drawHighlight = false//是否高亮
    private var sketchPadFloorBean: SketchPadFloorBean? = null
    private var sitePoints = arrayListOf<PointF>()

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

        initListener()
    }

    private fun initListener() {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
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
        labelList.forEach {
            it.drawLabel(canvas)
        }
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

                val degree = excuteDegree(
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
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (drawLabel) {
                    val labelPoint = PointF(event.x, event.y)
                    graphicList.forEach {
                        if (it.isGraphicContainsPoint(
                                event.x - contentTransX,
                                event.y - contentTransY
                            )
                        ) {
                            showInDialog(labelPoint, showInData())
                            return true
                        } else if (it.isGraphicContainsPoint(
                                event.x - 40,
                                event.y - 40
                            ) || it.isGraphicContainsPoint(event.x + 40, event.y + 40)
                        ) {
                            showInDialog(labelPoint, showBoundaryData())
                            return true
                        }
                    }
                    showOutDialog(labelPoint)
                    return true
                }
                selectGraphic = null
                selectLabel = null
                labelList.forEach {
                    if (it.isLabelInTouch(event.x - contentTransX, event.y - contentTransY)) {
                        selectLabel = it
                        return@forEach
                    }
                }
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
        if (!drawLabel && !drawHighlight) !gestureListener.onTouchEvent(event) && return true
        return false
    }

    private fun showInDialog(labelPoint: PointF, data: ArrayList<SketchLabelBean>) {
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SketchPadLabelAdapter(data).apply {
            addCheckedChangeListener {
                val list = data as ArrayList<SketchLabelBean>
                list.forEachIndexed { index, sketchLabelBean ->
                    if (sketchLabelBean == list[it]) {
                        content = sketchLabelBean.value
                        sketchLabelBean.isChecked = !sketchLabelBean.isChecked
                    } else {
                        sketchLabelBean.isChecked = false
                    }
                }
                notifyDataSetChanged()
            }
        }
        ZXDialogUtil.showCustomViewDialog(context, "", view, { dialog, which ->
            labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                offsetX = -contentTransX
                offsetY = -contentTransY
            })
            refreshGraphic()
            drawLabel = false
        }, { dialog, which -> }).apply {
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth() / 3
            layoutParams?.gravity = Gravity.RIGHT
            window?.attributes = layoutParams
        }
    }

    private fun showInData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "阳台"))
            add(SketchLabelBean("2", "内阳台"))
            add(SketchLabelBean("3", "砖湿"))
            add(SketchLabelBean("4", "砖瓦"))
            add(SketchLabelBean("5", "滴水"))
            add(SketchLabelBean("6", "猪圈"))
        }
    }

    private fun showOutData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "坝"))
            add(SketchLabelBean("2", "人行道"))
            add(SketchLabelBean("3", "水沟"))
            add(SketchLabelBean("4", "巷道"))
            add(SketchLabelBean("5", "林地"))
            add(SketchLabelBean("6", "耕地"))
        }
    }

    private fun showBoundaryData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "自墙"))
            add(SketchLabelBean("2", "共墙"))
            add(SketchLabelBean("3", "借墙"))
        }
    }

    private fun showOutDialog(labelPoint: PointF) {
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog, null)
        view.findViewById<EditText>(R.id.otherEt).apply {
            visibility = View.VISIBLE
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    content = s?.toString()?.trim() ?: ""
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SketchPadLabelAdapter(showOutData()).apply {
            addCheckedChangeListener {
                val list = data as ArrayList<SketchLabelBean>
                list.forEachIndexed { index, sketchLabelBean ->
                    if (sketchLabelBean == list[it]) {
                        content = sketchLabelBean.value
                        sketchLabelBean.isChecked = !sketchLabelBean.isChecked
                    } else {
                        sketchLabelBean.isChecked = false
                    }
                }
                notifyDataSetChanged()
            }
        }
        ZXDialogUtil.showCustomViewDialog(context, "", view, { dialog, which ->
            labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                offsetX = -contentTransX
                offsetY = -contentTransY
            })
            refreshGraphic()
            drawLabel = false
        }, { dialog, which -> }).apply {
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth() / 3
            layoutParams?.gravity = Gravity.RIGHT
            window?.attributes = layoutParams
        }
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
        val center = graphicList.getCenter()
        contentTransX = width / 2 - center.x
        contentTransY = height / 2 - center.y
        invalidate()
    }

    /**
     * 绘制标注
     */
    fun drawLabel() {
        drawLabel = true
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
            val minPoint = getDrawMin()
            val maxPoint = getDrawMax()
            val tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            draw(Canvas(tempBitmap))
            val drawBitmap = Bitmap.createBitmap(
                tempBitmap,
                contentTransX.toInt() - minPoint.x.toInt(),
                contentTransY.toInt() - minPoint.y.toInt(),
                (maxPoint.x - minPoint.x).toInt()*2,
                (maxPoint.y - minPoint.y).toInt()*2
            )
           /* val ivDraw = ImageView(context)
            ivDraw.setImageBitmap(drawBitmap)
            ZXDialogUtil.showCustomViewDialog(
                context,
                "",
                ivDraw
            ) { dialog: DialogInterface?, which: Int ->
                ZXBitmapUtil.bitmapToFile(
                    drawBitmap,
                    File(ZXSystemUtil.getSDCardPath() + "test.jpg")
                )
            }*/
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
     * 获取所有图形的中点
     */
    private fun ArrayList<SketchPadGraphicBean>.getCenter(): PointF {
        var maxX: Float? = null
        var maxY: Float? = null
        var minX: Float? = null
        var minY: Float? = null
        forEach { bean ->
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
    private fun getDrawMin(): PointF {
        var minX: Float? = null
        var minY: Float? = null
        graphicList.forEach { bean ->
            bean.points.forEach {
                minX = if (minX == null) (it.x + bean.offsetX) else min(minX!!, it.x + bean.offsetX)
                minY = if (minY == null) (it.y + bean.offsetY) else min(minY!!, it.y + bean.offsetY)
            }
        }
        labelList.forEach { bean ->
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
    }

    /**
     * 获取所有图形的左上角的点（x， y皆是最大点）
     */
    private fun getDrawMax(): PointF {
        var maxX: Float? = null
        var maxY: Float? = null
        graphicList.forEach { bean ->
            bean.points.forEach {
                maxX = if (maxX == null) (it.x + bean.offsetX) else max(maxX!!, it.x + bean.offsetX)
                maxY = if (maxY == null) (it.y + bean.offsetY) else max(maxY!!, it.y + bean.offsetY)
            }
        }
        labelList.forEach { bean ->
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
            if (selectLabel != null) {
                selectLabel!!.offsetX -= distanceX / contentScale
                selectLabel!!.offsetY -= distanceY / contentScale
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
            if (event.downTime == event.eventTime) {
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
}