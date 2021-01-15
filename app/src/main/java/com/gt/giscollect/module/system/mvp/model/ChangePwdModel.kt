package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService

import com.gt.giscollect.module.system.mvp.contract.ChangePwdContract
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ChangePwdModel : BaseModel(), ChangePwdContract.Model {
    override fun changePwdData(map: Map<String, String>): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .changePwd(map)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}