package com.gt.giscollect.module.collect.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.base.bean.NormalList

import com.gt.giscollect.module.collect.mvp.contract.SurveyListContract
import com.gt.giscollect.module.system.bean.DataResBean
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class SurveyListModel : BaseModel(), SurveyListContract.Model {

    override fun surveyListData(requestBody: RequestBody): Observable<NormalList<DataResBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getDataResList(requestBody)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun uploadData(body: RequestBody): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadCollect(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun updateInfoData(body: RequestBody): Observable<Any> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .updateCollectInfo(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

}