package com.gt.entrypad.module.project.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_sketch_main.*
import java.io.Serializable

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
        const val Sketch_Create="图层新增"
        const val Sketch_Feature = "要素编辑"
        const val Sketch_Field = "登记信息"
    }
    private lateinit var sketchCreateFragment: SketchCreateFragment
    private lateinit var sketchFieldFragment: SketchFiledFragment
    private lateinit var sketchFeatureFragment: SketchFeatureFragment
    private var currentFragType =
        Sketch_Create

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
        sketchCreateFragment = SketchCreateFragment.newInstance()
        sketchFieldFragment = SketchFiledFragment.newInstance()
        sketchFeatureFragment = SketchFeatureFragment.newInstance()
        sketchFieldFragment.fragChangeListener = this
        sketchFeatureFragment.fragChangeListener = this
        sketchCreateFragment.fragChangeListener = this

        mSharedPrefUtil.getBool("isEdit",false).apply {
            if (this){
                currentFragType =
                    Sketch_Feature
                ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
                    add(sketchFeatureFragment)
                    add(sketchFieldFragment)
                }, R.id.fm_sketch_main, 0)
            }else{
                currentFragType =
                    Sketch_Create
                ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
                    add(sketchCreateFragment)
                    add(sketchFeatureFragment)
                    add(sketchFieldFragment)
                }, R.id.fm_sketch_main, 0)
            }
        }
        tv_collect_title_name.text = currentFragType
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
                onFragGoto(Sketch_Create)
            }
            Sketch_Field -> {
                sketchFieldFragment.reInit()
                onFragGoto(Sketch_Feature)
                sketchFeatureFragment?.reInit()
            }

        }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_collect_title_name.text = type
        when (type) {
            Sketch_Create ->{
                iv_collect_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(sketchCreateFragment)
            }
            Sketch_Feature -> {
                ZXFragmentUtil.hideAllShowFragment(sketchFeatureFragment)
                any?.let {
                    sketchFeatureFragment.excuteLayer(it as FeatureLayer,false)
                }
                iv_collect_title_back.visibility = if (mSharedPrefUtil.getBool("isEdit",false))View.VISIBLE else View.GONE
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

    override fun onDestroy() {
        reInit()
        super.onDestroy()
    }
    fun reInit() {
        sketchFeatureFragment.reInit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        sketchFeatureFragment?.onActivityResult(requestCode,resultCode,data)
    }
}