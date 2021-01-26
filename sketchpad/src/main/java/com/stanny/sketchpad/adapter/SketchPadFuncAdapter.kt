package com.stanny.sketchpad.adapter

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadFuncBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SketchPadFuncAdapter(var dataList: List<SketchPadFuncBean>) :
    ZXQuickAdapter<SketchPadFuncBean, ZXBaseHolder>(R.layout.item_func_layout, dataList) {
    override fun convert(helper: ZXBaseHolder, item: SketchPadFuncBean) {
        helper.setText(R.id.tv_func_name, item.name)
        helper.setBackgroundRes(R.id.iv_func_icon, item.icon)
        helper.setTextColor(R.id.tv_func_name, Color.parseColor("#17abe3"))
//        if (item.isChecked) helper.setBackgroundRes(R.id.iv_func_icon, item.icon)else  helper.setBackgroundRes(R.id.iv_func_icon, item.normalIcon)
//        if (item.isChecked) helper.setTextColor(
//            R.id.tv_func_name,
//            Color.parseColor("#17abe3")
//        ) else helper.setTextColor(
//            R.id.tv_func_name,
//            ContextCompat.getColor(mContext, R.color.gray_cc)
//        )
    }
}