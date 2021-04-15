package com.gt.giscollect.module.survey.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.manager.UserManager
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.module.survey.mvp.contract.SurveyContract
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * Create By XB
 * 功能：
 */
class SurveyPresenter : SurveyContract.Presenter() {
    override fun uploadSurvey(
        file: String,
        name: String,
        templateId: String,
        catalogId: String,
        collectId: String
    ) {
        if (MyApplication.isOfflineMode) {
            mView.showToast("当前为离线模式，无法进行该操作！")
            return
        }
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        var originHttpFile = ""//上传过后的网络图片
//        builder.addFormDataPart("flag", "2")
//        builder.addFormDataPart("isOverwrite", "true")
        builder.addFormDataPart("templateId", templateId)
        builder.addFormDataPart("catalogId", catalogId)
//        builder.addFormDataPart("catalogId", "catalogId")
       // builder.addFormDataPart("layerName", name)
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

}