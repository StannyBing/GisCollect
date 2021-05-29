package com.gt.entrypad.module.project.func.view.idCardView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutIdCardViewBinding

class IdCardView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0):
    BaseCustomView<LayoutIdCardViewBinding, IdCardViewViewModel>(context, attrs, defStyleAttr){
    override fun getLayoutId(): Int {
        return R.layout.layout_id_card_view
    }

    override fun onRootClick(view: View) {
    }

    override fun setDataToView(data: IdCardViewViewModel) {
        getDataBinding().viewModel =data
    }

    override fun setStyle(resId: Int) {
    }

}