package com.gt.giscollect.module.query.func.adapter

import com.esri.arcgisruntime.data.Feature
import com.gt.giscollect.R
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class SearchAdapter(dataList: List<Feature>) : ZXQuickAdapter<Feature, ZXBaseHolder>(R.layout.item_search_list, dataList) {

    var showIndex: Int = 0

    override fun convert(helper: ZXBaseHolder, item: Feature?) {
        if (item != null) {
            if (showIndex == -1) {
                helper.setText(R.id.tv_search_map_name, "${helper.adapterPosition + 1}")
            } else {
                helper.setText(R.id.tv_search_map_name, "${item.attributes.values.toTypedArray()[showIndex]}")
            }
        }
    }
}