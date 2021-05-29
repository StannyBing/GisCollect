package com.gt.giscollect.module.layer.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.gt.giscollect.R
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.module.collect.ui.CollectCreateFragment
import com.gt.giscollect.module.layer.bean.LayerBean
import com.gt.giscollect.module.layer.func.adapter.BaseLayerAdapter
import com.gt.giscollect.module.layer.func.tool.UriPathTool
import com.gt.giscollect.module.layer.mvp.constract.LayerContract
import com.gt.giscollect.module.layer.mvp.model.LayerModel
import com.gt.giscollect.module.layer.mvp.presenter.LayerPresenter
import com.gt.giscollect.module.main.func.tool.LayerTool
import com.gt.module_map.listener.MapListener
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXIntentUtil
import kotlinx.android.synthetic.main.fragment_layer.*
import java.io.File

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
            .setTitleColor(
                ContextCompat.getColor(mContext, R.color.colorPrimary),
                ContextCompat.getColor(mContext, R.color.colorPrimary)
            )
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
        //添加本地图层
        flb_layer_localfile.setOnClickListener {
            getPermission(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ZXDialogUtil.showInfoDialog(
                    mContext,
                    "提示",
                    "仅支持加载*.shp、*.kml文件类型"
                ) { dialog, which ->
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "*/*"
//                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(Intent.createChooser(intent, "文件导入"), 0x01)
                }
            }
//            LocalLayerActivity.startAction(requireActivity(), false)
        }
    }

    private fun loadLocalLayer(path: String) {
        MapTool.mapListener?.getMap()?.let {
            LayerTool.loadLocalFile(it, path) {
                showToast("加载成功")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0x01) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                data?.data?.let {
                    UriPathTool.getRealPath(mContext, it)?.let { it1 ->
                        loadLocalLayer(it1)
                    }
                }
            } else {
                data?.data?.path?.let {
                    loadLocalLayer(it)
                }
            }
        }
    }

}
