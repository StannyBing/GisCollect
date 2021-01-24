package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.base.FragChangeListener
import com.gt.giscollect.module.collect.mvp.contract.CollectMainContract
import com.gt.giscollect.module.collect.mvp.model.CollectMainModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectMainPresenter
import com.gt.giscollect.module.main.func.tool.MapTool
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_collect_main.*

/**
 * Create By XB
 * 功能：采集
 */
class CollectMainFragment : BaseFragment<CollectMainPresenter, CollectMainModel>(),
    CollectMainContract.View, FragChangeListener {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectMainFragment {
            val fragment = CollectMainFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        const val Collect_List = "采集列表"
        const val Collect_Check = "采集审核"
        const val Collect_Create = "图层新增"
        const val Collect_Feature = "要素编辑"
        const val Collect_Field = "属性编辑"
    }

    private lateinit var collectListFragment: CollectListFragment
    private lateinit var collectCreateFragment: CollectCreateFragment
    private lateinit var collectFieldFragment: CollectFieldFragment
    private var collectFeatureFragment: CollectFeatureFragment? = null
    private lateinit var collectCheckFragment: CollectCheckFragment

    private var currentFragType = Collect_List

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_main
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {

        collectListFragment = CollectListFragment.newInstance()
        collectCreateFragment = CollectCreateFragment.newInstance()
        collectFieldFragment = CollectFieldFragment.newInstance()
        collectFeatureFragment = CollectFeatureFragment.newInstance()
        collectCheckFragment = CollectCheckFragment.newInstance()

        collectListFragment.fragChangeListener = this
        collectCreateFragment.fragChangeListener = this
        collectFieldFragment.fragChangeListener = this
        collectCheckFragment.fragChangeListener = this
        collectFeatureFragment?.fragChangeListener = this

        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(collectListFragment)
            add(collectCheckFragment)
            add(collectCreateFragment)
            add(collectFeatureFragment!!)
            add(collectFieldFragment)
        }, R.id.fm_collect_main, 0)

        tv_collect_title_name.text = "采集"

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
            Collect_List -> {

            }
            Collect_Check -> {
                onFragGoto(Collect_List)
            }
            Collect_Create -> {
                onFragGoto(Collect_List)
            }
            Collect_Feature -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
                onFragGoto(Collect_List)
            }
            Collect_Field -> {
                onFragGoto(Collect_Feature)
                collectFeatureFragment?.reInit()
            }
        }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_collect_title_name.text = type
        when (type) {
            Collect_List -> {
                collectListFragment.refresh()
                iv_collect_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(collectListFragment)
            }
            Collect_Check -> {
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(collectCheckFragment)
                collectCheckFragment.reInit()
            }
            Collect_Create -> {
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(collectCreateFragment)
                collectCreateFragment.reInit()
            }
            Collect_Feature -> {
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(collectFeatureFragment!!)
                any?.let {
                    if (it is FeatureLayer) {
                        collectFeatureFragment?.excuteLayer(it, true, true)
                    } else if (it is Pair<*, *>) {
                        collectFeatureFragment?.excuteLayer(
                            it.first as FeatureLayer,
                            (it.second as Array<Boolean>)[0],
                            (it.second as Array<Boolean>)[1]
                        )
                    } else {

                    }
                }
            }
            Collect_Field -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(collectFieldFragment)
                any?.let {
                    if (it is Feature) {
                        collectFieldFragment.excuteField(it, true)
                    } else if (it is Pair<*, *>) {
                        collectFieldFragment.excuteField(it.first as Feature, it.second as Boolean)
                    }
                }
            }
        }
    }

    fun reInit() {
        collectFeatureFragment?.reInit()
    }

}
