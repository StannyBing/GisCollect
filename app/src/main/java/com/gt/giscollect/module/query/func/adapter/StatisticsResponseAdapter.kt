package com.gt.giscollect.module.query.func.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gt.giscollect.R
import com.gt.giscollect.module.query.bean.StatisticResultBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import java.util.*

class StatisticsResponseAdapter(data: ArrayList<StatisticResultBean>) :
    ZXQuickAdapter<StatisticResultBean, ZXBaseHolder>(R.layout.item_gis_statistics_response_view, data) {
    override fun convert(helper: ZXBaseHolder, item: StatisticResultBean) {
        if (item.isTitle) {//标题
            helper.setBackgroundColor(
                R.id.ll_response_title,
                ContextCompat.getColor(mContext, R.color.gray_8f)
            )
            helper.getView<View>(R.id.tv_statistics_item_value1).visibility = View.VISIBLE
            helper.getView<View>(R.id.tv_statistics_item_value2).visibility = View.VISIBLE
            helper.setText(R.id.tv_statistics_item_name, item.name)
            helper.setText(R.id.tv_statistics_item_value1, item.value1)
            helper.setText(R.id.tv_statistics_item_value2, item.value2)
//            helper.getView<TextView>(R.id.tv_statistics_item_value1).typeface =
//                Typeface.defaultFromStyle(Typeface.NORMAL)
            helper.getView<TextView>(R.id.tv_statistics_item_value2).typeface =
                Typeface.defaultFromStyle(Typeface.NORMAL)
        } else if (item.value1 != null && item.value2 != null) {//常规统计
            helper.setBackgroundColor(
                R.id.ll_response_title,
                Color.TRANSPARENT
            )
            helper.getView<View>(R.id.tv_statistics_item_value1).visibility = View.VISIBLE
            helper.getView<View>(R.id.tv_statistics_item_value2).visibility = View.VISIBLE
            helper.setText(R.id.tv_statistics_item_name, item.name)
            helper.setText(R.id.tv_statistics_item_value1, item.value1)
            helper.setText(R.id.tv_statistics_item_value2, item.value2)
//            helper.getView<TextView>(R.id.tv_statistics_item_value1).typeface =
//                Typeface.defaultFromStyle(Typeface.BOLD)
            helper.getView<TextView>(R.id.tv_statistics_item_value2).typeface =
                Typeface.defaultFromStyle(Typeface.BOLD)
        } else if (item.value1 != null && item.value2 == null) {//自定义统计-单结果
            helper.setBackgroundColor(
                R.id.ll_response_title,
                ContextCompat.getColor(mContext, R.color.white)
            )
            helper.getView<View>(R.id.tv_statistics_item_value1).visibility = View.VISIBLE
            helper.getView<View>(R.id.tv_statistics_item_value2).visibility = View.GONE
            helper.setText(R.id.tv_statistics_item_name, item.name)
            helper.setText(R.id.tv_statistics_item_value1, item.value1)
//            helper.getView<TextView>(R.id.tv_statistics_item_value1).typeface =
//                Typeface.defaultFromStyle(Typeface.BOLD)
            helper.getView<TextView>(R.id.tv_statistics_item_value2).typeface =
                Typeface.defaultFromStyle(Typeface.BOLD)
        }
    }
}