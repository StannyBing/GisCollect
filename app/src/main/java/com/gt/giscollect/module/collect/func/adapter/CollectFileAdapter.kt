package com.gt.giscollect.module.collect.func.adapter

import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.FileInfoBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter

class CollectFileAdapter(dataList: List<FileInfoBean>) :
    ZXQuickAdapter<FileInfoBean, ZXBaseHolder>(R.layout.item_collect_create_file, dataList) {

    var fileParentPath = ""
    var showDelete = true

    public var editable: Boolean = true

    override fun convert(helper: ZXBaseHolder, item: FileInfoBean) {
        Glide.with(mContext)
            .run {
//                if (item.fileBytes == null) {
                    load(fileParentPath + "/" + item.pathImage)
//                } else {
//                    load(item.fileBytes)
//                }
            }
            .apply(
                RequestOptions().placeholder(
                    when (item.type) {
                        "camera", "CAMERA" -> R.drawable.layerlist_camera
                        "video", "VIDEO" -> R.drawable.layerlist_vedio
                        "record", "RECORD" -> R.drawable.layerlist_record
                        else -> R.drawable.layerlist_camera
                    }
                ).transform(RoundedCorners(20))
            )
            .into(helper.getView(R.id.iv_collect_file_image))
        helper.setVisible(R.id.iv_collect_video_play, item.type == "video" || item.type == "VIDEO")
        helper.addOnClickListener(R.id.iv_collect_file_delete)
        helper.getView<ImageView>(R.id.iv_collect_file_delete).visibility =
            if (showDelete && editable) View.VISIBLE else View.GONE
    }
}