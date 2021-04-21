package com.gt.entrypad.module.project.ui.fragment

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.*
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.stanny.module_rtk.tool.RTKTool
import com.gt.entrypad.R
import com.gt.entrypad.app.ConstString
import com.gt.entrypad.module.project.bean.FloorSiteBean
import com.gt.entrypad.module.project.bean.SiteBean
import com.gt.entrypad.module.project.func.adapter.SketchFeatureAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchFeatureContract
import com.gt.entrypad.module.project.mvp.model.SketchFeatureModel
import com.gt.entrypad.module.project.mvp.presenter.SketchFeaturePresenter
import com.gt.entrypad.module.project.ui.activity.ProjectListActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.*
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.tool.SketchPointTool
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXToastUtil
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_sketch_feature.*
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.cos
import kotlin.math.sin

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
    private var selectSite = arrayListOf<PointF>()
    //参考点集合
    private var sitePointList= arrayListOf<Point>()
    private var floorGraphicList = arrayListOf<FloorSiteBean>()

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

        mSharedPrefUtil.getBool("isEdit",false).apply {
            if (this){
                ConstString.feature?.let {
                    excuteLayer(it,true)
                }
            }
            mSharedPrefUtil.remove("isEdit")
        }
    }

    fun showData(any: Any?){
        any?.let {
            if (it is ArrayList<*>){
                val list = it as ArrayList<SiteBean>
                if (!list.isNullOrEmpty()){
                    for (index in list.indices){
                        selectSite.add(list[index].point)
                        val rtkList = list[index].rtkList
                        if (!rtkList.isNullOrEmpty()){
                            sitePointList.add(rtkList[0].resultSitePoint)
                        }
                    }
                }
            }
        }
        //去除所选界址点
        mSharedPrefUtil.getString("floorList")?.let {
            val floorList = Gson().fromJson<ArrayList<SketchPadFloorBean>>(it, object : TypeToken<ArrayList<SketchPadFloorBean>>() {}.type)
            floorList?.forEach {
                var floorSiteList = arrayListOf<ArrayList<SiteBean>>()
                it.sketchList.forEach {
                    //取出对应图形坐标点集合
                    var siteList = arrayListOf<SiteBean>()
                    if (selectSite.size>=2){
                        //求出对应的角度
                        it.points?.forEach {point->
                           var pointF =  PointF(point.x+it.offsetX,point.y+it.offsetY)
                            siteList.add(SiteBean(point = pointF,angle = RTKTool.excuteDegree(selectSite[0].x.toDouble(),selectSite[0].y.toDouble(),pointF.x.toDouble(),pointF.y.toDouble(),selectSite[1].x.toDouble(),selectSite[1].y.toDouble())))
                        }
                    }
                    floorSiteList.add(siteList)
                }
                floorGraphicList.add(FloorSiteBean(it.id.toString(),floorSiteList))
            }
        }
    }
    /**
     * 绘制草图
     */
    private fun  drawSketch(){
        //推导经纬度
        if (selectSite.size>=2){
            if (sitePointList.isNotEmpty()){
                val length = GeometrySizeTool.getLength(PolylineBuilder(PointCollection(sitePointList)).toGeometry())
                val flatLength = Math.sqrt(Math.pow((selectSite[0].x-selectSite[1].x).toDouble(),2.0)+Math.pow((selectSite[0].y-selectSite[1].y).toDouble(),2.0))
                floorGraphicList.forEach {
                    var floorLatLngs =  arrayListOf<ArrayList<Point>>()
                    //图形
                    it.graphicList?.forEach {
                        var latLngs = arrayListOf<Point>()
                        it.forEach {
                            //对应的角度
                            var flatDistance = Math.sqrt(Math.pow((it.point.x-selectSite[0].x).toDouble(),2.0)+Math.pow((it.point.y-selectSite[0].y).toDouble(),2.0))*(length.toDouble()/flatLength)
                            var pX = 0.0
                            var pY=0.0
                            if (it.angle.isNaN()){
                                pX= sitePointList[0].x
                                pY = sitePointList[0].y
                            }else{
                                val angle = resetDegree(it.angle, sitePointList[1].x, sitePointList[1].y, sitePointList[0].x, sitePointList[0].y)
                                pX= sitePointList[0].x + flatDistance * cos(Math.toRadians(angle))
                                pY = sitePointList[0].y + flatDistance * sin(Math.toRadians(angle))
                            }
                            latLngs.add(Point(pX,pY, SpatialReference.create(3857)))
                        }
                        floorLatLngs.add(latLngs)
                    }
                   if (floorLatLngs.isNotEmpty()) createFeature(floorLatLngs)
                }
            }
       }
    }

    private fun resetDegree(mAngle : Double, point0X : Double, point0Y : Double, point1X : Double, point1Y : Double) : Double{
        return Math.toDegrees(Math.atan((point1Y - point0Y) / (point1X - point0X))).let {
            if (point1Y >= point0Y && point1X >= point0X) {
                180 + it
            } else if (point1Y >= point0Y && point1X < point0X) {
                360 + it
            } else if (point1Y < point0Y && point1X >= point0X) {
                180 + it
            } else{
                it
            }
        } - mAngle
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
    }

    private fun createFeature(latLngs:ArrayList<ArrayList<Point>>) {
        var feature: Feature? = null
        currentLayer?.featureTable?.loadAsync()
        currentLayer?.featureTable?.addDoneLoadingListener {

        }
        feature = currentLayer?.featureTable?.createFeature()
       val partCollection = PartCollection(SpatialReference.create(3857))
        latLngs.forEach {
            partCollection.add(PointCollection(it))
        }
        feature?.geometry = Polygon(partCollection)
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
       feature?.let {
           featureList.add(it)
       }
        featureAdapter.loadMoreEnd()
        featureAdapter.notifyDataSetChanged()
    }

    /**
     * 处理图层
     */
    fun excuteLayer(featureLayer: FeatureLayer,isEdit:Boolean=false) {
        currentLayer = featureLayer
        currentLayer?.clearSelection()

        try {
            var extent = featureLayer.fullExtent
            if (extent.xMin == 0.0 || extent.xMax == 0.0 || extent.yMin == 0.0 || extent.yMax == 0.0) {
                MapTool.mapListener?.getMapView()?.setViewpointCenterAsync(extent.center, 100000.0)
            } else {
                MapTool.mapListener?.getMapView()?.setViewpointAsync(Viewpoint(extent), 1f)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }

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
        et_collect_rename.apply {
            visibility=View.VISIBLE
            isEnabled=false
            setText(currentLayer?.name?:"")
        }
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
        }else{
            mSharedPrefUtil.getString("siteList")?.let {
                val points = Gson().fromJson<ArrayList<SiteBean>>(it, object : TypeToken<ArrayList<SiteBean>>() {}.type)
                showData(points)
                drawSketch()
            }
        }
    }


    fun reInit() {
        currentLayer?.clearSelection()
        HighLightLayerTool.clearHighLight()
        featureAdapter.notifyDataSetChanged()
    }

    private fun applyLayerUpdateInfo() {
        if (currentLayer?.featureTable is ServiceFeatureTable) {
            (currentLayer?.featureTable as ServiceFeatureTable).applyEditsAsync()
        }
    }

    override fun onDestroy() {
        reInit()
        super.onDestroy()
    }
}
