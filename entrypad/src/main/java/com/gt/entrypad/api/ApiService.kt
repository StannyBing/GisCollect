package com.gt.entrypad.api

import com.frame.zxmvp.basebean.BaseRespose
import com.gt.base.app.CheckBean
import com.gt.base.bean.NormalList
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.bean.HouseTableBean
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable


interface ApiService {
    @POST("/office/word/createZDDJBFT")
    fun zddWorld(@Body body: RequestBody): Observable<BaseRespose<HouseTableBean>>

    @POST("/office/word/createFWDJBFT")
    fun fwdWorld(@Body body: RequestBody): Observable<BaseRespose<HouseTableBean>>

    @POST("/template/queryCollect")
    fun projectList(@Body body: RequestBody): Observable<BaseRespose<NormalList<CheckBean>>>

    @POST("template/queryCollectTemplate")
    fun getDrawTemplateList(@Body body: RequestBody): Observable<BaseRespose<NormalList<DrawTemplateBean>>>
    @POST("file/uploadCollectData")
    fun uploadDraw(@Body body: RequestBody): Observable<BaseRespose<String>>

}