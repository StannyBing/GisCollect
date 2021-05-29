package com.gt.entrypad.module.project.ui.change.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.CheckBean
import com.gt.base.app.ConstStrings
import com.gt.base.bean.NormalList
import com.gt.base.manager.UserManager
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.ui.change.mvp.contract.WorkListContract
import com.zx.zxutils.listener.ZXUnZipRarListener
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXUnZipRarUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class WorkListPresenter : WorkListContract.Presenter() {


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
            .subscribe(object : RxSubscriber<NormalList<CheckBean>>(mView) {
                override fun _onNext(t:  NormalList<CheckBean>?) {
                    t?.let {
                        mView.onProjectList(it.rows)
                    }
                    mView.dismissLoading()
                }

                override fun _onError(code: Int, message: String?) {
                    mView.handleError(code, message)
                    mView.dismissLoading()
                }
            })
    }
    override fun downloadProject(bean: CheckBean) {
        var fileName = ""
        var countLength = 0L
        var filePath = ""
        try {
            val fileObj = JSONObject(bean.fileJson)
            fileName = bean.getFileName()
            filePath = fileObj.getString("gpkgPath")
            countLength = fileObj.getLong("fileSize")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val saveFile = fileName.replace(".gpkg", ".zip")
        val downInfo = DownInfo(
            "file/downloadCollectFile?fileName=${fileName}&fileUri=${URLEncoder.encode(filePath)}"
        )
        downInfo.countLength = countLength
        downInfo.baseUrl = ApiConfigModule.BASE_IP
        if (ZXFileUtil.isFileExists(ConstStrings.getProjectCachePath() + saveFile)) {
            ZXFileUtil.deleteFiles(ConstStrings.getProjectCachePath() + saveFile)
        }
        downInfo.savePath = ConstStrings.getProjectCachePath() + saveFile
        downInfo.listener = object : DownloadOnNextListener<Any>() {
            override fun onNext(o: Any) {

            }

            override fun onStart() {
                mView.onDownloadProgress(0)
            }

            override fun onComplete(file: File) {
                dezipFile(fileName.replace(".gpkg", ""), file)
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
        if (ZXFileUtil.isFileExists(ConstStrings.getProjectCachePath() + saveFile)) {
            dezipFile(fileName.replace(".gpkg", ""), File(ConstStrings.getProjectCachePath() + saveFile))

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
                ConstStrings.getSketchLayersPath() + name + "/",
                object : ZXUnZipRarListener {
                    override fun onComplete(outputPath: String?) {
                        mView.dismissLoading()
                        val parentFile = File(outputPath)
                        parentFile.listFiles().forEach { child ->
                            if (child.isFile && child.name.endsWith(".gpkg")) {
                                mView.onCollectDownload(child)


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
}