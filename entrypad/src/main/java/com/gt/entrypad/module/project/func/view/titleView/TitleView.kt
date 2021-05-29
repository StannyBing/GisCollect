package com.gt.entrypad.module.project.func.view.titleView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutTitleViewBinding

class TitleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0) :
    BaseCustomView<LayoutTitleViewBinding, TitleViewViewModel>(context, attrs, defStyleAttr){
    override fun setStyle(resId: Int) {
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_title_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: TitleViewViewModel) {
        getDataBinding().viewModel = data
    }

}