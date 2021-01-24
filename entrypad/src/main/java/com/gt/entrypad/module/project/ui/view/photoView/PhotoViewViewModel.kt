package com.gt.entrypad.module.project.ui.view.photoView

import android.view.View
import com.frame.zxmvp.baserx.RxManager
import com.gt.entrypad.R
import com.gt.base.viewModel.BaseCustomViewModel

class PhotoViewViewModel(var url:String="",var width:Int = 288,var height:Int=288) :
    BaseCustomViewModel(){
    fun onClick(view: View){
        when(view.id){
            R.id.photoViewIv->{
                if (url.isEmpty()&&resId!=0){
                    RxManager().post("show",resId)
                }else{
                    RxManager().post("preview",url)
                }
            }else->{
            RxManager().post("delete",this)
        }
        }
    }
}