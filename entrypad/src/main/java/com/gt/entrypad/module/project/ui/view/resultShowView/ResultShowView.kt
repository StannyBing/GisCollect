package com.gt.entrypad.module.project.ui.view.resultShowView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutResultShowViewBinding

class ResultShowView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0): BaseCustomView<LayoutResultShowViewBinding, ResultShowViewViewModel>(context, attrs, defStyleAttr){
    override fun getLayoutId(): Int {
        return R.layout.layout_result_show_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: ResultShowViewViewModel) {
       getDataBinding().resultShow = data
    }

    override fun setStyle(resId: Int) {

    }

}