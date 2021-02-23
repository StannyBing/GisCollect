package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.app.ConstString
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_sketch_main.*
import java.io.Serializable
import java.lang.Exception

/**
 * create by 96212 on 2021/2/22.
 * Email 962123525@qq.com
 * desc
 */
class SketchMainFragment :BaseFragment<SketchMainPresenter,SketchMainModel>(),SketchMainContract.View,FragChangeListener{

    companion object {
        /**
         * 启动器
         */
        fun newInstance(vararg params:Serializable): SketchMainFragment {
            val fragment = SketchMainFragment()
            val bundle = Bundle()
            params.forEachIndexed { index, serializable ->
                bundle.putSerializable("p$index",serializable)
            }
            fragment.arguments = bundle
            return fragment
        }

        const val Sketch_Feature = "要素编辑"
        const val Sketch_Field = "登记信息"
    }

    private lateinit var sketchFieldFragment: SketchFiledFragment
    private lateinit var sketchFeatureFragment: SketchFeatureFragment
    private var currentFragType = Sketch_Feature

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_main
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        sketchFieldFragment = SketchFiledFragment.newInstance()
        sketchFeatureFragment = SketchFeatureFragment.newInstance()

        sketchFieldFragment.fragChangeListener = this
        sketchFeatureFragment.fragChangeListener = this

        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(sketchFeatureFragment)
            add(sketchFieldFragment)
        }, R.id.fm_sketch_main, 0)

        tv_collect_title_name.text = Sketch_Feature
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //返回
        iv_collect_title_back.setOnClickListener {
            onFragBack(currentFragType)
        }

    }

    override fun onFragBack(type: String, any: Any?) {
        when (type) {
            Sketch_Feature -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
            }
            Sketch_Field -> {
                onFragGoto(Sketch_Feature)
                sketchFeatureFragment?.reInit()
            }
        }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_collect_title_name.text = type
        when (type) {
            Sketch_Feature -> {
                iv_collect_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(sketchFeatureFragment)
            }
            Sketch_Field -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(sketchFieldFragment)
                any?.let {
                    if (it is Feature) {
                        sketchFieldFragment.excuteField(it, true)
                    } else if (it is Pair<*, *>) {
                        sketchFieldFragment.excuteField(it.first as Feature, it.second as Boolean)
                    }
                }
            }
        }
    }

    fun reInit() {
        sketchFeatureFragment.reInit()
    }

}