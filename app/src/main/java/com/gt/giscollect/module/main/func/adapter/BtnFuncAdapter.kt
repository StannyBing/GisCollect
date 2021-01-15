package com.gt.giscollect.module.main.func.adapter

import com.gt.giscollect.R
import com.gt.giscollect.module.main.bean.FuncBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class BtnFuncAdapter(dataList: List<FuncBean>) : ZXQuickAdapter<FuncBean, ZXBaseHolder>(R.layout.item_btn_func, dataList) {
    override fun convert(helper: ZXBaseHolder, item: FuncBean?) {
        if (item != null) {
            helper.setBackgroundRes(R.id.iv_func_icon, item.res)
            helper.setText(R.id.tv_func_name, item.name)
        }
    }
}