package com.gt.giscollect.api

import com.frame.zxmvp.basebean.BaseRespose
import com.gt.base.bean.NormalList
import com.gt.base.manager.UserBean
import com.gt.base.app.CheckBean
import com.gt.giscollect.module.collect.bean.VersionBean
import com.gt.giscollect.module.system.bean.DataResBean
import com.gt.base.bean.GisServiceBean
import com.gt.giscollect.module.system.bean.TemplateBean
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import rx.Observable

interface ApiService {

    @POST("applogin")
    fun doLogin(@Body requestBody: RequestBody): Observable<BaseRespose<UserBean>>

    @POST("file/uploadCollectData")
    fun uploadCollect(@Body body: RequestBody): Observable<BaseRespose<String>>

    @POST("system/catalog/editMaterial")
    fun updateCollectInfo(@Body body: RequestBody): Observable<BaseRespose<String>>

    @POST("template/queryCollectTemplate")
    fun getTemplateList(@Body body: RequestBody): Observable<BaseRespose<NormalList<TemplateBean>>>

    @POST("system/catalog/queryAppMaterial")
    fun getDataResList(@Body body: RequestBody): Observable<BaseRespose<NormalList<DataResBean>>>

    @GET("system/catalog/hasDownload")
    fun checkFileAuth(@QueryMap map: HashMap<String, String>): Observable<BaseRespose<String>>

    @GET("env/appConfig")
    fun getAppConfig(): Observable<BaseRespose<String>>

    @POST("template/getCollectCheckInfo")
    fun getCheckList0(@Body body: RequestBody): Observable<BaseRespose<List<CheckBean>>>

    @POST("template/queryCollectAndCheckInfo")
//    @POST("template/queryCheckInfoAndStatus")
    fun getCheckList(@Body body: RequestBody): Observable<BaseRespose<NormalList<CheckBean>>>

    @POST("app/queryAppLatestVersion")
    fun getVersion(@Body body: RequestBody): Observable<BaseRespose<NormalList<VersionBean>>>

    @GET("system/employee/modifyPwd")
    fun changePwd(@QueryMap map: Map<String, String>): Observable<BaseRespose<String>>

    @GET("/system/user/loginAppMenus")
    fun getAppFuncs(@QueryMap map: Map<String, String>) : Observable<BaseRespose<Any>>

    @GET("/template/hasSameCollectName")
    fun checkMultiName(@QueryMap map: Map<String, String>) : Observable<BaseRespose<Boolean>>

    @POST("project/queryProjectWithAlais")
    fun getFieldList(@Body body: RequestBody): Observable<BaseRespose<NormalList<Any>>>
    @GET("/env/appCollectConfig")
    fun getSurveyType(): Observable<BaseRespose<String>>


    @POST("file/uploadCommonCollectData")
    fun uploadTanKan(@Body body: RequestBody): Observable<BaseRespose<String>>

    //在线服务
    @GET("/tGisService/queryUserGisServiceAll")
    fun getGisService(@QueryMap map: Map<String, String>) : Observable<BaseRespose<List<GisServiceBean>>>
}