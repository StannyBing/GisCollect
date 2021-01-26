package com.gt.entrypad.module.project.mvp.presenter

import android.util.Log
import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.ui.view.editText.EditTextViewViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class DrawSketchPresenter : DrawSketchContract.Presenter() {

    override fun uploadInfo(info: List<String>?, files: List<String>) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        if (!info.isNullOrEmpty()&&info.size==28){
            builder.addFormDataPart("txt8",info[3] )
            Log.e("editData","${info[3]}")
        }


        files.forEach {
            if (it.isNotEmpty()&&!(it.contains("http")||it.contains("https"))){
                builder.addFormDataPart(
                    "file",
                    it,
                    RequestBody.create(MediaType.parse("multipart/form-data"), File(it))
                )
            }
        }
        val uploadRequestBody =
            UploadRequestBody(
                builder.build(),
                UploadRequestBody.UploadListener { progress, done ->
                    mView.showLoading(
                        "上传中...",
                        progress
                    )
                })
        mModel.uploadInfo(uploadRequestBody)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<String>(mView) {
                override fun _onNext(t: String?) {
                    mView.uploadResult(t)
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