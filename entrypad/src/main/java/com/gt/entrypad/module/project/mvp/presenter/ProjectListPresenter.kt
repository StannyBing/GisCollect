package com.gt.entrypad.module.project.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.ConstStrings
import com.gt.base.bean.NormalList
import com.gt.base.manager.UserManager
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.bean.DrawTemplateBean
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.ProjectListContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class ProjectListPresenter : ProjectListContract.Presenter() {

    override fun uploadSurvey(
        file: String,
        name: String,
        templateId: String,
        catalogId: String,
        collectId: String
    ) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        var originHttpFile = ""//上传过后的网络图片
//        builder.addFormDataPart("flag", "2")
//        builder.addFormDataPart("isOverwrite", "true")
        builder.addFormDataPart("templateId", templateId)
        builder.addFormDataPart("catalogId", catalogId)
//        builder.addFormDataPart("catalogId", "catalogId")
        builder.addFormDataPart("layerName", name)
        builder.addFormDataPart("userId", UserManager.user?.userId)
        //  if (collectId.isNotEmpty()) builder.addFormDataPart("collectId", collectId)
        builder.addFormDataPart(
            "file",
            file,
            RequestBody.create(MediaType.parse("multipart/form-data"), File(file))
        )
        val uploadRequestBody =
            UploadRequestBody(
                builder.build(),
                UploadRequestBody.UploadListener { progress, done ->
                    mView.showLoading(
                        "上传中...",
                        progress
                    )
                })
        mModel.uploadData(uploadRequestBody)
            .compose(RxHelper.bindToLifecycle(mView))
//            .flatMap {
//                mModel.updateInfoData(
//                    hashMapOf(
//                        "materialName" to name,
//                        "catalogId" to "6f38bfad-ab3a-4bde-b485-8efcdd29bb9a",
//                        "maType" to "FS",
//                        "imFlag" to "N",
//                        "maYear" to Calendar.getInstance().get(Calendar.YEAR).toString(),
////                        "created" to Date().toString().replace("GMT+08:00", "CST"),
//                        "userId" to UserManager.user?.userId,
//                        "rnCode" to UserManager.user?.rnCode,
//                        "fileJson" to
//                                Gson().toJson(
//                                    arrayListOf(
//                                        hashMapOf(
//                                            "fileName" to it.fileName,
//                                            "fileExt" to it.ext,
//                                            "fileUri" to it.localUri,
//                                            "fileSize" to it.fileSize
//                                        )
//                                    )
//                                )
//                    ).toJson()
//                )
//            }
            .subscribe(object : RxSubscriber<String>(mView) {
                override fun _onNext(t: String?) {
                    mView.onSurveyUpload(name)
                    mView.dismissLoading()
                    mView.showToast("上传成功")
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                    mView.dismissLoading()
                }
            })
    }
    override fun getProject(body: RequestBody) {
        mModel.getProject(body)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<String>(mView) {
                override fun _onNext(t: String?) {
                    mView.onProjectList(t)
                    mView.dismissLoading()
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                    mView.dismissLoading()
                }
            })
    }
}