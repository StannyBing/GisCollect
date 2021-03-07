package com.gt.entrypad.module.project.mvp.presenter

import com.frame.zxmvp.baserx.RxHelper
import com.frame.zxmvp.baserx.RxSubscriber
import com.frame.zxmvp.http.download.DownInfo
import com.frame.zxmvp.http.download.listener.DownloadOnNextListener
import com.frame.zxmvp.http.download.manager.HttpDownManager
import com.frame.zxmvp.http.upload.UploadRequestBody
import com.gt.base.app.ConstStrings
import com.gt.entrypad.api.ApiConfigModule
import com.gt.entrypad.module.project.bean.HouseTableBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXSystemUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


/**
 * Create By admin On 2017/7/11
 * 功能：
 */
class DrawSketchPresenter : DrawSketchContract.Presenter() {


}