package com.gt.entrypad.module.project.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.entrypad.api.ApiService
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import okhttp3.RequestBody
import rx.Observable


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class DrawSketchModel : BaseModel(), DrawSketchContract.Model{

    override fun uploadInfo(body: RequestBody): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadInfo(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
}