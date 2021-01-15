package com.gt.giscollect.module.layer.func.adapter

import com.gt.giscollect.R
import com.gt.giscollect.module.layer.bean.LayerBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class BaseLayerAdapter(dataList: List<LayerBean>) : ZXQuickAdapter<LayerBean, ZXBaseHolder>(R.layout.item_layer_base, dataList) {
    override fun convert(helper: ZXBaseHolder, item: LayerBean?) {
        if (item != null) {
            helper.setText(R.id.tv_layer_name, item.name)
            helper.setVisible(R.id.iv_layer_checked, item.isChecked)
            if (item.res != null) {
                helper.setBackgroundRes(R.id.iv_layer_icon, item.res!!)
            }
        }
    }
}