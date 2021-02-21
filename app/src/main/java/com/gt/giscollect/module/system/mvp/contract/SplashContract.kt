package com.gt.giscollect.module.system.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.base.manager.UserBean
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
interface SplashContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun onAppConfigResult(appInfo: String)

        fun onLoginResult(bean: UserBean?)
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel {
        fun appConfigData(): Observable<String>

        fun loginData(body: RequestBody): Observable<UserBean>
    }

    //方法
    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun initData(body: RequestBody?)
    }
}

