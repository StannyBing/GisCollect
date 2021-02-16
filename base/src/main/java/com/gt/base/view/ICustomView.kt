package com.gt.base.view

import com.gt.base.viewModel.BaseCustomViewModel

interface ICustomView <S: BaseCustomViewModel>{
    fun setData(data:S)
    fun setStyle(resId:Int)
    fun setActionListener(listener: ICustomViewActionListener)
}