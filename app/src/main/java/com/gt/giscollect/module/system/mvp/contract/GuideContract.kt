package com.gt.giscollect.module.system.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.base.app.AppFuncBean
import com.gt.base.bean.GisServiceBean
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
interface GuideContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun appFuncResult(appFuncs : List<AppFuncBean>)
        fun surveyTypeResult(typeResult:String?)
        fun gisServiceResult(serviceResult:List<GisServiceBean>?)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun  appFuncData(map: HashMap<String, String>) : Observable<Any>
        fun doSurveyType():Observable<String>
        fun doGisService(map: Map<String,String>):Observable<List<GisServiceBean>>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun getAppFuncs(map: HashMap<String, String>)
        abstract fun doSurveyType()
        abstract fun doGisService(map: Map<String, String>)
    }
}

