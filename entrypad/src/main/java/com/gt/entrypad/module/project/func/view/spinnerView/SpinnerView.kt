package com.gt.entrypad.module.project.func.view.spinnerView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutSpinnerViewBinding

class SpinnerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0):
    BaseCustomView<LayoutSpinnerViewBinding, SpinnerViewViewModel>(context, attrs, defStyleAttr){
    override fun setStyle(resId: Int) {

    }

    override fun getLayoutId(): Int {
        return R.layout.layout_spinner_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: SpinnerViewViewModel) {
       getDataBinding().viewModel  = data
        getDataBinding().spinner.apply {
            setData(data.entries)
                showSeletedLayoutColor(true, R.color.red)
                showSelectedTextColor(true, R.color.colorPrimary)
                showDivider(true, ContextCompat.getColor(context, R.color.colorPrimary))
                showUnderineColor(false)
                setItemHeightDp(30)
                setItemTextSizeSp(12)
                build()
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    data.inputContent = selectedValue as String
                }
            }
        }
    }

}