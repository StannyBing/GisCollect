package com.gt.entrypad.module.project.func

import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.ui.view.editText.EditTextView
import com.gt.entrypad.module.project.ui.view.editText.EditTextViewViewModel
import com.gt.entrypad.module.project.ui.view.infoDialogView.InfoDialogView
import com.gt.entrypad.module.project.ui.view.infoDialogView.InfoDialogViewViewModel
import com.gt.entrypad.module.project.ui.view.spinnerView.SpinnerView
import com.gt.entrypad.module.project.ui.view.spinnerView.SpinnerViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleView
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXMultiItemQuickAdapter

class InfoInputAdapter :ZXMultiItemQuickAdapter<InputInfoBean,ZXBaseHolder>{
    constructor(dataList:List<InputInfoBean>):super(dataList){
        addItemType(1, R.layout.item_title_layout)
        addItemType(2,R.layout.item_input_layout)
        addItemType(3,R.layout.item_spinner_layout)
        addItemType(4,R.layout.item_info_dialog_layout)
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
                    setStyle(R.style.titleText)
                }
            }
            2->{
                //输入框
                helper.getView<EditTextView>(R.id.itemEditTextView).apply {
                    setData(item.data as EditTextViewViewModel)
                }
            }
            3->{
                //下拉选择框
                helper.getView<SpinnerView>(R.id.itemSpinnerView).apply {
                    setData(item.data as SpinnerViewViewModel)
                }
            }
            4->{
                //弹窗
                helper.getView<InfoDialogView>(R.id.itemInfoDialogView).apply {
                    setData(item.data as InfoDialogViewViewModel)
                }
            }
        }
    }
}