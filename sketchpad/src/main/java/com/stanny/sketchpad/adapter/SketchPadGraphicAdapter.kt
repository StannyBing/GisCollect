package com.stanny.sketchpad.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.ImageView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.tool.SketchPadConstant
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SketchPadGraphicAdapter(dataList: List<SketchPadGraphicBean>) :
    ZXQuickAdapter<SketchPadGraphicBean, ZXBaseHolder>(R.layout.item_graphic_layout, dataList) {

    override fun convert(helper: ZXBaseHolder, item: SketchPadGraphicBean) {
        helper.setImageResource(R.id.iv_graphic_bg, item.thumbnail)
    }

}