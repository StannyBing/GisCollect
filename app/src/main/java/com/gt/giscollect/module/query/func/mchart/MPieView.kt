package com.gt.giscollect.module.query.func.mchart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import java.text.DecimalFormat

/**
 * Created by Xiangb on 2019/10/11.
 * 功能：
 */
class MPieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var piePaint = Paint()//饼图画笔
    private var textPaint = Paint()//文字画笔
    private var linePaint = Paint()//指示线画笔
    private val pieList = arrayListOf<MChartBean>()//数据源

    private val pieWidth = 180f//统计图宽度
    private val pieMargin = 80f//统计图间隔
    private val lineWidth = 80f//指示线长

    private var rotateAngle = -45f

    private var isAllEmpty = false

    private var legendView: MLegendView

    init {
//        setBackgroundColor(Color.WHITE)

        setWillNotDraw(false)

        piePaint.style = Paint.Style.STROKE
        piePaint.strokeWidth = 35f
        piePaint.isAntiAlias = true

        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 0.3f
        textPaint.isAntiAlias = true
        textPaint.textSize = 30f

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 1.2f
        linePaint.isAntiAlias = true

        legendView = MLegendView(context, attrs, defStyle)
        addView(legendView)
    }

    /**
     * 初始化统计数据
     */
    fun initPie(pieList: List<MChartBean>) {
        this.pieList.clear()
        var total = 0.0
        if (pieList.isNotEmpty()) {
            pieList.forEach {
                total += it.num
            }
            if (total == 0.0) {
                isAllEmpty = true
                pieList.forEach {
                    it.percent = 0.0000
                }
            } else {
                isAllEmpty = false
                pieList.forEach {
                    //                    it.percent = DecimalFormat("#0.00").format(Math.floor(it.num/total * 100)/100).replace(".00", "").toDouble()
//                    it.percent = DecimalFormat("#0.0000").format(it.num / total).toDouble()
                    it.percent = (it.num / total * 10000).toInt() / 10000.0
                    if (it.percent > 1) {
                        it.percent = 1.0
                    } else if (it.percent < 0) {
                        it.percent = 0.0
                    }
                }
            }
            this.pieList.addAll(pieList)
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = pieMargin * 2 + pieWidth
        setMeasuredDimension(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(heightSize.toInt(), heightMode)
        )
    }

    @SuppressLint("DrawAllocation", "NewApi")
    override fun onDraw(canvas: Canvas?) {
        if (pieList.isEmpty()) {
            return
        }
        val pieCenter = PointF(width / 2.toFloat(), pieMargin + pieWidth / 2)//中心点
        var startAngle = 0f
        val arcRectf = RectF(
            width / 2 - pieWidth / 2,
            pieMargin,
            width / 2 + pieWidth / 2,
            pieMargin + pieWidth
        )
        val textLineList = arrayListOf<TextLineBean>()
        pieList.forEachIndexed { index, it ->
            val color = ContextCompat.getColor(context, ChartConfig.getColor(index))
            val sweepAngle = if (isAllEmpty) {
                360.toFloat() / pieList.size
            } else {
                it.percent.toFloat() * 360
            }
            //绘制圆弧
            piePaint.color = color
            canvas?.drawArc(arcRectf, startAngle + rotateAngle, sweepAngle, false, piePaint)
            //计算文字位置
            textPaint.color = color
            val drawText = if (isAllEmpty) {
                "0.00%"
            } else {
                "${DecimalFormat("#0.00").format(it.percent * 100)}%"
            }
            val textBounds = Rect()
            textPaint.getTextBounds(drawText, 0, drawText.length, textBounds)
            val lineStartX: Float =
                pieCenter.x + Math.sin(Math.PI * (90 - startAngle - sweepAngle / 2 - rotateAngle) / 180.toDouble())
                    .toFloat() * pieWidth / 2
            var lineStartY: Float =
                pieCenter.y + Math.cos(Math.PI * (90 - startAngle - sweepAngle / 2 - rotateAngle) / 180.toDouble())
                    .toFloat() * pieWidth / 2
            val lineEndX: Float = if (lineStartX > pieCenter.x) {
                (lineStartX + lineWidth + 10).run {
                    if (this + textBounds.width() > width) {
                        (width - textBounds.width()).toFloat()
                    } else {
                        this
                    }
                }
            } else {
                (lineStartX - lineWidth - 10 - textBounds.width()).run {
                    if (this < 0) {
                        0f
                    } else {
                        this
                    }
                }
            }
            //根据起始线的所处象限，移动线的高度，避免线穿透统计图，导致出现显示错误
            if (lineStartY > pieCenter.y) {
                lineStartY += 15
            } else {
                lineStartY -= 15
            }
            textLineList.add(
                TextLineBean(
                    drawText,
                    textBounds.height().toFloat(),
                    textBounds.width().toFloat(),
                    color,
                    lineStartX,
                    lineStartY,
                    lineEndX,
                    lineStartY
                )
            )
            //角度增加
            startAngle += sweepAngle
        }
        //绘制中心图
        pieList.forEach {
            if (it.showCenter) {
                val rect = Rect()
                textPaint.getTextBounds(it.name.substring(0, 5), 0, 5, rect)
                canvas?.drawText(
                    it.name.substring(0, 5),
                    (width / 2 - rect.width() / 2).toFloat(),
                    pieCenter.y - rect.height() * 1.5f,
                    textPaint
                )
                textPaint.getTextBounds(it.name.substring(5), 0, it.name.length - 5, rect)
                canvas?.drawText(
                    it.name.substring(5),
                    (width / 2 - rect.width() / 2).toFloat(),
                    pieCenter.y - rect.height() * 0.5f,
                    textPaint
                )
                textPaint.textSize = 70f
                textPaint.strokeWidth = 4f
                textPaint.getTextBounds(
                    it.num.toInt().toString(),
                    0,
                    it.num.toInt().toString().length,
                    rect
                )
                canvas?.drawText(
                    it.num.toInt().toString(),
                    (width / 2 - rect.width() / 2).toFloat(),
                    pieCenter.y + rect.height(),
                    textPaint
                )
                textPaint.textSize = 30f
                textPaint.strokeWidth = 0.3f
                return@forEach
            }
        }
        //重置绘制文字的位置
        //TODO 只计算过一次，可能会再次出现交叉的情况
        textLineList.forEachIndexed { index, it ->
            val lastBean = if (index == 0) {
                textLineList[textLineList.size - 1]
            } else {
                textLineList[index - 1]
            }
            if (it.startX < it.endX && lastBean.startX < lastBean.endX) {//与上一个同为右侧，此时才会去计算
                if (Math.abs(it.endY - lastBean.endY) < it.textHeight) {//此时已相交
                    it.endY = lastBean.endY + it.textHeight + 5f
                }
            } else if (it.startX > it.endX && lastBean.startX > lastBean.endX) {//与上一个同为左侧，此时才会去计算
                if (Math.abs(it.endY - lastBean.endY) < it.textHeight) {//此时已相交
                    it.endY = lastBean.endY - it.textHeight + 5f
                }
            }
        }
        //绘制文字和指示线
        textLineList.forEach {
            //绘制指示文字
            textPaint.color = it.color
            canvas?.drawText(it.text, it.endX, it.endY + it.textHeight / 2, textPaint)
            //绘制指示线
            linePaint.color = it.color
            if (it.startX > pieCenter.x) {
                it.startX += 15
                canvas?.drawLine(it.startX, it.startY, (it.startX + lineWidth).run {
                    if (this > it.endX) {
                        it.endX
                    } else {
                        this
                    }
                }, it.endY, linePaint)
            } else {
                it.startX -= 15
                canvas?.drawLine(it.startX, it.startY, (it.startX - lineWidth).run {
                    if (this < (it.textWidth + it.endX)) {
                        it.textWidth + it.endX
                    } else {
                        this
                    }
                }, it.endY, linePaint)
            }
        }
    }

    private data class TextLineBean(
        var text: String,
        var textHeight: Float,
        var textWidth: Float,
        var color: Int,
        var startX: Float,
        var startY: Float,
        var endX: Float,
        var endY: Float
    )
}