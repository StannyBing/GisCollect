package com.gt.entrypad.module.project.func.adapter

import android.view.View
import com.frame.zxmvp.baserx.RxManager
import com.gt.entrypad.R
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.ui.view.resultShowView.ResultShowView
import com.gt.entrypad.module.project.ui.view.resultShowView.ResultShowViewViewModel
import com.gt.entrypad.module.project.ui.view.signView.SignView
import com.gt.entrypad.module.project.ui.view.titleView.TitleView
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXMultiItemQuickAdapter

class ResultShowAdapter :ZXMultiItemQuickAdapter<InputInfoBean,ZXBaseHolder>{
    constructor(dataList:List<InputInfoBean>):super(dataList){
        addItemType(1, R.layout.item_title_layout)
        addItemType(2,R.layout.item_result_show_layout)
        addItemType(3,R.layout.item_attachment_layout)
        addItemType(4,R.layout.item_signature_layout)
    }
    override fun convert(helper: ZXBaseHolder, item: InputInfoBean) {
        setData(helper,item)
    }

    override fun onBindViewHolder(holder: ZXBaseHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()){
            setData(holder,data[position])
        }else{
            super.onBindViewHolder(holder, position, payloads)
        }
    }
    private fun setData(helper: ZXBaseHolder,item:InputInfoBean){
        when(item.itemType){
            1->{
                //标题
                helper.getView<TitleView>(R.id.itemTitleView).apply {
                    setData(item.data as TitleViewViewModel)
                }
            }
            2->{
                //输入框
                helper.getView<ResultShowView>(R.id.itemResultShowView).apply {
                        setData(item.data as ResultShowViewViewModel)
                }
            }
           3->{
                //标题
                helper.getView<TitleView>(R.id.itemAttachmentTv).apply {
                    setData(item.data as TitleViewViewModel)
                    setActionListener(object : ICustomViewActionListener {
                        override fun onAction(
                            action: String,
                            view: View,
                            viewModel: BaseCustomViewModel
                        ) {
                            RxManager().post("attachment",(item.data as TitleViewViewModel).title)
                        }

                    })
                }
            }
            4->{
                //签名
                helper.getView<TitleView>(R.id.itemSignatureTv).apply {
                    setData(item.data as TitleViewViewModel)
                }
                helper.getView<TitleView>(R.id.itemResetTv).apply {
                    setData(TitleViewViewModel("重签").apply {
                        resId =R.style.titleText
                        setActionListener(object : ICustomViewActionListener {
                            override fun onAction(
                                action: String,
                                view: View,
                                viewModel: BaseCustomViewModel
                            ) {
                               helper.getView<SignView>(R.id.itemSignView).apply {
                                   clear()
                               }
                            }

                        })
                    })
                }
            }
        }
    }
}