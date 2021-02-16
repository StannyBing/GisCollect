package com.gt.entrypad.module.project.ui.view.infoDialogView

import android.view.View
import com.frame.zxmvp.baserx.RxManager
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.widget.CustomInfoDialog
import com.zx.zxutils.util.ZXDialogUtil

class InfoDialogViewViewModel(var title:String="",var inputContent:String="",var hint:String="",var isRequired:Boolean=false,var requiredContent:String="") :
    BaseCustomViewModel(){

    fun onClick(view: View){
        val dialog = CustomInfoDialog(view.context).apply {

        }
        ZXDialogUtil.showCustomViewDialog(view.context,hint,dialog) { p0, p1 ->
            inputContent = ""
            val data = dialog.getData()
           kotlin.run data@{
               data.forEach {
                   if (it.checked){
                       inputContent+= if (it.content.isNotEmpty()) it.content+"、" else ""
                       RxManager().post("notify",this)

                   }
               }
               if (inputContent.isNotEmpty())inputContent=inputContent.substring(0,inputContent.length-1)
           }
        }.apply {
            show()
        }
    }
}