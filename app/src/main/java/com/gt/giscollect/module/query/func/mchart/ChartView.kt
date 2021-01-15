package com.gt.giscollect.module.query.func.mchart

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.gt.giscollect.R

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var barChart: MBarView
    private var pieChart: MPieView
    private var legendChart: MLegendView

    enum class ChartType {
        BarChart,
        PieChart
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_chart_view, this, true)
        barChart = findViewById(R.id.bar_chart)
        pieChart = findViewById(R.id.pie_chart)
        legendChart = findViewById(R.id.legend_chart)
    }

    fun setData(data: List<MChartBean>, type: ChartType? = null) {
        barChart.visibility = View.GONE
        pieChart.visibility = View.GONE
        when (type) {
            ChartType.BarChart -> {
                barChart.initBar(data)
                barChart.visibility = View.VISIBLE
            }
            ChartType.PieChart -> {
                pieChart.initPie(data)
                pieChart.visibility = View.VISIBLE
            }
            else -> {
                barChart.visibility = View.VISIBLE
                pieChart.visibility = View.VISIBLE
                barChart.initBar(data)
                pieChart.initPie(data)
            }
        }
        legendChart.initLegend(data)
    }

}