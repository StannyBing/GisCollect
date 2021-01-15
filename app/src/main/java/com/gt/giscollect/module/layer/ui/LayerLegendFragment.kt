package com.gt.giscollect.module.layer.ui

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.layers.Layer
import com.gt.giscollect.R
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.base.BaseFragment
import com.gt.giscollect.module.layer.func.adapter.LegendAdapter
import com.gt.giscollect.module.layer.mvp.contract.LayerLegendContract
import com.gt.giscollect.module.layer.mvp.model.LayerLegendModel
import com.gt.giscollect.module.layer.mvp.presenter.LayerLegendPresenter
import com.gt.giscollect.module.main.func.listener.MapListener
import com.gt.giscollect.module.main.func.tool.MapTool
import kotlinx.android.synthetic.main.fragment_layer_legend.*
import rx.functions.Action1

/**
 * Create By XB
 * 功能：图层-图例
 */
class LayerLegendFragment : BaseFragment<LayerLegendPresenter, LayerLegendModel>(), LayerLegendContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): LayerLegendFragment {
            val fragment = LayerLegendFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    private val layerList = arrayListOf<Layer>()
    private val legendAdapter = LegendAdapter(layerList)

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_layer_legend
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_layer_legend.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = legendAdapter
        }

        refreshData()
        mRxManager.on(ConstStrings.RxLayerChange, Action1<Boolean> {
            refreshData()
        })
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {

    }

    private fun refreshData() {
        layerList.clear()
        if (MapTool.mapListener?.getMap()?.operationalLayers?.isNotEmpty() == true) {
            MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
                if (it.isVisible) {
                    layerList.add(it)
                }
            }
        }
        legendAdapter.notifyDataSetChanged()
    }
}
