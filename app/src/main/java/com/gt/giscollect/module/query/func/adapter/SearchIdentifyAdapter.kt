package com.gt.giscollect.module.query.func.adapter

import com.esri.arcgisruntime.data.Field
import com.gt.giscollect.R
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.ZXTimeUtil
import java.text.SimpleDateFormat
import java.util.*

class SearchIdentifyAdapter(dataList: List<Pair<Field, Any?>>) :
    ZXQuickAdapter<Pair<Field, Any?>, ZXBaseHolder>(R.layout.item_collect_create_field, dataList) {
    override fun convert(helper: ZXBaseHolder, item: Pair<Field, Any?>?) {
        if (item != null) {
            helper.setText(
                R.id.tv_collect_create_field_type, if (item.first.alias.isEmpty()) {
                    item.first.name
                } else {
                    item.first.alias
                }
            )

            helper.setText(
                R.id.tv_collect_create_field_name, when (item.first.fieldType) {
                    Field.Type.DATE -> {
                        if (item.second == null || item.second !is GregorianCalendar) "" else {
                            ZXTimeUtil.getTime(
                                (item.second as GregorianCalendar).timeInMillis,
                                SimpleDateFormat("yyyy/MM/dd")
                            )
                        }
                    }
                    else -> {
                        item.second.toString().replace("null", "")
                    }
                }
            )
        }
    }
}