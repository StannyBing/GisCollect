package com.gt.giscollect.module.survey.bean

import com.zx.zxutils.other.QuickAdapter.entity.MultiItemEntity

data class FileResultBean(var spanString:String="",var itemStyle:Int,var fileInfoBean: FileInfoBean):MultiItemEntity{
    override fun getItemType(): Int {
        return  itemStyle
    }
}