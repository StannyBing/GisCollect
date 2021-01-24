package com.gt.entrypad.module.project.bean

import com.gt.base.viewModel.BaseCustomViewModel
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

data class InputInfoBean(var itemStyle:Int=1,var data: BaseCustomViewModel):MultiItemEntity{
    override fun getItemType(): Int {
        return itemStyle
    }

}