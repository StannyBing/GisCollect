package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.base.UserBean

import com.gt.giscollect.module.system.mvp.contract.LoginContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class LoginModel : BaseModel(), LoginContract.Model {
    override fun loginData(body: RequestBody): Observable<UserBean> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .doLogin(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

}