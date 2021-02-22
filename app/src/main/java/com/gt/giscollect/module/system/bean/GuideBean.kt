package com.gt.giscollect.module.system.bean

import androidx.annotation.DrawableRes
import com.gt.base.app.AppFuncBean
import com.zx.zxutils.other.QuickAdapter.entity.AbstractExpandableItem
import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

data class GuideBean(
    var parentName: String = "",
    var itemName: String = "",
    var id: String = "",
    @DrawableRes var icon: Int = 0,
    var type: Int = GUIDE_STEP,
    var childList: ArrayList<GuideBean> = arrayListOf(),
    var appFuncs: List<AppFuncBean> = arrayListOf(),
    var templateId : String? = ""
) : AbstractExpandableItem<GuideBean>(), MultiItemEntity {

    companion object {
        const val GUIDE_STEP = 0
        const val GUIDE_ITEM = 1
    }

    override fun getItemType(): Int {
        return type
    }

    override fun getLevel(): Int {
        return type
    }
}
