package com.gt.giscollect.module.query.func.mchart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class MLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private val legendList = arrayListOf<MChartBean>()//数据源
    private val legendCirclePaint = Paint()//图例圆画笔
    private val legendTextPaint = Paint()//图例值画笔
    private val legendMargin = 30f//图例间隔
    private val legendCircleRadius = 8f//圆点半径
    private val legendItemHeight = 35f
    private val legendSize = 3//图例一排的数量

    init {
//        setBackgroundColor(Color.WHITE)

        setWillNotDraw(false)
        legendCirclePaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        legendTextPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            textSize = 16f
        }
    }

    fun initLegend(legendList: List<MChartBean>) {
        this.legendList.clear()
        this.legendList.addAll(legendList)

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = legendMargin + legendItemHeight * (legendList.size / legendSize + 1)
        setMeasuredDimension(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(heightSize.toInt(), heightMode)
        )
    }

    override fun onDraw(canvas: Canvas?) {
        if (legendList.isEmpty()) {
            return
        }
        val legendDivider = (width - legendMargin * 2) / legendSize
        legendList.forEachIndexed { index, it ->
            //绘制圆点
            legendCirclePaint.color = ContextCompat.getColor(context, ChartConfig.getColor(index))
            canvas?.drawCircle(
                legendMargin + legendDivider * (index % legendSize) + legendCircleRadius + 10,
                legendMargin / 2 + legendItemHeight * (index / legendSize) + legendItemHeight / 2,
                legendCircleRadius,
                legendCirclePaint
            )
            //绘制文字
            legendTextPaint.color = ContextCompat.getColor(context, ChartConfig.getColor(index))
            val rect = Rect()
            legendTextPaint.getTextBounds(it.name, 0, it.name.length, rect)
            if (rect.width() < legendDivider - 10f) {
                canvas?.drawText(
                    it.name,
                    legendMargin + legendDivider * (index % legendSize) + legendCircleRadius * 2 + 5f + 10,
                    legendMargin / 2 + legendItemHeight * (index / legendSize) + legendItemHeight / 2 + legendTextPaint.getBaseline(),
                    legendTextPaint
                )
            } else {
                val nameIndex = if (it.name.length / 2 > 7) 7 else it.name.length / 2
                canvas?.drawText(
                    it.name.substring(0, nameIndex),
                    legendMargin + legendDivider * (index % legendSize) + legendCircleRadius * 2 + 5f + 10,
                    legendMargin / 2 + legendItemHeight * (index / legendSize) + legendItemHeight / 2 + legendTextPaint.getBaseline() - rect.height() / 2,
                    legendTextPaint
                )
                val nameLastIndex =
                    if (nameIndex * 2 < it.name.length) nameIndex * 2 else it.name.length
                canvas?.drawText(
                    it.name.substring(nameIndex, nameLastIndex) + "...",
                    legendMargin + legendDivider * (index % legendSize) + legendCircleRadius * 2 + 5f + 10,
                    legendMargin / 2 + legendItemHeight * (index / legendSize) + legendItemHeight / 2 + legendTextPaint.getBaseline() + rect.height() / 2,
                    legendTextPaint
                )
            }
        }
        super.onDraw(canvas)
    }

    fun Paint.getBaseline(): Float {
        return (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    }

}
