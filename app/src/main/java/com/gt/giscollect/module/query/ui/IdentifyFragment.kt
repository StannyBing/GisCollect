package com.gt.giscollect.module.query.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.google.android.material.tabs.TabLayout
import com.gt.giscollect.R
import com.gt.giscollect.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.module.collect.bean.FileInfoBean
import com.gt.giscollect.module.collect.func.adapter.CollectFileAdapter
import com.gt.giscollect.module.collect.func.tool.InScrollGridLayoutManager
import com.gt.giscollect.module.collect.ui.FilePreviewActivity
import com.gt.giscollect.module.main.func.tool.MapTool
import com.gt.giscollect.module.query.func.adapter.SearchIdentifyAdapter
import com.gt.giscollect.module.query.func.tool.HighLightLayerTool
import com.gt.giscollect.module.query.mvp.contract.IdentifyContract
import com.gt.giscollect.module.query.mvp.model.IdentifyModel
import com.gt.giscollect.module.query.mvp.presenter.IdentifyPresenter
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXRecordUtil
import kotlinx.android.synthetic.main.fragment_identify.*

/**
 * Create By XB
 * 功能：要素查询
 */
class IdentifyFragment :BaseFragment<IdentifyPresenter, IdentifyModel>(), IdentifyContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): IdentifyFragment {
            val fragment = IdentifyFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    private var defaultListener: View.OnTouchListener? = null


    private val identifyList = arrayListOf<Pair<Field, Any?>>()
    private val identifyInfoAdapter = SearchIdentifyAdapter(identifyList)

    private val fileList = arrayListOf<FileInfoBean>()
    private val fileAdapter = CollectFileAdapter(fileList)

    private var recordUtil: ZXRecordUtil? = null

    private val identifyFeatures = arrayListOf<Feature>()

    private val highLightLayer = GraphicsOverlay()

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_identify
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        rv_search_identify_info.apply {
            layoutManager = ZXInScrollRecylerManager(requireActivity())
            adapter = identifyInfoAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
        rv_search_identify_file.apply {
            layoutManager = InScrollGridLayoutManager(requireActivity(), 2)
            adapter = fileAdapter
            fileAdapter.showDelete = false
        }

        tl_search_identify.apply {
            setSelectedTabIndicatorColor(ContextCompat.getColor(mContext, R.color.colorPrimary))
            setTabTextColors(ContextCompat.getColor(mContext, R.color.colorPrimary), ContextCompat.getColor(mContext, R.color.colorPrimary))
            setBackgroundColor(ContextCompat.getColor(mContext, R.color.content_bg))
            tabMode = TabLayout.MODE_SCROLLABLE
        }


        recordUtil = ZXRecordUtil(requireActivity())

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //文件点击事件
        fileAdapter.setOnItemClickListener { adapter, view, position ->
            if (fileList[position].type == "record" || fileList[position].type == "RECORD") {
                recordUtil?.playMedia(fileAdapter.fileParentPath + "/" + fileList[position].path)
            } else {
                FilePreviewActivity.startAction(requireActivity(), false, "文件预览", fileAdapter.fileParentPath + "/" + fileList[position].path)
            }
        }

        tl_search_identify.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                val feature = identifyFeatures[p0!!.tag as Int]
                clearAllFeatureSelect()
//                    tv_identify_layer_name.text = "图层:${feature.featureTable.tableName}"
                addHighLightFeature(feature)
                fileAdapter.fileParentPath = ConstStrings.getOperationalLayersPath() + feature.featureTable.displayName + "/file"
                identifyList.clear()
                fileList.clear()
                feature.featureTable.fields.forEach {
                    if (it.name in arrayOf("camera", "video", "record", "CAMERA", "VIDEO", "RECORD")) {
                        val paths = feature.attributes[it.name].toString().split(ConstStrings.File_Split_Char)
                        paths.forEach { path ->
                            if (path.length > 1 && path.isNotEmpty() && path != "null") {
                                fileList.add(FileInfoBean("", path = path, pathImage = path, type = it.name))
                            }
                        }
                    } else {
                        identifyList.add(it to feature.attributes[it.name])
                    }
                }
//                feature.attributes.keys.forEach {
//                    if (it in arrayOf("camera", "video", "record")) {
//                        val paths = feature.attributes[it].toString().split(ConstEntryStrings.File_Split_Char)
//                        paths.forEach { path ->
//                            if (path.length > 1 && path.isNotEmpty() && path != "null") {
//                                fileList.add(FileInfoBean("", path = path, pathImage = path, type = it))
//                            }
//                        }
//                    } else {
//                        identifyList.add(KeyValueEntity(it, feature.attributes[it].toString()))
//                    }
//                }
                fileAdapter.notifyDataSetChanged()
                identifyInfoAdapter.notifyDataSetChanged()
                if (identifyList.isNotEmpty()) {
                    rv_search_identify_info.scrollToPosition(0)
                }
            }
        })
    }

    /**
     * 清空所有要素选择
     */
    private fun clearAllFeatureSelect() {
        HighLightLayerTool.clearHighLight()
//        MapTool.mapListener?.getMap()?.basemap?.baseLayers?.forEach {
//            if (it is FeatureLayer) {
//                it.clearSelection()
//            }
//        }
//        MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
//            if (it is FeatureLayer) {
//                it.clearSelection()
//            }
//        }
    }

    /**
     * 设置要素信息
     */
    fun initIdentifyList(features: List<Feature>) {
        if (features.isNotEmpty()) {
            identifyFeatures.clear()
            tl_search_identify.removeAllTabs()
            features.forEachIndexed { index, it ->
                identifyFeatures.add(it)
                tl_search_identify.addTab(tl_search_identify.newTab().apply {
                    text = it.featureTable.featureLayer?.name ?: it.featureTable.tableName
                    tag = index
                })
            }
            sv_search_identify.smoothScrollTo(0, 0)
        }
    }

    fun closeQueryIdentify() {
        MapTool.mapListener?.getMapView()?.isMagnifierEnabled = false
        if (defaultListener != null) {
            MapTool.mapListener?.getMapView()?.onTouchListener = defaultListener
        }
        clearAllFeatureSelect()
    }

    /**
     * 添加高亮要素
     */
    fun addHighLightFeature(feature: Feature) {
        HighLightLayerTool.showHighLight(feature)
    }

}
