package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.base.app.ConstStrings
import com.gt.base.app.TempIdsBean
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.SketchBean
import com.gt.entrypad.module.project.func.adapter.SketchFieldCreateAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchCreateContract
import com.gt.entrypad.module.project.mvp.model.SketchCreateModel
import com.gt.entrypad.module.project.mvp.presenter.SketchCreatePresenter
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.listener.ZXRecordListener
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.*
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import kotlinx.android.synthetic.main.fragment_sketch_create.*
import java.io.File

/**
 * create by 96212 on 2021/2/22.
 * Email 962123525@qq.com
 * desc
 */
class SketchCreateFragment :BaseFragment<SketchCreatePresenter,SketchCreateModel>(),SketchCreateContract.View{

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SketchCreateFragment {
            val fragment = SketchCreateFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "sketch_create"
    }

    var fragChangeListener: FragChangeListener? = null

    private val identifyList = arrayListOf<Field>()
    private val identifyAdapter =
        SketchFieldCreateAdapter(identifyList)

    private lateinit var swipeIdentify: ZXRecyclerDeleteHelper

    private var recordUtil: ZXRecordUtil? = null


    private var tempalteType: GeometryType? = GeometryType.POLYGON

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_create
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        recordUtil = ZXRecordUtil(requireActivity())
        sp_create_layer_model.showUnderineColor(false)
            .setItemHeightDp(40)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setDefaultItem("请选择模板")
            .build()

        rv_create_layer_field.apply {
            layoutManager = ZXInScrollRecylerManager(requireActivity())
            adapter = identifyAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }

