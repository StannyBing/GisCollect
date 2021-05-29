package com.gt.entrypad.module.project.func.view.infoDialogView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutInfoDialogViewBinding

class InfoDialogView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0): BaseCustomView<LayoutInfoDialogViewBinding, InfoDialogViewViewModel>(context, attrs, defStyleAttr){
    override fun setStyle(resId: Int) {

    }

    override fun getLayoutId(): Int {
        return R.layout.layout_info_dialog_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: InfoDialogViewViewModel) {
        getDataBinding().viewModel = data
    }


}