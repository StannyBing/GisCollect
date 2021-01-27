package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.bean.InputInfoBean
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.model.DrawSketchModel
import com.gt.entrypad.module.project.mvp.presenter.DrawSketchPresenter
import com.gt.entrypad.module.project.ui.view.photoView.PhotoViewViewModel
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import kotlinx.android.synthetic.main.layout_tool_bar.*

@Route(path =RouterPath.DRAW_SKETCH)
class DrawSketchActivity : BaseActivity<DrawSketchPresenter, DrawSketchModel>(),DrawSketchContract.View{
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean,data:ArrayList<PhotoViewViewModel>,infoData:ArrayList<String>) {
            val intent = Intent(activity, DrawSketchActivity::class.java)
            intent.putExtra("photoData",data)
            intent.putExtra("infoData",infoData)
           activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolBarTitleTv.text = getString(R.string.sketchDraw)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    ActivityCompat.finishAfterTransition(this@DrawSketchActivity)
                }

            })
        }
        rightTv.apply {
            visibility= View.VISIBLE
            setData(TitleViewViewModel(getString(R.string.submit)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    val arrayList = arrayListOf<String>().apply {
                        add("房屋图.docx")
                        add("宗地图.docx")
                        add("渝北现场查勘表.docx")
                    }
                    //信息上传
                    ZXDialogUtil.showListDialog(mContext,"生成图形","确定", arrayList,DialogInterface.OnClickListener { dialog, which ->
                        uploadInfo(arrayList[which])
                    },DialogInterface.OnClickListener { dialog, which ->

                    })
                }

            })
        }
        finishTv.apply {
            setData(TitleViewViewModel(getString(R.string.finish)))
            visibility = View.GONE
        }
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sketch_draw
    }
    /**
     * 上传填写信息
     */
    private fun uploadInfo(tplName:String){
        var fileData = arrayListOf<String>()
        val photoList = intent?.getSerializableExtra("photoData") as ArrayList<PhotoViewViewModel>
        var infoData = if (intent.hasExtra("infoData")) intent.getSerializableExtra("infoData") as ArrayList<String> else arrayListOf()
        //获取文件信息
        photoList.forEach {
            fileData.add(it.url)
        }
        //获取草图
        val sketch = mContext.filesDir.path + "/sketch/draw.jpg"
        if (ZXFileUtil.isFileExists(sketch)) fileData.add(sketch)
        mPresenter.uploadInfo(infoData,fileData,tplName)
    }

    /**
     * 上传回调接口
     */
    override fun uploadResult(uploadResult: String?) {
        ResultShowActivity.startAction(this,false)
    }
}