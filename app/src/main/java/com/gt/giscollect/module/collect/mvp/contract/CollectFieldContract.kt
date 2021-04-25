package com.gt.giscollect.module.collect.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.base.bean.NormalList
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody
import retrofit2.http.Body
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
interface CollectFieldContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun dictQueryResult(dictResult:Any?)

        fun dictQueryByQictResult(dictByDictResult:NormalList<DataResBean>?)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun doDictQuery(body: RequestBody):Observable<Any>
        fun doDictByDictQuery(body: RequestBody):Observable<NormalList<DataResBean>>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun doDictQuery(body: RequestBody)
        abstract fun doDictByDictQuery(body: RequestBody)
    }
}

