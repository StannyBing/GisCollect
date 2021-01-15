package com.gt.giscollect.module.collect.func.adapter

import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CheckBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import org.json.JSONObject
import java.lang.Exception

class CollectCheckAdapter(dataList: List<CheckBean>) :
    ZXQuickAdapter<CheckBean, ZXBaseHolder>(R.layout.item_collect_check_list, dataList) {
    override fun convert(helper: ZXBaseHolder, bean: CheckBean) {
        helper.setText(R.id.tv_check_name, bean.getFileName())
        helper.setText(R.id.tv_check_person, "审核人：${bean.checker}")
        helper.setText(R.id.tv_check_note, bean.note)
    }

}