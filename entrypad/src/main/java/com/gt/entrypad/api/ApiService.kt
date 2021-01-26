package com.gt.entrypad.api

import com.frame.zxmvp.basebean.BaseRespose
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable


interface ApiService {
    @POST("office/word/createReportCKB")
    fun uploadInfo(@Body body: RequestBody): Observable<BaseRespose<String>>
}