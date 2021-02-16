package com.gt.entrypad.module.project.ui.view.editText

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutEditTextViewBinding

class EditTextView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0):
    BaseCustomView<LayoutEditTextViewBinding, EditTextViewViewModel>(context, attrs, defStyleAttr){
    override fun setStyle(resId: Int) {

    }

    override fun getLayoutId(): Int {
        return R.layout.layout_edit_text_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: EditTextViewViewModel) {
        getDataBinding().viewModel = data
        getDataBinding().viewEtInput.setOnFocusChangeListener { v, hasFocus ->
           data.isFocus = hasFocus
        }
    }
}