package com.gt.entrypad.module.project.func.adapter

import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.IDCardInfoBean
import com.gt.entrypad.module.project.ui.view.idCardView.IdCardView
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class IdCardAdapter(data:List<IDCardInfoBean>) : ZXQuickAdapter<IDCardInfoBean, ZXBaseHolder>(R.layout.item_id_card_layout,data){
    override fun convert(helper: ZXBaseHolder, item: IDCardInfoBean) {
        helper.getView<IdCardView>(R.id.itemIdCardView).apply {
            setData(item.data)
        }
    }

}