package com.gt.giscollect.module.collect.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.giscollect.api.ApiConfigModule
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.base.NormalList
import com.gt.base.app.CheckBean
import com.gt.giscollect.module.collect.mvp.contract.CollectListContract
import com.gt.base.manager.UserManager
import com.gt.giscollect.module.collect.mvp.contract.SurveyListContract
import com.gt.giscollect.module.system.bean.DataResBean
import com.tencent.bugly.crashreport.CrashReport
import com.zx.zxutils.listener.ZXUnZipRarListener
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXUnZipRarUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder


/**
 * Create By XB
 * 功能：
 */
class SurveyListPresenter : SurveyListContract.Presenter() {

    override fun getSurveyDataList(requestBody: RequestBody) {
        if (MyApplication.isOfflineMode) {
            return
        }
        mModel.surveyListData(requestBody)
            .compose(RxHelper.bindToLifecycle(mView))
            .subscribe(object : RxSubscriber<NormalList<DataResBean>>(mView) {
                override fun _onNext(t: NormalList<DataResBean>?) {
                    if (t != null) {
                        mView.onSurveyListResult(t)
                    }
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                }

            })
    }

    override fun downloadSurvey(dataBean: DataResBean) {
        var filePath = ""
        var fileName = ""
        var countLength = 0L
        try {
            val fileObj = JSONArray(dataBean.fileJson).getJSONObject(0)
//            fileName = fileObj.getString("fileName")
            fileName = dataBean.materialName + "." + fileObj.getString("fileExt")
            filePath = fileObj.getString("fileUri")
            countLength = fileObj.getLong("fileSize")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val downInfo =
            DownInfo("file/downloadFile?fileUri=${filePath}@@${dataBean.catalogId}&fileName=${fileName}")
        downInfo.countLength = countLength
        downInfo.baseUrl = ApiConfigModule.BASE_IP
        val localPath = ConstStrings.getSurveyPath() + dataBean.materialName + "/" + fileName
        if (!File(localPath).parentFile.exists()) {
            File(localPath).parentFile.mkdirs()
        }
        if (ZXFileUtil.isFileExists(localPath)) {
            ZXFileUtil.deleteFiles(localPath)
        }

        downInfo.savePath = localPath
        downInfo.listener = object : DownloadOnNextListener<Any>() {
            override fun onNext(o: Any) {

            }

            override fun onStart() {
                mView.onDownloadProgress(0)
            }

            override fun onComplete(file: File) {
                mView.onSurveyDownload(file)
                mView.dismissLoading()
            }

            override fun onError(message: String?) {
                mView.showToast(message)
                mView.dismissLoading()
            }

            override fun updateProgress(progress: Int) {
                mView.onDownloadProgress(progress)
            }
        }
        if (ZXFileUtil.isFileExists(localPath)) {
            mView.onSurveyDownload(File(localPath))
        } else {
            HttpDownManager.getInstance().startDown(downInfo) { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Cookie", ConstStrings.Cookie)
                    .build()
                chain.proceed(request)
            }
        }
    }

    private fun dezipFile(name: String, file: File) {
        try {
            ZXUnZipRarUtil.unZip(
                file.path,
                ConstStrings.getOperationalLayersPath() + name + "/",
                object : ZXUnZipRarListener {
                    override fun onComplete(outputPath: String?) {
                        mView.dismissLoading()
                        val parentFile = File(outputPath)
                        parentFile.listFiles().forEach { child ->
                            if (child.isFile && child.name.endsWith(".gpkg")) {
                                mView.onSurveyDownload(child)


//                                MapTool.mapListener?.getMap()?.let {
//                                    LayerTool.addLocalMapLayer(it, child)
//                                }
                            }
                        }
                    }

                    override fun onPregress(p0: Int) {
                    }

                    override fun onError(p0: String?) {
                        mView.dismissLoading()
                        mView.showToast("解压失败，请稍后再试")
                    }

                    override fun onStart() {
                        mView.showLoading("正在解压中...")
                    }
                })

//            val destDir = File(ConstEntryStrings.getOperationalLayersPath() + name)
//            destDir.mkdirs()
//            ZXUnZipRarUtil.unZip(
//                file.path,
//                ConstEntryStrings.getCachePath(),
//                object : ZXUnZipRarListener {
//                    override fun onComplete(outputpath: String?) {
//                        mView.dismissLoading()
//                        if (outputpath == null) return
//                        val parentFile = File(outputpath)
//                        if (parentFile.isFile) return
//                        val childFiles = parentFile.listFiles()
//                        childFiles.forEach {
//                            if (it.isFile) {
//                                val gpkgFile = ZXFileUtil.copyFile(it.path, destDir.path + it.name)
//                                if (gpkgFile.name.endsWith(".gpkg")) {
//                                    MapTool.mapListener?.getMap()?.let {
//                                        LayerTool.addLocalMapLayer(it, file)
//                                    }
//                                }
//                            }
//                            if (it.isDirectory) {
//                                it.listFiles().forEach { inFile ->
//                                    ZXFileUtil.copyFile(
//                                        inFile.path,
//                                        destDir.path + it.name + inFile.name
//                                    )
//                                }
//                            }
//                        }
//                        mView.onCollectDownload(destDir)
//                    }
//
//                    override fun onPregress(p0: Int) {
//                    }
//
//                    override fun onError(p0: String?) {
//                        mView.dismissLoading()
//                        mView.showToast("解压失败，请稍后再试")
//                    }
//
//                    override fun onStart() {
//                        mView.showLoading("正在解压中...")
//                    }
//
//                })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

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
        builder.addFormDataPart("layerName", name)
        builder.addFormDataPart("userId", UserManager.user?.userId)
        if (collectId.isNotEmpty()) builder.addFormDataPart("collectId", collectId)
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