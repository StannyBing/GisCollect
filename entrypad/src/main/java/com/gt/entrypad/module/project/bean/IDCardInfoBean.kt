package com.gt.entrypad.module.project.bean

import com.gt.entrypad.module.project.ui.view.idCardView.IdCardViewViewModel
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

class IDCardInfoBean (var itemStyle:Int=1,var data:IdCardViewViewModel):MultiItemEntity{
    override fun getItemType(): Int {
        return itemStyle
    }

}