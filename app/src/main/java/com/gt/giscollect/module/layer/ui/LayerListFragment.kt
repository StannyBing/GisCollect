package com.gt.giscollect.module.layer.ui

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.VectorTileCache
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.gt.giscollect.R
import com.gt.giscollect.app.ConstStrings
import com.gt.giscollect.base.AppInfoManager
import com.gt.base.fragment.BaseFragment
import com.gt.giscollect.module.layer.func.adapter.DataLayerAdapter
import com.gt.giscollect.module.layer.mvp.contract.LayerListContract
import com.gt.giscollect.module.layer.mvp.model.LayerListModel
import com.gt.giscollect.module.layer.mvp.presenter.LayerListPresenter
import com.gt.giscollect.module.main.func.tool.MapTool
import com.gt.giscollect.module.main.func.tool.StyleFileTool
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_layer_list.*
import org.json.JSONObject
import java.io.File

/**
 * Create By XB
 * 功能：图层-列表
 */
class LayerListFragment : BaseFragment<LayerListPresenter, LayerListModel>(),
    LayerListContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(type: Int): LayerListFragment {
            val fragment = LayerListFragment()
            val bundle = Bundle()
            bundle.putInt("type", type)
            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "layer_list"
    }

    private var type: Int = 0//0：基础层       1：业务层

    //图层列表
    private val dataList = arrayListOf<Layer>()
    private val dataAdapter = DataLayerAdapter(dataList)


    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_layer_list
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        type = arguments?.getInt("type") ?: 0

        rv_layer_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = dataAdapter
            addItemDecoration(SimpleDecoration(requireActivity()))
        }
        if (type == 0) {
//            btn_layer_import.visibility = View.VISIBLE
            dataList.addAll(MapTool.mapListener?.getMap()?.basemap?.baseLayers ?: arrayListOf())
        } else {
//            btn_layer_import.visibility = View.GONE
            dataList.addAll(MapTool.mapListener?.getMap()?.operationalLayers ?: arrayListOf())
        }
        MapTool.registerLayerChange(ChangeTag, object : MapTool.LayerChangeListener {
            override fun onLayerChange(layer: Layer, type: MapTool.ChangeType) {
                rv_layer_list?.let {
                    if (this@LayerListFragment.type == 0) {
                        if (type == MapTool.ChangeType.BaseAdd) {
                            dataList.add(layer)
                        } else if (type == MapTool.ChangeType.BaseRemove) {
                            dataList.remove(layer)
                        }
                    } else {
                        if (type == MapTool.ChangeType.OperationalAdd) {
                            dataList.add(layer)
                        } else if (type == MapTool.ChangeType.OperationalRemove) {
                            dataList.remove(layer)
                        }
                    }
                    dataAdapter.notifyDataSetChanged()
                }
            }
        })
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        ZXRecyclerDeleteHelper(requireActivity(), rv_layer_list)
            .setSwipeOptionViews(R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                //滑动菜单点击事件
                if (id == R.id.tv_delete) {
                    if (MapTool.mapListener?.getMap()?.basemap?.baseLayers?.contains(dataList[pos]) == true) {
                        MapTool.mapListener?.getMap()?.basemap?.baseLayers?.remove(dataList[pos])
                    }
                    dataList.removeAt(pos)
                    dataAdapter.notifyItemRemoved(pos)
                }
            }
        //数据
        dataAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.iv_layer_checked) {
//                val visible = dataList[position].isVisible
                dataList[position].isVisible = !dataList[position].isVisible
                dataAdapter.notifyItemChanged(position)
//                if (!visible) {
//                    moveToLayer(position)
//                }
            } else if (view.id == R.id.tv_layer_name) {
                moveToLayer(position)
            }
            mRxManager.post(ConstStrings.RxLayerChange, true)
        }
        //导入离线图层
        btn_layer_import.setOnClickListener {
            getPermission(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                loadImport()
            }
        }
    }

    private fun loadImport() {
        val localMaps = getLocalMap(ConstStrings.getLocalMapPath())
        val names = arrayListOf<String>()
        localMaps.forEach {
            names.add(it.substring(it.lastIndexOf("/") + 1))
        }
        if (localMaps.isNotEmpty()) {
            val boolCheckArray = BooleanArray(localMaps.size)
            ZXDialogUtil.showCheckListDialog(
                mContext,
                "请选择加载的离线地图（请将离线地图包拷贝至相应位置）",
                names.toTypedArray(),
                boolCheckArray,
                { dialog: DialogInterface?, which: Int, isChecked: Boolean ->
                    boolCheckArray[which] = isChecked
                }, { dialog, which ->
                    localMaps.forEachIndexed { index, it ->
                        if (boolCheckArray[index]) {
                            if (ZXFileUtil.isFileExists(it)) {
                                when (it.substring(it.lastIndexOf(".") + 1)) {
                                    "vtpk" -> {
                                        val localCache = VectorTileCache(it)
                                        val localMap = ArcGISVectorTiledLayer(localCache)
                                        localMap.name = it.substring(
                                            it.lastIndexOf("/") + 1,
                                            it.lastIndexOf(".")
                                        )
                                        MapTool.postLayerChange(
                                            ChangeTag,
                                            localMap,
                                            MapTool.ChangeType.BaseAdd
                                        )
                                        dataList.add(localMap)
                                        dataAdapter.notifyDataSetChanged()
                                        localMap.addDoneLoadingListener {
                                            moveToLayer(dataList.lastIndex)
                                        }
                                    }
                                    "gpkg" -> {
                                        val geoPackage = GeoPackage(it)
                                        geoPackage.loadAsync()
                                        geoPackage.addDoneLoadingListener {
                                            if (geoPackage.loadStatus == LoadStatus.LOADED) {
                                                val geoTables = geoPackage.geoPackageFeatureTables
                                                geoTables.forEach { table ->
                                                    val featureLayer = FeatureLayer(table)
                                                    StyleFileTool.loadRenderer(featureLayer)
                                                }
                                                try {
                                                    AppInfoManager.appInfo?.layerstyle?.forEach {
                                                        val obj = JSONObject(it)
                                                        if (obj.has("symbol")) {
                                                            geoTables.forEach { table ->
                                                                if (obj.getString("itemName") == table.tableName) {
                                                                    val featureLayer =
                                                                        FeatureLayer(table)
                                                                    featureLayer.renderer =
                                                                        SimpleRenderer.fromJson(
                                                                            it
                                                                        )
                                                                    featureLayer.isVisible =
                                                                        !obj.has("visible") || obj.getString(
                                                                            "visible"
                                                                        ) == "true"
                                                                    featureLayer.loadAsync()
                                                                    MapTool.postLayerChange(
                                                                        ChangeTag,
                                                                        featureLayer,
                                                                        MapTool.ChangeType.BaseAdd
                                                                    )
                                                                    dataList.add(featureLayer)
                                                                    dataAdapter.notifyDataSetChanged()
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        showToast("暂不支持该文件类型")
                                    }
                                }
                            }
                        }
                    }
                })
        } else {
            showToast("未找到离线文件")
        }
    }

    private fun moveToLayer(position: Int) {
        try {
            val extent = dataList[position].fullExtent
            if (extent.xMin == 0.0 || extent.xMax == 0.0 || extent.yMin == 0.0 || extent.yMax == 0.0) {
                MapTool.mapListener?.getMapView()?.setViewpointCenterAsync(extent.center, 100000.0)
            } else {
                MapTool.mapListener?.getMapView()?.setViewpointAsync(Viewpoint(extent), 1f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocalMap(path: String): ArrayList<String> {
        val mapList = MapTool.mapListener?.getMap()?.basemap?.baseLayers
        val nameList = arrayListOf<String>()
        if (!mapList.isNullOrEmpty()) {
            mapList.forEach {
                nameList.add(it.name)
            }
        }
        val localFiles = arrayListOf<String>()
        val file = File(path)
        if (file.exists() && file.isDirectory && file.listFiles().isNotEmpty()) {
            file.listFiles().forEach {
                if (it.isFile && !nameList.contains(
                        it.name.substring(
                            it.name.lastIndexOf("/") + 1,
                            it.name.lastIndexOf(".")
                        )
                    )
                ) {
                    localFiles.add(it.path)
                }
            }
        }
        return localFiles
    }
}
