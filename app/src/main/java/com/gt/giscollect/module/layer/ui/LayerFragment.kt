package com.gt.giscollect.module.layer.ui

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.gt.giscollect.R
import com.gt.giscollect.base.BaseFragment
import com.gt.giscollect.module.layer.bean.LayerBean
import com.gt.giscollect.module.layer.func.adapter.BaseLayerAdapter
import com.gt.giscollect.module.layer.mvp.constract.LayerContract
import com.gt.giscollect.module.layer.mvp.model.LayerModel
import com.gt.giscollect.module.layer.mvp.presenter.LayerPresenter
import com.gt.giscollect.module.main.func.tool.MapTool
import kotlinx.android.synthetic.main.fragment_layer.*

/**
 * Create By XB
 * 功能：图层列表
 */
class LayerFragment : BaseFragment<LayerPresenter, LayerModel>(), LayerContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): LayerFragment {
            val fragment = LayerFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    //基础图层
    private val baseList = arrayListOf<LayerBean>()
    private val baseAdapter = BaseLayerAdapter(baseList)

    private lateinit var baeListFragment: LayerListFragment
    private lateinit var operationalListFragment: LayerListFragment
    private lateinit var legendFragment: LayerLegendFragment


    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_layer
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        baseList.add(LayerBean("矢量", "", isChecked = true, res = R.drawable.map_layer_base))
        baseList.add(LayerBean("影像", "", res = R.drawable.map_layer_base))
//        baseList.add(LayerBean("影像", "", res = R.drawable.map_layer_base))

        rv_layer_base.apply {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            adapter = baseAdapter
//            visibility = View.GONE
        }

        baeListFragment = LayerListFragment.newInstance(0)
        operationalListFragment = LayerListFragment.newInstance(1)
        legendFragment = LayerLegendFragment.newInstance()

        tvp_layer_main
            .setManager(childFragmentManager)
            .addTab(baeListFragment, "基础层")
            .addTab(operationalListFragment, "业务层")
//            .addTab(legendFragment, "图例")
            .setIndicatorHeight(5)
            .setTablayoutBackgroundColor(ContextCompat.getColor(mContext, R.color.content_bg))
            .setTitleColor(ContextCompat.getColor(mContext, R.color.colorPrimary), ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setTabTextSize(13)
            .setTabScrollable(false)
            .setViewpagerCanScroll(false)
            .setIndicatorColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setTablayoutHeight(40)
            .build()

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //底图
        baseAdapter.setOnItemClickListener { adapter, view, position ->
            baseList.forEach {
                it.isChecked = false
            }
            baseList[position].isChecked = true
            baseAdapter.notifyDataSetChanged()
            MapTool.mapListener?.getMap()?.basemap?.baseLayers?.forEach {
                it.isVisible = false
            }
            if (position == 0) {
                MapTool.mapListener?.getMap()?.basemap?.baseLayers?.get(0)?.isVisible = true
                MapTool.mapListener?.getMap()?.basemap?.baseLayers?.get(1)?.isVisible = true
            } else if (position == 1) {
                MapTool.mapListener?.getMap()?.basemap?.baseLayers?.get(2)?.isVisible = true
                MapTool.mapListener?.getMap()?.basemap?.baseLayers?.get(3)?.isVisible = true
            }
        }
    }

}
