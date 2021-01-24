package com.stanny.sketchpad.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadLabelAdapter
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.bean.SketchPadLabelBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchPadConstant
import com.stanny.sketchpad.tool.SketchPointTool
import com.zx.zxutils.util.*
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
        canvas?.restore()
    }

    /**
     * 触摸事件
     * 包含普通触摸、双指缩放、单指移动、松手贴边
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (drawLabel) {
                    //TODO 替换成可编辑标注内容的弹窗，单独提出去作为一个方法
                    val labelPoint = PointF(event.x, event.y)
                    graphicList.forEach {
                        if (it.isGraphicInTouch(event.x - contentTransX, event.y - contentTransY)) {
                            showInDialog(labelPoint)
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
//                        val vibrator =
//                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                        vibrator.vibrate(50L)
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
            }
        }
        //TODO 双指缩放 隐藏
//        selectGraphic == null && !scaleGestureDetector.onTouchEvent(event) && return true
        //单指移动
        !gestureListener.onTouchEvent(event) && return true
        return false
    }
    private fun showInDialog(labelPoint:PointF){
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog,null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SketchPadLabelAdapter(showInData()).apply {
            addCheckedChangeListener {
                val list = data as ArrayList<SketchLabelBean>
                content = list[it].value
            }
        }
            ZXDialogUtil.showCustomViewDialog(context,"",view,{dialog, which ->
                labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                    offsetX = -contentTransX
                    offsetY = -contentTransY
                })
                refreshGraphic()
                drawLabel = false
            },{dialog, which ->  }).apply {
                val layoutParams = window?.attributes
                layoutParams?.width = ZXScreenUtil.getScreenWidth()/3
                layoutParams?.gravity = Gravity.RIGHT
                window?.attributes = layoutParams
            }
    }

    private fun showInData():ArrayList<SketchLabelBean>{
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1","阳台"))
            add(SketchLabelBean("2","内阳台"))
            add(SketchLabelBean("3","砖湿"))
            add(SketchLabelBean("4","砖瓦"))
            add(SketchLabelBean("5","滴水"))
            add(SketchLabelBean("6","猪圈"))
        }
    }

    private fun showOutData():ArrayList<SketchLabelBean>{
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1","坝"))
            add(SketchLabelBean("2","人行道"))
            add(SketchLabelBean("3","水沟"))
            add(SketchLabelBean("4","巷道"))
            add(SketchLabelBean("5","林地"))
            add(SketchLabelBean("6","耕地"))
        }
    }

    private fun showOutDialog(labelPoint:PointF){
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog,null)
        view.findViewById<EditText>(R.id.otherEt).apply {
            visibility=View.VISIBLE
            addTextChangedListener(object :TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    content = s?.toString()?.trim()?:""
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
                content = list[it].value
            }
        }
        ZXDialogUtil.showCustomViewDialog(context,"",view,{dialog, which ->
            labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                offsetX = -contentTransX
                offsetY = -contentTransY
            })
            refreshGraphic()
            drawLabel = false
        },{dialog, which ->  }).apply {
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth()/3
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
     * 获取当前图形
     */
    fun getCurrentGraphic(pointF: PointF): SketchPadGraphicBean? {
        var sketchPadGraphicBean: SketchPadGraphicBean? = null
        graphicList.forEach {
            if (it.isGraphicInTouch(pointF.x - contentTransX, pointF.y - contentTransY)) {
                sketchPadGraphicBean = it
                return@forEach
            }
        }
        return sketchPadGraphicBean
    }

    /**
     * 保存图形
     */
    fun saveGraphicInfo(callBack: () -> Unit) {
        val minPoint = graphicList.getMin()
        val maxPoint = graphicList.getMax()
        val viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(viewBitmap))
//        val divider = SketchPadConstant.backgroundGridSpace
//        val cutBitmap = Bitmap.createBitmap(
//            viewBitmap,
//            (minPoint.x - divider).toInt(),
//            (minPoint.y - divider).toInt(),
//            (maxPoint.x - minPoint.x + divider * 2).toInt(),
//            (maxPoint.y - minPoint.y + divider * 2).toInt()
//        )

        callBack()
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
    private fun ArrayList<SketchPadGraphicBean>.getMin(): PointF {
        var minX: Float? = null
        var minY: Float? = null
        forEach { bean ->
            bean.points.forEach {
                minX = if (minX == null) (it.x + bean.offsetX) else min(minX!!, it.x + bean.offsetX)
                minY = if (minY == null) (it.y + bean.offsetY) else min(minY!!, it.y + bean.offsetY)
            }
        }
        if (minX == null || minY == null) {
            return PointF(0f, 0f)
        }
        return PointF(minX!!, minY!!)
    }

    /**
     * 获取所有图形的左上角的点（x， y皆是最大点）
     */
    private fun ArrayList<SketchPadGraphicBean>.getMax(): PointF {
        var maxX: Float? = null
        var maxY: Float? = null
        forEach { bean ->
            bean.points.forEach {
                maxX = if (maxX == null) (it.x + bean.offsetX) else max(maxX!!, it.x + bean.offsetX)
                maxY = if (maxY == null) (it.y + bean.offsetY) else max(maxY!!, it.y + bean.offsetY)
            }
        }
        if (maxX == null || maxY == null) {
            return PointF(0f, 0f)
        }
        return PointF(maxX!!, maxY!!)
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
            graphicList.forEach {
                if (it.isGraphicInTouch(event.x - contentTransX, event.y - contentTransY)) {
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