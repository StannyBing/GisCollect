package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.base.UserBean

import com.gt.giscollect.module.system.mvp.contract.SplashContract
import com.zx.zxutils.http.ZXHttpListener
import com.zx.zxutils.http.ZXHttpTool
import okhttp3.RequestBody
import org.json.JSONObject
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