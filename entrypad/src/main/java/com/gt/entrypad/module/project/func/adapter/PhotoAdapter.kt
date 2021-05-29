package com.gt.entrypad.module.project.func.adapter

import com.gt.entrypad.R
import com.gt.entrypad.module.project.func.view.photoView.PhotoView
import com.gt.entrypad.module.project.func.view.photoView.PhotoViewViewModel
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class PhotoAdapter(dataList:List<PhotoViewViewModel>) :ZXQuickAdapter<PhotoViewViewModel,ZXBaseHolder>(R.layout.item_photo_layout,dataList){
    override fun convert(helper: ZXBaseHolder, item: PhotoViewViewModel) {
        helper.getView<PhotoView>(R.id.itemPhotoView).apply {
            setData(item)
        }
    }
}
