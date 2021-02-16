package com.gt.base.view

import android.view.View
import com.gt.base.viewModel.BaseCustomViewModel

interface ICustomViewActionListener {
companion object{
    var ACTION_ROOT_VIEW_CLICKED = "action_root_view_clicked"
}

     fun onAction(action: String, view: View, viewModel: BaseCustomViewModel)
}