package com.gt.entrypad.module.project.func

import android.view.View
import android.widget.TextView
import com.esri.arcgisruntime.data.Field
import com.gt.entrypad.R
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SketchFieldCreateAdapter(dataList: List<Field>) :
    ZXQuickAdapter<Field, ZXBaseHolder>(R.layout.item_sketch_create_field, dataList) {

    override fun convert(helper: ZXBaseHolder, item: Field) {
        helper.setText(
            R.id.tv_collect_create_field_type, when (item.fieldType) {
                Field.Type.TEXT -> "字符型"
                Field.Type.INTEGER -> "整型"
                Field.Type.FLOAT -> "浮点型"
                Field.Type.DATE -> "日期型"
                else -> "其他"
            }
        )
        helper.setText(R.id.tv_collect_create_field_name, item.name)
        if (item.alias == "default") {
            helper.getView<TextView>(R.id.tv_delete).visibility = View.GONE
        } else {
            helper.getView<TextView>(R.id.tv_delete).visibility = View.VISIBLE
        }
    }
}