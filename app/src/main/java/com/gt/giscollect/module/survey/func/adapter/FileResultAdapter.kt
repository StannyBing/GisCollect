package com.gt.giscollect.module.survey.func.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.gt.giscollect.R
import com.gt.giscollect.module.survey.bean.FileInfoBean
import com.gt.giscollect.module.survey.bean.FileResultBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXMultiItemQuickAdapter

class FileResultAdapter(data:List<FileResultBean>) :ZXMultiItemQuickAdapter<FileResultBean,ZXBaseHolder>(data){

    init {
        addItemType(1,R.layout.item_file_result)
        addItemType(2,R.layout.item_file_result_img)
    }
    override fun convert(helper: ZXBaseHolder, item: FileResultBean) {
        when(item.itemType){
            1->{
                //文件夹
                val name = item.fileInfoBean.name
                if (item.spanString.isNullOrEmpty()){
                   helper.setText(R.id.itemFileNameTv,name)
               }else{
                   val spannableString = SpannableString(name)
                   var index = name.indexOf(item.spanString)
                   spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#81B9F2")),index,index+item.spanString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                   helper.getView<TextView>(R.id.itemFileNameTv).text = spannableString
               }
            }
            2->{
                //文件
                Glide.with(mContext).load(item.fileInfoBean.pathImage)
                    .apply(RequestOptions().error(R.drawable.icon_img_error))
                    .into(helper.getView(R.id.itemFileIv))
            }
        }
    }

}