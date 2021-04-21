package com.stanny.sketchpad.adapter

import android.widget.CheckBox
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter
import com.zx.zxutils.util.ZXToastUtil

class SketchPadFloorGraphicAdapter(data: List<SketchPadGraphicBean>) :
    ZXRecyclerQuickAdapter<SketchPadGraphicBean, ZXBaseHolder>(
        R.layout.item_floor_layout, data
    ) {
    private var checkedChangeListener: (Int) -> Unit = { position -> }

    override fun quickConvert(helper: ZXBaseHolder, item: SketchPadGraphicBean) {
//        helper.setText(R.id.itemContentTv,"${item.id}")
        helper.setText(R.id.itemContentTv, "图形${helper.adapterPosition + 1}")
        helper.getView<CheckBox>(R.id.itemFloorCb).apply {
            isChecked = item.isChecked
            setOnClickListener {
                checkedChangeListener(helper.adapterPosition)
            }
        }
        helper.itemView.setOnClickListener {
            checkedChangeListener(helper.adapterPosition)
        }
    }

    fun addCheckedChangeListener(checkedChangeListener: (Int) -> Unit = { position -> }) {
        this.checkedChangeListener = checkedChangeListener
    }
}