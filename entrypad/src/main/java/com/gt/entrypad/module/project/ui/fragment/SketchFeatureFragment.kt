package com.gt.entrypad.module.project.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.internal.jni.it
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.esri.arcgisruntime.mapping.view.SketchStyle
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.gt.base.app.AppInfoManager
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.manager.UserManager
import com.gt.base.app.TempIdsBean
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.ProjectListBean
import com.gt.entrypad.module.project.func.adapter.SketchFeatureAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchFeatureContract
import com.gt.entrypad.module.project.mvp.model.SketchFeatureModel
import com.gt.entrypad.module.project.mvp.presenter.SketchFeaturePresenter
import com.gt.entrypad.module.project.ui.activity.ProjectListActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.*
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_sketch_create.*
import kotlinx.android.synthetic.main.fragment_sketch_feature.*
import kotlinx.android.synthetic.main.fragment_sketch_feature.sp_create_layer_model
import kotlinx.android.synthetic.main.layout_tool_bar.*
import org.json.JSONObject
import rx.functions.Action1
import java.io.File
import java.text.DecimalFormat
import java.util.*

/**
 * Create By XB
 * 功能：
 */
class SketchFeatureFragment : BaseFragment<SketchFeaturePresenter, SketchFeatureModel>(),
    SketchFeatureContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SketchFeatureFragment {
            val fragment = SketchFeatureFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    var fragChangeListener: FragChangeListener? = null
    private var currentLayer: FeatureLayer? = null
    private val featureList = arrayListOf<Feature>()
    private val featureAdapter = SketchFeatureAdapter(featureList)
    private var editPosition = -1
    private var keyList = arrayListOf<String>()
    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_feature
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rv_sketch_filed_features.apply {
            layoutManager = ZXInScrollRecylerManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = featureAdapter
        }

        sp_collect_feature_showfield.showUnderineColor(false)
            .showSelectedTextColor(true)
            .setItemHeightDp(30)
            .setItemTextSizeSp(12)
            .build()

        sp_create_layer_model.showUnderineColor(false)
            .setItemHeightDp(40)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setDefaultItem("请选择模板")
            .build()
        getGpkgList()
    }

    /**
     * 获取模板列表
     */
    private fun getGpkgList() {
        val templateList = arrayListOf<KeyValueEntity>()
        val file = File(ConstStrings.getSketchTemplatePath())
        if (file.exists() && file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isFile && it.name.endsWith(".gpkg")) {
                    templateList.add(
                        KeyValueEntity(
                            it.name.substring(0, it.name.lastIndexOf(".")),
                            it.path
                        )
                    )
                }
            }
        }
        sp_create_layer_model.apply {
            dataList.clear()
            dataList.addAll(templateList)
            setDefaultItem("请选择模板")
            setSelection(0)
            notifyDataSetChanged()
        }
    }
    /**
     * 从模板分钟复制gpkg
     */
    private fun copyGpkgFromTemplate(name: String): GeoPackage? {
        val file = File(sp_create_layer_model.selectedValue.toString())
        if (file.exists() && file.isFile) {
            val destFile =
                File(ConstStrings.getSketchLayersPath() + "/")
            destFile.mkdirs()
            val copyFile = ZXFileUtil.copyFile(
                file.path,
                destFile.path + "/" + name + file.name.substring(file.name.lastIndexOf("."))
            )
            if (copyFile.exists()) {
                val geoPackage = GeoPackage(copyFile.path)
                return geoPackage
            }
        }

        return null
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        featureAdapter.setEnableLoadMore(true)
        ZXRecyclerDeleteHelper(activity, rv_sketch_filed_features)
            .setSwipeOptionViews(R.id.tv_edit, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->

            }.setClickable {
                editPosition = it
                currentLayer?.clearSelection()
                HighLightLayerTool.showHighLight(featureList[it])
                currentLayer?.selectFeature(featureList[it])
                fragChangeListener?.onFragGoto(SketchMainFragment.Sketch_Field, featureList[it])
            }

        //要素显示名
        sp_collect_feature_showfield.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    featureAdapter.showName = sp_collect_feature_showfield.selectedKey
                    featureAdapter.notifyDataSetChanged()
                }
            }
        //模板选择监听
        sp_create_layer_model.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (sp_create_layer_model.selectedValue.toString().isNotEmpty()) {
                    ConstStrings.sktchId = UUID.randomUUID().toString()
                    if (!keyList.contains(ConstStrings.sktchId)){
                        keyList.add(ConstStrings.sktchId)
                        mSharedPrefUtil.putList("sketchId",keyList)
                    }
                    val geoPackage = copyGpkgFromTemplate("竣工验收")
                    geoPackage?.loadAsync()
                    geoPackage?.addDoneLoadingListener {
                        if (geoPackage.loadStatus == LoadStatus.LOADED) {
                            geoPackage.geoPackageFeatureTables?.let {
                                if (it.isNotEmpty()) {
                                    val table = it.first()
                                    var fieldList = arrayListOf<KeyValueEntity>()
                                    table.loadAsync()
                                    table.addDoneLoadingListener {
                                        table.fields.forEach {
                                            if (it.isEditable) {//不能加载FID字段
                                                fieldList.add(KeyValueEntity(
                                                   it.name,
                                                   it
                                                ))
                                            }
                                        }
                                        table.cancelLoad()
                                    }
                                    sp_collect_feature_showfield.apply {
                                        dataList.addAll(fieldList)
                                        notifyDataSetChanged()
                                    }
                                   it.forEach {
                                       // excuteLayer(FeatureLayer(it))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
     //完成
        btnSketchFinish.setOnClickListener {
            if (sp_create_layer_model.selectedKey.toString()!="请选择模板"){
                 ProjectListActivity.startAction(mActivity,true)
            }else{
                showToast("请选择模板")
            }
        }
    }

    private fun createFeature() {
        var feature: Feature? = null
        currentLayer?.featureTable?.loadAsync()
        currentLayer?.featureTable?.addDoneLoadingListener {

        }
        feature = currentLayer?.featureTable?.createFeature()
        val pointCollection = PointCollection(arrayListOf<Point>().apply {
            add(
                GeometryEngine.project(
                    Point(106.5635978468597, 29.575631491622985),
                    SpatialReferences.getWgs84()
                ) as Point
            )
            add(
                GeometryEngine.project(
                    Point(106.58894815535211, 29.57216102606911),
                    SpatialReferences.getWgs84()
                ) as Point
            )
            add(
                GeometryEngine.project(
                    Point(106.56553432975977, 29.554704814709538),
                    SpatialReferences.getWgs84()
                ) as Point
            )
            add(
                GeometryEngine.project(
                    Point(106.5635978468597, 29.575631491622985),
                    SpatialReferences.getWgs84()
                ) as Point
            )
        })
        feature?.geometry = Polygon(pointCollection)
        //获取筛选内容
        try {
            feature?.featureTable?.fields?.forEach OUT@{ field ->
                if (field.fieldType == Field.Type.DATE) {
                    feature.attributes?.keys?.forEach IN@{ name ->
                        if (name == field.name) {
                            val calendar = Calendar.getInstance()
                            feature.attributes?.set(
                                name,
                                GregorianCalendar(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                            )
                            feature.attributes?.put("uuid", UUID.randomUUID().toString())
                            return@IN
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentLayer?.featureTable?.addFeatureAsync(feature)?.addDoneListener {
            applyLayerUpdateInfo()
            feature?.refresh()
            tv_collect_feature_title.text =
                "要素列表(${currentLayer?.featureTable?.totalFeatureCount})"
        }
        featureList.add(feature!!)
        featureAdapter.loadMoreEnd()
        featureAdapter.notifyDataSetChanged()
    }

    /**
     * 处理图层
     */
    fun excuteLayer(featureLayer: FeatureLayer,isEdit:Boolean=false) {
        currentLayer = featureLayer
        currentLayer?.clearSelection()

        //设置字段显示筛选项
        sp_collect_feature_showfield.dataList.clear()
        var showIndex = 0
        featureLayer.featureTable.fields.forEachIndexed { index, it ->
            if (showIndex == 0 && it.name == featureAdapter.showName) {
                showIndex = index
            }
            sp_collect_feature_showfield.dataList.add(KeyValueEntity(it.name, index))
        }

        sp_collect_feature_showfield.notifyDataSetChanged()
        sp_collect_feature_showfield.setSelection(showIndex)
        featureList.clear()
        if (isEdit){
            currentLayer?.featureTable?.loadAsync()
            currentLayer?.featureTable?.addDoneLoadingListener {
                val queryGet = currentLayer?.featureTable?.queryFeaturesAsync(QueryParameters())
                queryGet?.addDoneListener {

                    val list = queryGet.get()
                    featureAdapter.loadMoreEnd()
                    featureList.addAll(list)
                    featureAdapter.notifyDataSetChanged()
                }
            }
            et_collect_rename.apply {
                visibility=View.VISIBLE
                isEnabled=false
                setText(currentLayer?.name?:"")
            }
            sp_create_layer_model.visibility=View.GONE
            btnSketchFinish.visibility=View.GONE
        }else{
            et_collect_rename.visibility=View.GONE
            sp_create_layer_model.visibility=View.VISIBLE
            btnSketchFinish.visibility=View.VISIBLE
            createFeature()
        }
    }


    fun reInit() {
        HighLightLayerTool.clearHighLight()
        currentLayer?.clearSelection()
        featureAdapter.notifyDataSetChanged()
    }

    private fun applyLayerUpdateInfo() {
        if (currentLayer?.featureTable is ServiceFeatureTable) {
            (currentLayer?.featureTable as ServiceFeatureTable).applyEditsAsync()
        }
    }
}
