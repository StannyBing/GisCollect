package com.gt.giscollect.module.system.func.adapter

import com.gt.giscollect.R
import com.gt.giscollect.module.system.bean.GuideBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXMultiItemQuickAdapter
import com.zx.zxutils.other.QuickAdapter.ZXSectionMultiItemQuickAdapter

class GuideAdapter(dataList: List<GuideBean>) :
    ZXMultiItemQuickAdapter<GuideBean, ZXBaseHolder>(dataList) {

    var call: (GuideBean) -> Unit = {}

    init {
        addItemType(GuideBean.GUIDE_STEP, R.layout.item_guide_step)
        addItemType(GuideBean.GUIDE_ITEM, R.layout.item_guide_item)
    }

    override fun convert(helper: ZXBaseHolder, bean: GuideBean) {
        when (helper.itemViewType) {
            GuideBean.GUIDE_STEP -> {
                helper.setBackgroundRes(R.id.iv_guide_step_icon, bean.icon)
                helper.setText(R.id.tv_guide_step_name, bean.itemName)
                helper.setBackgroundRes(
                    R.id.iv_guide_step_status,
                    if (bean.isExpanded) R.drawable.guide_up else R.drawable.guide_down
                )
                helper.itemView.setOnClickListener {
                    if (bean.isExpanded) {
                        collapse(helper.adapterPosition, true)
                    } else {
                        expand(helper.adapterPosition, true)
                    }
                }
            }
            GuideBean.GUIDE_ITEM -> {
                helper.setText(R.id.tv_guide_item_name, bean.itemName)
                helper.itemView.setOnClickListener {
                    call(bean)
                }
            }
        }
    }

    fun setChildCall(call: (GuideBean) -> Unit) {
        this.call = call
    }
}