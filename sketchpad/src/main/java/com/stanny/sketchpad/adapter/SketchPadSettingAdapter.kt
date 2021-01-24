package com.stanny.sketchpad.adapter

import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadSettingBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SketchPadSettingAdapter(dataList: List<SketchPadSettingBean>) :
    ZXQuickAdapter<SketchPadSettingBean, ZXBaseHolder>(R.layout.item_func_setting, dataList) {
    override fun convert(helper: ZXBaseHolder, item: SketchPadSettingBean) {
        helper.setText(R.id.tv_setting_name, item.name)
        helper.setGone(R.id.sw_setting_switch, item.type == SketchPadSettingBean.SettingType.Switch)
        helper.setGone(R.id.et_setting_edit, item.type == SketchPadSettingBean.SettingType.Edit)
    }
}