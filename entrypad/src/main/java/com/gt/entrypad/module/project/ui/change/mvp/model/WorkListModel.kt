package com.gt.entrypad.module.project.ui.change.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.base.app.CheckBean
import com.gt.base.bean.NormalList
import com.gt.entrypad.api.ApiService

import com.gt.entrypad.module.project.ui.change.mvp.contract.WorkListContract
import okhttp3.RequestBody
import rx.Observable

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class WorkListModel : BaseModel(), WorkListContract.Model {

    override fun uploadData(body: RequestBody): Observable<String> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .uploadDraw(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

    override fun getProject(body: RequestBody): Observable<NormalList<CheckBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .projectList(body)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }
}