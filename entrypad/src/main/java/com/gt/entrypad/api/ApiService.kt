package com.gt.entrypad.api

import com.frame.zxmvp.basebean.BaseRespose
import com.gt.entrypad.module.project.bean.HouseTableBean
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable


interface ApiService {
    @POST("office/word/createReportCKB2")
    fun uploadInfo(@Body body: RequestBody): Observable<BaseRespose<HouseTableBean>>


}