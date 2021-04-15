package com.gt.giscollect.module.survey.mvp.modle

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService

import com.gt.giscollect.module.layer.mvp.contract.LayerListContract
import com.gt.giscollect.module.survey.mvp.contract.SurveyContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class SurveyModel : BaseModel(), SurveyContract.Model {
    override fun uploadData(body: RequestBody): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadTanKan(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

}