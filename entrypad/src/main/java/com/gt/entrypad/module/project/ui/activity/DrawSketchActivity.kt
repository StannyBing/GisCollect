package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.gt.base.activity.BaseActivity
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.model.DrawSketchModel
import com.gt.entrypad.module.project.mvp.presenter.DrawSketchPresenter
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import kotlinx.android.synthetic.main.layout_tool_bar.*

@Route(path =RouterPath.DRAW_SKETCH)
class DrawSketchActivity : BaseActivity<DrawSketchPresenter, DrawSketchModel>(),DrawSketchContract.View{
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, DrawSketchActivity::class.java)
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
            setData(TitleViewViewModel(getString(R.string.nextStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    GroundFigureActivity.startAction(this@DrawSketchActivity,false)
                }

            })
        }
        finishTv.apply {
            setData(TitleViewViewModel(getString(R.string.finish)))
            visibility = View.VISIBLE
        }
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sketch_draw
    }

}