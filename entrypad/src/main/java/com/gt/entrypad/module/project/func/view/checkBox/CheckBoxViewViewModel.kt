package com.gt.entrypad.module.project.func.view.checkBox

import android.view.View
import android.widget.CheckBox
import com.gt.base.viewModel.BaseCustomViewModel

class CheckBoxViewViewModel(var content:String="",var checked:Boolean=false) :
    BaseCustomViewModel(){
    fun onClick(view: View){
       checked = (view as CheckBox).isChecked
    }
}