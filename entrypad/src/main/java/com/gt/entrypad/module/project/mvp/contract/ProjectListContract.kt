package com.gt.entrypad.module.project.mvp.contract

import com.frame.zxmvp.base.BasePresenter
import com.frame.zxmvp.base.IView
import com.frame.zxmvp.base.IModel
import com.gt.base.app.CheckBean
import com.gt.base.bean.NormalList
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.bean.HouseTableBean
import okhttp3.RequestBody
import rx.Observable
import java.io.File

/**
 * Create By admin On 2017/7/11
 * 功能：
 */
interface ProjectListContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View : IView {
        fun onSurveyUpload(name: String)

        fun onProjectList(result: List<CheckBean>?)
        fun onDownloadProgress(progress: Int)

        fun onCollectDownload(file: File)
    }
    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model : IModel{
        fun uploadData(body: RequestBody): Observable<String>

        fun getProject(body: RequestBody):Observable< NormalList<CheckBean>>
    }
	
	//方法
	abstract class Presenter : BasePresenter<View, Model>(){
        abstract fun getProject(body: RequestBody)
        abstract fun downloadProject(bean: CheckBean)
        abstract fun uploadSurvey(
            file: String,
            name: String,
            templateId: String,
            catalogId: String ,
            collectId: String
        )
    }
}

