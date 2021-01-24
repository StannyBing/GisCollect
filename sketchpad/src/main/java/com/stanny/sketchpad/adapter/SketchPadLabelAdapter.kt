package com.stanny.sketchpad.adapter

import android.widget.CheckBox
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchLabelBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter

class SketchPadLabelAdapter(data:List<SketchLabelBean>) :ZXRecyclerQuickAdapter<SketchLabelBean,ZXBaseHolder>(
    R.layout.item_lable_layout,data){
    override fun quickConvert(helper: ZXBaseHolder, item: SketchLabelBean) {
        helper.setText(R.id.itemContentTv,item.value)
        helper.getView<CheckBox>(R.id.itemLabelCb).apply {
            isChecked = item.isChecked
        }
        helper.addOnClickListener(R.id.itemLabelCb)
    }

}