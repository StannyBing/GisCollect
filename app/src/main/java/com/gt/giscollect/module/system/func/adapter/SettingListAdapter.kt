package com.gt.giscollect.module.system.func.adapter

import androidx.core.content.ContextCompat
import com.gt.giscollect.R
import com.gt.giscollect.module.system.bean.SettingBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SettingListAdapter(dataList: List<SettingBean>) : ZXQuickAdapter<SettingBean, ZXBaseHolder>(R.layout.item_setting_list, dataList) {
    override fun convert(helper: ZXBaseHolder, item: SettingBean) {
        helper.setText(R.id.tv_setting_name, item.name)
        helper.setImageResource(R.id.iv_setting_icon, item.icon)
        helper.setVisible(R.id.iv_setting_more, item.showMore)
        helper.setTextColor(
            R.id.tv_setting_name,
            ContextCompat.getColor(mContext, if (helper.adapterPosition == data.lastIndex) R.color.red else R.color.default_text_color)
        )
    }
}