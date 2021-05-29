package com.gt.entrypad.module.project.bean

import com.gt.entrypad.module.project.func.view.idCardView.IdCardViewViewModel
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity
import java.io.Serializable

class IDCardInfoBean (var itemStyle:Int=1,var data: IdCardViewViewModel):MultiItemEntity,Serializable{
    override fun getItemType(): Int {
        return itemStyle
    }

}