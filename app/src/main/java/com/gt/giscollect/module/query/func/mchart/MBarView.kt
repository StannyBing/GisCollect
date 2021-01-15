package com.gt.giscollect.module.query.func.mchart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.gt.giscollect.R

/**
 * Created by Xiangb on 2019/10/11.
 * 功能：
 */
class MBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val pieWidth = 180f//统计图宽度
    private val barHeight = 400f//统计图宽度
    private val barMargin = 50f//统计图间隔
    private val lineWidth = 80f//指示线长

    private var rotateAngle = -45f

    private var isAllEmpty = false

    private val barList = arrayListOf<MChartBean>()//数据源
    private var barPaint = Paint()//柱状图画笔
    private var textPaint = Paint()//文字画笔
    private var linePaint = Paint()//指示线画笔
    private var maxNum = 0//最大值
    private var arrawWidth = 10//箭头宽度
    private var scaleNum = 5//竖线刻度数量
    private var scaleWidth = 5//刻度宽度
    private var maxBarWidth = 80//最大柱状图宽度

    init {
//        setBackgroundColor(Color.WHITE)

        setWillNotDraw(false)

        barPaint.style = Paint.Style.FILL
        barPaint.isAntiAlias = true

        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 0.1f
        textPaint.isAntiAlias = true
        textPaint.textSize = 20f
        textPaint.color = ContextCompat.getColor(context, R.color.Chart_21)

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 1.2f
        linePaint.isAntiAlias = true
        linePaint.color = ContextCompat.getColor(context, R.color.Chart_21)

    }

    /**
     * 初始化统计数据
     */
    fun initBar(barList: List<MChartBean>) {
        this.barList.clear()
        if (barList.isNotEmpty()) {
            var maxValue = 0.0
            barList.forEach {
                maxValue = Math.max(it.num, maxValue)
            }
            if (maxValue < 5) {
                maxNum = 5
            } else if (maxValue < 10) {
                maxNum = maxValue.toInt() + 1
            } else if (maxValue < 100) {
                maxNum = ((maxValue.toInt().toString().substring(0, 1)
                    .toInt() + 1).toString() + "0").toInt()
            } else {
                maxNum = ((maxValue.toInt().toString()
                    .substring(0, maxValue.toInt().toString().length - 2)
                    .toInt() + 1).toString() + "00").toInt()
            }
            this.barList.addAll(barList)
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = barMargin * 1.5f + barHeight
        setMeasuredDimension(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(heightSize.toInt(), heightMode)
        )
    }

    @SuppressLint("DrawAllocation", "NewApi")
    override fun onDraw(canvas: Canvas?) {
        if (barList.isEmpty()) {
            return
        }
        //绘制柱状图边框
        drawBarFrame(canvas)
        //绘制柱状值
        drawBarValue(canvas)
    }

    /**
     * 绘制边框
     */
    private fun drawBarFrame(canvas: Canvas?) {
        //线+箭头
        val path = Path()
        path.moveTo(barMargin * 1.5f - arrawWidth / 2, barMargin + arrawWidth)
        path.lineTo(barMargin * 1.5f, barMargin)
        path.lineTo(barMargin * 1.5f + arrawWidth / 2, barMargin + arrawWidth)
        path.moveTo(barMargin * 1.5f, barMargin)
        path.lineTo(barMargin * 1.5f, barHeight + barMargin)
        path.lineTo(width - barMargin, barHeight + barMargin)
        path.lineTo(width - barMargin - arrawWidth, barHeight + barMargin - arrawWidth / 2)
        path.lineTo(width - barMargin, barHeight + barMargin)
        path.lineTo(width - barMargin - arrawWidth, barHeight + barMargin + arrawWidth / 2)
        canvas?.drawPath(path, linePaint)
        //绘制刻度值
        val scaleMargin = (barHeight - 30) / scaleNum
        textPaint.color = ContextCompat.getColor(context, R.color.Chart_21)
        for (i in 0..scaleNum) {
            //绘制刻度线
            if (i > 0) {//不绘制第一个
                canvas?.drawLine(
                    barMargin * 1.5f - scaleWidth,
                    barHeight + barMargin - scaleMargin * i,
                    barMargin * 1.5f,
                    barHeight + barMargin - scaleMargin * i,
                    linePaint
                )
            }
            //绘制刻度值
            textPaint.textSize = 20f
            val rect = Rect()
            textPaint.getTextBounds(
                (maxNum / scaleNum * i).toString(),
                0,
                (maxNum / scaleNum * i).toString().length,
                rect
            )
            canvas?.drawText(
                (maxNum / scaleNum * i).toString(),
                barMargin * 1.5f - scaleWidth - 5 - rect.width(),
                barHeight + barMargin - scaleMargin * i + textPaint.getBaseline(),
                textPaint
            )
        }
    }

    /**
     * 绘制柱状图的值
     */
    private fun drawBarValue(canvas: Canvas?) {
        if (barList.size > 0) {
            val barTotalWidth = width - barMargin * 3
            var barWidth = barTotalWidth / barList.size * 0.7f
            if (barWidth > maxBarWidth) {//避免柱状图过于宽
                barWidth = maxBarWidth.toFloat()
            }
            val barDivider = barTotalWidth / barList.size - barWidth
            barList.forEachIndexed { index, it ->
                val barValueHeight = (it.num / maxNum) * (barHeight - 30)
                barPaint.color = ContextCompat.getColor(context, ChartConfig.getColor(index))
                textPaint.color = ContextCompat.getColor(context, ChartConfig.getColor(index))
                canvas?.drawRect(
                    barMargin * 1.5f + barWidth * index + barDivider * (index + 1),
                    (barHeight + barMargin - barValueHeight).toFloat(),
                    barMargin * 1.5f + barWidth * (index + 1) + barDivider * (index + 1),
                    barHeight + barMargin,
                    barPaint
                )
                //绘制数据值
                val num = if (it.num.toString().endsWith(".0")) {
                    it.num.toInt().toString()
                } else {
                    it.num.toString()
                }
                textPaint.textSize = 15f
                val rect = Rect()
                textPaint.getTextBounds(
                    num,
                    0,
                    num.length,
                    rect
                )
                canvas?.drawText(
                    num,
                    barMargin * 1.5f + barWidth * (index + 1) + barDivider * (index + 1) - barWidth / 2 - rect.width() / 2,
                    (barHeight + barMargin - barValueHeight - rect.height()).toFloat(),
                    textPaint
                )
            }
        }
    }

    fun Paint.getBaseline(): Float {
        return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

}