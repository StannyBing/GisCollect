package com.gt.giscollect.module.system.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.giscollect.api.ApiService
import com.gt.giscollect.base.NormalList
import com.gt.giscollect.module.system.bean.TemplateBean

import com.gt.giscollect.module.system.mvp.contract.TemplateDownloadContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By XB
 * 功能：
 */
class TemplateDownloadModel : BaseModel(), TemplateDownloadContract.Model {
    override fun templateListData(requestBody: RequestBody): Observable<NormalList<TemplateBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getTemplateList(requestBody)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }


}