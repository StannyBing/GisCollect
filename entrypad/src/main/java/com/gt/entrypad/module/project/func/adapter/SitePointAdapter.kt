package com.gt.entrypad.module.project.func.adapter

import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.Field
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.SiteBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SitePointAdapter(dataList:List<SiteBean>) : ZXQuickAdapter<SiteBean, ZXBaseHolder>(R.layout.item_site_point_layout, dataList) {
    override fun convert(helper: ZXBaseHolder, item: SiteBean) {
        helper.setText(R.id.itemSiteTitleTv,item.title)
        helper.setText(R.id.itemSiteLocationTv,item.status)
        helper.setTextColor(R.id.itemSiteLocationTv, ContextCompat.getColor(mContext, if (item.status == "未编辑") R.color.default_text_color else R.color.colorAccent))
    }
}