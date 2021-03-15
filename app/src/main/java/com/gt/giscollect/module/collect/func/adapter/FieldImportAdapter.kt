package com.gt.giscollect.module.collect.func.adapter

import android.widget.TextView
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.FieldImportBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter

class FieldImportAdapter(dataList: List<FieldImportBean>) :
    ZXQuickAdapter<FieldImportBean, ZXBaseHolder>(R.layout.item_field_import, dataList) {

    override fun convert(helper: ZXBaseHolder, item: FieldImportBean) {
        helper.setText(R.id.tv_field_import_name, item.getInfo(!item.isExpand))
        helper.getView<TextView>(R.id.tv_field_import_name).isSingleLine = !item.isExpand
        helper.addOnClickListener(R.id.ll_field_import_title)
        helper.addOnClickListener(R.id.iv_field_import_check)
    }
}