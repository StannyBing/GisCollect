package com.stanny.sketchpad.adapter

import android.widget.CheckBox
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter
import com.zx.zxutils.util.ZXToastUtil

class SketchPadFloorAdapter(data: List<SketchPadFloorBean>) :
    ZXRecyclerQuickAdapter<SketchPadFloorBean, ZXBaseHolder>(
        R.layout.item_floor_layout, data
    ) {
    private var checkedChangeListener: (Int) -> Unit = { position -> }

    override fun quickConvert(helper: ZXBaseHolder, item: SketchPadFloorBean) {
        helper.setText(R.id.itemContentTv, item.name)
        helper.getView<CheckBox>(R.id.itemFloorCb).apply {
            isClickable = false
            isChecked = item.sketchList.isNotEmpty()
        }
    }
}