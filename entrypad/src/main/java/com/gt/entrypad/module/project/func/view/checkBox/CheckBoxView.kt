package com.gt.entrypad.module.project.func.view.checkBox

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutCheckBoxViewBinding

class CheckBoxView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0): BaseCustomView<LayoutCheckBoxViewBinding, CheckBoxViewViewModel>(context, attrs, defStyleAttr){
    override fun setStyle(resId: Int) {

    }

    override fun getLayoutId(): Int {
        return R.layout.layout_check_box_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: CheckBoxViewViewModel) {
        getDataBinding().viewModel = data
    }

}