        reInit()
        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        //属性列表
        initIdentify()
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
                identifyList.clear()
                if (sp_create_layer_model.selectedValue.toString().isNotEmpty()) {
                    val file = File(sp_create_layer_model.selectedValue.toString())
                    if (file.exists() && file.isDirectory) {
                        file.listFiles().forEach {
                            if (it.name.endsWith(".shp")) {
                                val table = ShapefileFeatureTable(it.path)
                                table.loadAsync() //异步方式读取文件
                                table.addDoneLoadingListener {
                                    tempalteType = table.geometryType
                                    table.fields.forEach {
                                        if (it.isEditable) {//不能加载FID字段
                                            identifyList.add(it)
                                        }
                                    }
                                    identifyAdapter.notifyDataSetChanged()
                                    table.close()
                                }
                                return@forEach
                            }
                        }
                    } else if (file.exists() && file.isFile && file.name.endsWith(".gpkg")) {
                        GeoPackageTool.getTablesFromGpkg(file.path) {
                            if (it.isNotEmpty()) {
                                val table = it.first()
                                table.loadAsync()
                                table.addDoneLoadingListener {
                                    tempalteType = table.geometryType
                                    table.fields.forEach {
                                        if (it.isEditable) {//不能加载FID字段
                                            identifyList.add(it)
                                        }
                                    }
                                    identifyAdapter.notifyDataSetChanged()
                                    table.cancelLoad()
                                }
                            }
                        }
                    }
                }
                identifyAdapter.notifyDataSetChanged()
            }
        }
        //录音
        recordUtil?.setOnRecordListener(
            object : ZXRecordListener {
                override fun onSuccess(file: File?) {
                    if (file != null) {
                        val fileBean = SketchBean.FileInfo(
                            file.name,
                            file.path,
                            "",
                            ZXTimeUtil.getCurrentTime(),
                            3
                        )
                    }
                }

                override fun onInitPath(): String {
                    return ConstStrings.getLocalPath() + "ReportFile/" + System.currentTimeMillis() + "x.amr"
                }
            })
        //保存
        btn_collect_create_save.setOnClickListener {
            if (et_create_layer_name.text.toString().isEmpty()) {
                showToast("请输入图层名")
            } else if (sp_create_layer_model.selectedValue.toString().isEmpty()) {
                showToast("请选择模板")
            } else {
                ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否新增图层？") { _, _ ->
                    if (et_create_layer_name.text.toString().contains("/")
                        || et_create_layer_name.text.toString().contains("\\")
                    ) {
                        showToast("请勿使用‘/’,‘\\’等关键字作为图层名称")
                        return@showYesNoDialog
                    }
                    if (checkLayerExist(et_create_layer_name.text.toString())) {
                        showToast("该名称已被占用，请更换")
                        return@showYesNoDialog
                    }
                    createLayer()
                }
            }
        }
    }

    private fun createLayer() {
        val geoPackage = copyGpkgFromTemplate(et_create_layer_name.text.toString())

        geoPackage?.loadAsync()
        geoPackage?.addDoneLoadingListener {
            geoPackage.geoPackageFeatureTables?.forEach {
                val layer = FeatureLayer(it)
                var tempSketchList = arrayListOf<String>()
                val sketchIdList = mSharedPrefUtil.getList<String>(ConstStrings.SketchIdList)
                sketchIdList?.let {
                    tempSketchList.addAll(it)
                }
                val layerName = et_create_layer_name.text.toString()
                if (!tempSketchList.contains(layerName)){
                      tempSketchList.add(layerName)
                  }
                mSharedPrefUtil.putList(ConstStrings.SketchIdList,tempSketchList)
                if (geoPackage.geoPackageFeatureTables.size == 1) {
                    layer.loadAsync()
                    layer.name = et_create_layer_name.text.toString()
                    layer.featureTable.displayName =
                        et_create_layer_name.text.toString()
                    it.displayName = et_create_layer_name.text.toString()
                    layer.cancelLoad()

                }
                MapTool.postLayerChange(
                    ChangeTag,
                    layer,
                    MapTool.ChangeType.OperationalAdd
                )
                fragChangeListener?.onFragGoto(SketchMainFragment.Sketch_Feature,layer)
            }
            showToast("创建成功")
        }
    }

    /**
     * 检测图层重复
     */
    private fun checkLayerExist(name: String): Boolean {
        MapTool.mapListener?.getMap()?.operationalLayers?.forEach {
            if (it.name == name) {
                return true
            }
        }
        ConstStrings.checkList.forEach {
            if (it.getFileName().replace(".gpkg", "") == name) {
                return true
            }
        }
        return false
    }

    /**
     * 从模板分钟复制gpkg
     */
    private fun copyGpkgFromTemplate(name: String): GeoPackage? {
        ConstStrings.clear()
        val file = File(sp_create_layer_model.selectedValue.toString())
        if (file.exists() && file.isFile) {
            val destFile =
                File(ConstStrings.getSketchLayersPath() + name + "/")
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
     * 属性列表
     */
    private fun initIdentify() {
        swipeIdentify = ZXRecyclerDeleteHelper(requireActivity(), rv_create_layer_field)
            .setSwipeOptionViews(R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_delete -> {
                        identifyList.removeAt(pos)
                        identifyAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    fun reInit() {

        val templateList = arrayListOf<KeyValueEntity>()
        /*val templateIds =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.DrawTemplateIdList)

        val file = File(ConstStrings.getSketchTemplatePath())
        if (file.exists() && file.isDirectory) {
            val childFiles = file.listFiles()
            childFiles.forEach {
                //                if (it.isDirectory) {
//                    templateList.add(KeyValueEntity(it.name, it.path))
//                } else
                if (it.isFile && it.name.endsWith(".gpkg")) {
                    //只添加当前用户对应的采集模板
               *//*     templateIds?.forEach temp@{ temp ->
                        if (it.path.contains(temp.name) && temp.templateId == ConstStrings.bussinessId) {
                            templateList.add(
                                KeyValueEntity(
                                    it.name.substring(
                                        0,
                                        it.name.lastIndexOf(".")
                                    ), it.path
                                )
                            )
                            return@temp
                        }
                    }*//*
                    templateList.add(
                        KeyValueEntity(
                            it.name.substring(
                                0,
                                it.name.lastIndexOf(".")
                            ), it.path
                        )
                    )
                }
            }
        }*/
        templateList.add( KeyValueEntity(
            ConstStrings.drawTempleteName.substring(
                0,
               ConstStrings.drawTempleteName.lastIndexOf(".")
            ), ConstStrings.getDrawTemplatePath()+ConstStrings.drawTempleteName
        ))
        sp_create_layer_model.dataList.clear()
        sp_create_layer_model.dataList.addAll(templateList)
        sp_create_layer_model.setDefaultItem("请选择模板")
        sp_create_layer_model.notifyDataSetChanged()
        sp_create_layer_model.setSelection(0)
        identifyList.clear()
        identifyAdapter.notifyDataSetChanged()
        et_create_layer_name.setText("")
    }

    override fun onDestroy() {
        ConstStrings.clear()
        super.onDestroy()
    }
}