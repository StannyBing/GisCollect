package com.gt.entrypad.module.project.mvp.model

import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSchedulers
import com.gt.base.bean.NormalList
import com.gt.entrypad.api.ApiService
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.contract.DrawTemplateContract
import okhttp3.RequestBody
import org.json.JSONObject
import rx.Observable


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class DrawTemplateModel : BaseModel(), DrawTemplateContract.Model{
    override fun templateListData(requestBody: RequestBody): Observable<NormalList<DrawTemplateBean>> {
        return mRepositoryManager.obtainRetrofitService(ApiService::class.java)
            .getDrawTemplateList(requestBody)
            .compose(RxHelper.handleResult())
            .compose(RxSchedulers.io_main())
    }

}