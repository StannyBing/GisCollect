package com.gt.entrypad.module.project.func.adapter

import com.gt.entrypad.R
import com.gt.entrypad.module.project.func.view.checkBox.CheckBoxView
import com.gt.entrypad.module.project.func.view.checkBox.CheckBoxViewViewModel
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class CheckBoxAdapter(data:List<CheckBoxViewViewModel>) :ZXQuickAdapter<CheckBoxViewViewModel,ZXBaseHolder>(R.layout.item_check_box_layout,data){
    override fun convert(helper: ZXBaseHolder, item: CheckBoxViewViewModel) {
        helper.getView<CheckBoxView>(R.id.itemCheckBoxView).apply {
            setData(item)
        }
    }

}