package com.gt.entrypad.module.project.ui.view.editText

import android.text.Editable
import android.text.TextWatcher
import com.gt.base.viewModel.BaseCustomViewModel

class EditTextViewViewModel (var title:String="",var hint:String="",var inputContent:String="",var isRequired:Boolean=false,var requiredContent:String="",var isFocus:Boolean=false):
    BaseCustomViewModel(){

    var textWatcher = object :TextWatcher{
       override fun afterTextChanged(p0: Editable?) {
           if (isFocus)inputContent = p0?.toString()?.trim()?:""

       }

       override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
       }

       override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
       }

   }
}