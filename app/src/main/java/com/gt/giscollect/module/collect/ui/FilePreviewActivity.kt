package com.gt.giscollect.module.collect.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXLogUtil
import java.io.File
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gt.base.activity.BaseActivity
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.mvp.contract.FilePreviewContract
import com.gt.giscollect.module.collect.mvp.model.FilePreviewModel
import com.gt.giscollect.module.collect.mvp.presenter.FilePreviewPresenter
import kotlinx.android.synthetic.main.activity_file_preview.*


/**
 * Create By admin On 2017/7/11
 * 功能：文件预览
 */
class FilePreviewActivity : BaseActivity<FilePreviewPresenter, FilePreviewModel>(), FilePreviewContract.View {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean, name: String, path: String) {
            val intent = Intent(activity, FilePreviewActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("path", path)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_sketch_file_preview
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val name = intent.getStringExtra("name")
        val path = intent.getStringExtra("path")
        ZXDialogUtil.showLoadingDialog(this, "正在加载文件...")
        getPermission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            if (path.endsWith("jpg") || path.endsWith("png")) {
                pv_preview_image.visibility = View.VISIBLE
                vv_preview_video.visibility = View.GONE

                Glide.with(this).load(path)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            ZXDialogUtil.dismissLoadingDialog()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            ZXDialogUtil.dismissLoadingDialog()
                            return false
                        }
                    })
                    .into(pv_preview_image)
            } else {
                pv_preview_image.visibility = View.GONE
                vv_preview_video.visibility = View.VISIBLE
                vv_preview_video.setMediaController(MediaController(this))
//            vv_preview_video.setOnErrorListener { mp, what, extra -> vv_preview_video. }
                vv_preview_video.setOnCompletionListener { vv_preview_video.start() }
                val uri: Uri
                if (path.startsWith("http")) {
//                mPresenter.downloadFile(hazardname, path)
                    uri = Uri.parse(path)
                    vv_preview_video.setVideoURI(uri)
                } else {
                    uri = Uri.fromFile(File(path))
                    vv_preview_video.setVideoURI(uri)
                }
                vv_preview_video.start()
                vv_preview_video.setOnPreparedListener {
                    ZXLogUtil.loge(it.isPlaying.toString())
                }
//                onFileDownloadResult(File(path))
                ZXDialogUtil.dismissLoadingDialog()
            }
        }
    }

    override fun onFileDownloadResult(file: File) {
        val uri = Uri.fromFile(file)
        vv_preview_video.setVideoURI(uri)
        vv_preview_video.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (vv_preview_video != null) {
            vv_preview_video.stopPlayback()
            vv_preview_video.suspend()
        }
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        iv_preview_back.setOnClickListener { finish() }
    }

}
