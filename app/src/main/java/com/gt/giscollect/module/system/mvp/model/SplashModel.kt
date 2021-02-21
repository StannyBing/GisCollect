package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.base.manager.UserBean

import com.gt.giscollect.module.system.mvp.contract.SplashContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class SplashModel : BaseModel(), SplashContract.Model {
    override fun appConfigData(): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getAppConfig()
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun loginData(body: RequestBody): Observable<UserBean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .doLogin(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}