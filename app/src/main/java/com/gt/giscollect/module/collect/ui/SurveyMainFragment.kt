package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.giscollect.module.collect.mvp.contract.CollectMainContract
import com.gt.giscollect.module.collect.mvp.model.CollectMainModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectMainPresenter
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXFragmentUtil
import kotlinx.android.synthetic.main.fragment_collect_main.*

/**
 * Create By XB
 * 功能：采集
 */
class SurveyMainFragment : BaseFragment<CollectMainPresenter, CollectMainModel>(),
    CollectMainContract.View, FragChangeListener {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SurveyMainFragment {
            val fragment = SurveyMainFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        const val Survey_List = "调查列表"
        const val Survey_Feature = "要素编辑"
        const val Survey_Field = "属性编辑"
    }

    private lateinit var surveyListFragment: SurveyListFragment
    private lateinit var surveyFieldFragment: CollectFieldFragment
    private var surveyFeatureFragment: CollectFeatureFragment? = null

    private var currentFragType = Survey_List

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

        surveyListFragment = SurveyListFragment.newInstance()
        surveyFieldFragment = CollectFieldFragment.newInstance()
        surveyFeatureFragment = CollectFeatureFragment.newInstance()

        surveyListFragment.fragChangeListener = this
        surveyFieldFragment.fragChangeListener = this
        surveyFeatureFragment?.fragChangeListener = this

        ZXFragmentUtil.addFragments(childFragmentManager, arrayListOf<Fragment>().apply {
            add(surveyListFragment)
            add(surveyFeatureFragment!!)
            add(surveyFieldFragment)
        }, R.id.fm_collect_main, 0)

        tv_collect_title_name.text = "调查"

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
            Survey_List -> {

            }
            Survey_Feature -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
                onFragGoto(Survey_List)
            }
            Survey_Field -> {
                onFragGoto(Survey_Feature)
                surveyFeatureFragment?.reInit()
            }
        }
    }

    override fun onFragGoto(type: String, any: Any?) {
        currentFragType = type
        tv_collect_title_name.text = type
        when (type) {
            Survey_List -> {
                surveyListFragment.refresh()
                iv_collect_title_back.visibility = View.GONE
                ZXFragmentUtil.hideAllShowFragment(surveyListFragment)
            }
            Survey_Feature -> {
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(surveyFeatureFragment!!)
                any?.let {
                    if (it is FeatureLayer) {
                        surveyFeatureFragment?.excuteLayer(it, true, true, true)
                    } else if (it is Pair<*, *>) {
                        surveyFeatureFragment?.excuteLayer(
                            it.first as FeatureLayer,
                            (it.second as Array<Boolean>)[0],
                            (it.second as Array<Boolean>)[1],
                            clickEdit = true
                        )
                    } else {

                    }
                }
            }
            Survey_Field -> {
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
                iv_collect_title_back.visibility = View.VISIBLE
                ZXFragmentUtil.hideAllShowFragment(surveyFieldFragment)
                any?.let {
                    if (it is Feature) {
                        surveyFieldFragment.excuteField(it, true,2)
                    } else if (it is Pair<*, *>) {
                        surveyFieldFragment.excuteField(it.first as Feature, it.second as Boolean,2)
                    }
                }
            }
        }
    }

    fun reInit() {
        surveyFeatureFragment?.reInit()
    }

}
