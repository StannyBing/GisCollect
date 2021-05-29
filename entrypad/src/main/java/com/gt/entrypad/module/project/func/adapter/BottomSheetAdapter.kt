package com.gt.entrypad.module.project.func.adapter

import android.view.View
import com.frame.zxmvp.baserx.RxManager
import com.gt.entrypad.R
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.module.project.func.view.titleView.TitleView
import com.gt.entrypad.module.project.func.view.titleView.TitleViewViewModel
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.ZXRecyclerAdapter.ZXRecyclerQuickAdapter

class BottomSheetAdapter(dataList:List<TitleViewViewModel>) :ZXRecyclerQuickAdapter<TitleViewViewModel,ZXBaseHolder>(R.layout.item_title_layout,dataList){
    override fun quickConvert(helper: ZXBaseHolder, item: TitleViewViewModel) {
       helper.getView<TitleView>(R.id.itemTitleView).apply {
           setData(item)
           setActionListener(object : ICustomViewActionListener {
               override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                   RxManager().post("bottom",item.title)
               }

           })
       }
    }

}