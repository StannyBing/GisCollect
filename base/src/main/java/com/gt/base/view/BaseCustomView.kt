package com.gt.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gt.base.viewModel.BaseCustomViewModel

abstract class BaseCustomView<T:ViewDataBinding,S: BaseCustomViewModel> @JvmOverloads constructor(context:Context, attrs:AttributeSet? = null, defStyleAttr:Int = 0):ConstraintLayout(context, attrs, defStyleAttr),
    View.OnClickListener, ICustomView<S> {
    private lateinit var dataBinding: T
    private lateinit var viewModel:S
    private var mListener: ICustomViewActionListener? = null

    init {
        initView()
    }
    private fun initView(){
        val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (getLayoutId() != 0) {
            dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), this, false)
            dataBinding.root.setOnClickListener { view ->
                mListener?.onAction(
                    ICustomViewActionListener.ACTION_ROOT_VIEW_CLICKED,
                    view,
                    viewModel
                )
                onRootClick(view)
            }
            this.addView(dataBinding.root)
        }
    }

    override fun setActionListener(listener: ICustomViewActionListener) {
        this.mListener = listener
    }
    override fun onClick(p0: View) {
    }

    override fun setData(data: S) {
        viewModel = data
        setDataToView(viewModel)
        if (this::dataBinding.isInitialized) {
            dataBinding.executePendingBindings()
        }
        onDataUpdated()
    }
    protected fun onDataUpdated(){

    }
    protected fun getDataBinding(): T {
        return dataBinding
    }
    abstract fun getLayoutId():Int
    abstract fun onRootClick(view:View)
    abstract fun setDataToView(data:S)
}