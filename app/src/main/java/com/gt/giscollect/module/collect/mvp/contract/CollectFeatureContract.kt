package com.gt.giscollect.module.collect.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
interface CollectFeatureContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun checkMultiNameResult(boolean: Boolean, beforeName: String?, afterName: String)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun checkMultiNameData(map : HashMap<String, String>) : Observable<Boolean>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun checkMultiName(map: HashMap<String, String>, beforeName: String?, afterName: String)
    }
}

