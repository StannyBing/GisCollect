package com.gt.entrypad.module.project.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import com.gt.base.activity.BaseActivity
import com.gt.entrypad.R
import com.gt.entrypad.module.project.mvp.contract.MapContract
import com.gt.entrypad.module.project.mvp.model.MapModel
import com.gt.entrypad.module.project.mvp.presenter.MapPresenter
import com.gt.entrypad.module.project.ui.fragment.MapFragment
import com.gt.entrypad.module.project.ui.fragment.SketchFiledFragment
import com.gt.entrypad.module.project.ui.fragment.SketchMainFragment
import com.zx.zxutils.util.ZXFragmentUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.activity_sketch_load.*

class SketchLoadActivity :BaseActivity<MapPresenter,MapModel>(),MapContract.View{
    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, SketchLoadActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ZXFragmentUtil.addFragment(supportFragmentManager,MapFragment.newInstance(),R.id.mapFl)
        iv_data_show.performClick()
        ZXFragmentUtil.addFragment(supportFragmentManager,SketchMainFragment.newInstance(),R.id.fm_data)
    }

    override fun onViewListener() {
        //收起菜单
        iv_data_hide.setOnClickListener {
            rl_main_data.animation =
                TranslateAnimation(0f, ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f)
                    .apply {
                        duration = 500
                        start()
                    }
            rl_main_data.visibility = View.GONE
            iv_data_show.visibility = View.VISIBLE
        }
        //打开菜单
        iv_data_show.setOnClickListener {
            if (rl_main_data.visibility != View.VISIBLE) {
                rl_main_data.animation =
                    TranslateAnimation(ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f, 0f)
                        .apply {
                            duration = 500
                            start()
                        }
                rl_main_data.visibility = View.VISIBLE
            }
            iv_data_show.visibility = View.GONE
        }
    }

    override fun getLayoutId(): Int {
       return R.layout.activity_sketch_load
    }

}