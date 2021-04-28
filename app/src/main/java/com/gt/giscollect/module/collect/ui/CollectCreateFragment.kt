package com.gt.giscollect.module.collect.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.GeoPackage
import com.esri.arcgisruntime.data.ShapefileFeatureTable
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.giscollect.module.collect.bean.CollectBean
import com.gt.giscollect.module.collect.func.adapter.CollectFieldCreateAdapter
import com.gt.giscollect.module.collect.mvp.contract.CollectCreateContract
import com.gt.giscollect.module.collect.mvp.model.CollectCreateModel
import com.gt.giscollect.module.collect.mvp.presenter.CollectCreatePresenter
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.gt.base.app.TempIdsBean
import com.gt.giscollect.tool.SimpleDecoration
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.listener.ZXRecordListener
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.*
import com.zx.zxutils.views.RecylerMenu.ZXRecyclerDeleteHelper
import com.zx.zxutils.views.ZXSpinner
import kotlinx.android.synthetic.main.fragment_collect_create.*
import java.io.File

/**
 * Create By XB
 * 功能：新增采集
 */
class CollectCreateFragment : BaseFragment<CollectCreatePresenter, CollectCreateModel>(),
    CollectCreateContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): CollectCreateFragment {
            val fragment = CollectCreateFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        private const val ChangeTag = "collect_create"
    }

    var fragChangeListener: FragChangeListener? = null

    private val identifyList = arrayListOf<Field>()
    private val identifyAdapter = CollectFieldCreateAdapter(identifyList)

    private lateinit var swipeIdentify: ZXRecyclerDeleteHelper

    private var recordUtil: ZXRecordUtil? = null

    private var createLayer: FeatureLayer? = null

    private var tempalteType: GeometryType? = GeometryType.POLYGON

    private var isEditCollect = true//是否是编辑状态
    private var editSpotIndex = 0//图斑编辑位置

    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_collect_create
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        recordUtil = ZXRecordUtil(requireActivity())

        sp_create_layer_type.showUnderineColor(false)
            .setData(
                arrayListOf(
                    KeyValueEntity("点图层", "1"),
                    KeyValueEntity("线图层", "2"),
                    KeyValueEntity("面图层", "3")
                )
            )
            .setItemHeightDp(40)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .build()

        sp_create_layer_model.showUnderineColor(false)
            .setItemHeightDp(40)
            .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
            .setDefaultItem("请选择采集模板")
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
                        val fileBean = CollectBean.FileInfo(
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
                showToast("请选择采集模板")
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
                    val templateIds =
                        mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
                    val nowTemplateId = templateIds.firstOrNull {
                        it.name.contains(sp_create_layer_model.selectedKey)
                    }?.templateId ?: ConstStrings.mGuideBean.getTemplatesFirst()
                    mPresenter.checkMultiName(
                        hashMapOf(
                            "templateId" to nowTemplateId,
                            "layerName" to et_create_layer_name.text.toString()
                        )
                    )
//                    createLayer()

//                    val shapePath = createShape()
////                    val shapePath = copyShapeFromTempalte(et_create_layer_name.text.toString())
////                    ShapeTool.exuteCopyLayer(shapePath)
//                    if (shapePath.isNotEmpty()) {
//                        showToast("创建成功")
//                        fragChangeListener?.onFragGoto(CollectMainFragment.Collect_List)
//                        val shapefileFeatureTable =
//                            ShapefileFeatureTable(shapePath)
//                        shapefileFeatureTable.loadAsync() //异步方式读取文件
//                        shapefileFeatureTable.addDoneLoadingListener { //数据加载完毕后，添加到地图
//                            val layer = FeatureLayer(shapefileFeatureTable)
//                            layer.name = et_create_layer_name.text.toString()
//                            layer.featureTable.displayName = et_create_layer_name.text.toString()
//                            MapTool.postLayerChange(ChangeTag, layer, MapTool.ChangeType.OperationalAdd)
////                            MapTool.mapListener?.getMap()?.operationalLayers?.add(layer)
//                        }
//                    } else {
//                        showToast("图层创建失败，请检查该图层是否已存在")
//                    }
                }
            }
        }
    }

    private fun createLayer() {
        var myTemplateId = ""
        val templateIds =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)
        templateIds.forEach {
            if (it.name.contains(sp_create_layer_model.selectedKey)) {
                myTemplateId = it.templateId
                it.layerNames.add(et_create_layer_name.text.toString())
                mSharedPrefUtil.putList(
                    ConstStrings.TemplateIdList,
                    templateIds
                )
                return@forEach
            }
        }

        val geoPackage = copyGpkgFromTemplate(et_create_layer_name.text.toString(), myTemplateId)

        geoPackage?.loadAsync()
        geoPackage?.addDoneLoadingListener {
            geoPackage.geoPackageFeatureTables?.forEach {
                val layer = FeatureLayer(it)
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
            }
            fragChangeListener?.onFragGoto(CollectMainFragment.Collect_List)
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
     * 创建shape文件
     */
//    private fun createShape(): String {
//        val shapePath = ShapeTool.createShape(
//            MapTool.mapListener?.getMap()!!,
//            ConstEntryStrings.getOperationalLayersPath(),
//            et_create_layer_name.text.toString(),
////            when (sp_create_layer_type.selectedValue) {
////                "1" -> ShapeTool.ShapeType.Point
////                "2" -> ShapeTool.ShapeType.Polyline
////                "3" -> ShapeTool.ShapeType.Polygon
////                else -> ShapeTool.ShapeType.Point
////            },
//            when (tempalteType) {
//                GeometryType.POINT -> ShapeTool.ShapeType.Point
//                GeometryType.POLYLINE -> ShapeTool.ShapeType.Polyline
//                GeometryType.POLYGON -> ShapeTool.ShapeType.Polygon
//                else -> ShapeTool.ShapeType.Point
//            },
//            arrayListOf<ShapeTool.FieldBean>().apply {
//                identifyList.forEach {
//                    add(
//                        ShapeTool.FieldBean(
//                            it.name, when (it.fieldType) {
//                                Field.Type.TEXT -> ShapeTool.FieldBean.FieldType.String
//                                Field.Type.INTEGER -> ShapeTool.FieldBean.FieldType.Int
//                                Field.Type.FLOAT -> ShapeTool.FieldBean.FieldType.FLOAT
//                                else -> ShapeTool.FieldBean.FieldType.String
//                            }
//                        )
//                    )
//                }
//            })
//        return shapePath
//    }

    /**
     * 从模板中复制shape
     * @param name shape名称
     */
    private fun copyShapeFromTempalte(name: String): String {
        var shpPath = ""
        val file = File(sp_create_layer_model.selectedValue.toString())
        if (file.exists() && file.isDirectory) {
            val destFile = File(ConstStrings.getOperationalLayersPath() + name + "/shape/")
            destFile.mkdirs()
            file.listFiles().forEach {
                val copyFile = ZXFileUtil.copyFile(
                    it.path,
                    destFile.path + "/" + name + it.name.substring(it.name.lastIndexOf("."))
                )
                if (copyFile.exists() && copyFile.name.endsWith(".shp")) {
                    shpPath = copyFile.path
                }
            }
        }
        return shpPath
    }

    /**
     * 从模板分钟复制gpkg
     */
    private fun copyGpkgFromTemplate(name: String, templateId: String = ""): GeoPackage? {
        val file = File(sp_create_layer_model.selectedValue.toString())
        if (file.exists() && file.isFile) {
            val destFile =
                File(ConstStrings.getOperationalLayersPath(templateId = templateId) + name + "/")
            destFile.mkdirs()
            val copyFile = ZXFileUtil.copyFile(
                file.path,
                destFile.path + "/" + name + file.name.substring(file.name.lastIndexOf("."))
            )
            if (copyFile.exists()) {
//                val manager = GeoPackageFactory.getManager(mContext)
//                manager.importGeoPackage(copyFile)
//                val geo = manager.open(manager.databases()[0])
//                val dao = geo.getFeatureDao(geo.featureTables[0])
//                dao.featureDb.table.tableName = name

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
            .setSwipeOptionViews(R.id.tv_edit, R.id.tv_delete)
            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->
                //滑动菜单点击事件
                when (id) {
                    R.id.tv_delete -> {
                        identifyList.removeAt(pos)
                        identifyAdapter.notifyDataSetChanged()
                    }
                }
            }
        //添加字段
        tv_collect_create_field.setOnClickListener {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_info_dialog_edit_identify, null, false)
            val spField = view.findViewById<ZXSpinner>(R.id.sp_create_field_type)
            val etValue = view.findViewById<EditText>(R.id.et_info_value)
//            etValue.keyListener = object : DigitsKeyListener() {
//
//                override fun getInputType(): Int {
//                    return InputType.TYPE_TEXT_VARIATION_PASSWORD
//                }
//
//                override fun getAcceptedChars(): CharArray {
//                    return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
//                }
//                //
//                //            protected val acceptedChars: CharArray?
//                //                protected get() = getStringData(R.string.wordAndNum).toCharArray()
//            }
            spField.showUnderineColor(false)
                .setData(
                    arrayListOf(
                        KeyValueEntity("字符型", "String"),
                        KeyValueEntity("整型", "Integer"),
                        KeyValueEntity("浮点型", "Float")
                    )
                )
                .setItemHeightDp(40)
                .showSelectedTextColor(true, ContextCompat.getColor(mContext, R.color.colorPrimary))
                .build()
            ZXDialogUtil.showCustomViewDialog(mContext, "请输入字段信息", view, { dialog, which ->
                if (etValue.text.toString().isEmpty()) {
                    showToast("请输入字段名")
                } else {
                    identifyList.forEach {
                        if (it.name == etValue.text.toString()) {
                            showToast("该字段已存在，请勿重复添加")
                            return@showCustomViewDialog
                        }
                    }
                    identifyList.add(
                        when (spField.selectedValue.toString()) {
                            "String" -> Field.createString(
                                etValue.text.toString(),
                                etValue.text.toString(),
                                100
                            )
                            "Integer" -> Field.createInteger(
                                etValue.text.toString(),
                                etValue.text.toString()
                            )
                            "Float" -> Field.createFloat(
                                etValue.text.toString(),
                                etValue.text.toString()
                            )
                            else -> Field.createString(
                                etValue.text.toString(),
                                etValue.text.toString(),
                                100
                            )
                        }
                    )
                    identifyAdapter.notifyItemInserted(identifyList.lastIndex)
                    showToast("添加成功")
                    try {
                        ZXSystemUtil.closeKeybord(requireActivity())
                    } catch (e: Exception) {
                    }
                }
            }, { dialog, which ->
            })
        }
    }

    fun reInit() {

        val templateIds =
            mSharedPrefUtil.getList<TempIdsBean>(ConstStrings.TemplateIdList)

        val templateList = arrayListOf<KeyValueEntity>()
        val file = File(ConstStrings.getCollectTemplatePath())
        if (file.exists() && file.isDirectory) {
            val childFiles = file.listFiles()
            childFiles.forEach {
                //                if (it.isDirectory) {
//                    templateList.add(KeyValueEntity(it.name, it.path))
//                } else
                if (it.isFile && it.name.endsWith(".gpkg")) {
                    //只添加当前用户对应的采集模板
                    templateIds?.forEach temp@{ temp ->
                        if (it.path.contains(temp.name) && ConstStrings.mGuideBean.getTemlatesList().contains(
                                temp.templateId
                            )
                        ) {
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
                    }
                }
            }
        }
        sp_create_layer_model.dataList.clear()
        sp_create_layer_model.dataList.addAll(templateList)
        sp_create_layer_model.setDefaultItem("请选择采集模板")
        sp_create_layer_model.notifyDataSetChanged()
        sp_create_layer_model.setSelection(0)
        identifyList.clear()
//        identifyList.add(Field.createString("camera", "default", 300))
//        identifyList.add(Field.createString("video", "default", 300))
//        identifyList.add(Field.createString("record", "default", 300))
        identifyAdapter.notifyDataSetChanged()
        et_create_layer_name.setText("")
        sp_create_layer_type.setSelection(0)
    }

    override fun checkMultiNameResult(isMulti: Boolean) {
        if (isMulti) {
            showToast("该名称已被占用，请更换")
            return
        }
        createLayer()
    }

    /**
     * 举证文件
     */
//    private fun initFile() {
//        swipeFile = ZXRecyclerDeleteHelper(requireActivity(), rv_create_file)
//            .setSwipeOptionViews(R.id.tv_delete)
//            .setSwipeable(R.id.rl_content, R.id.ll_menu) { id, pos ->//滑动菜单点击事件
//                when (id) {
//                    R.id.tv_delete -> {
//                        ZXDialogUtil.showYesNoDialog(mContext, "提示", "是否移除该文件？") { dialog, which ->
//                            fileList.removeAt(pos)
//                            fileAdapter.notifyDataSetChanged()
//                            saveCollect()
//                        }
//                    }
//                }
//            }
//            .setClickable { position ->
//                if (position == fileList.size - 1 && isEditCollect) {//新增文件
//                    getPermission(
//                        arrayOf(
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.RECORD_AUDIO
//                        )
//                    ) {
//                        val view = LayoutInflater.from(mContext).inflate(R.layout.layout_info_dialog_file, null, false)
//                        view.findViewById<LinearLayout>(R.id.ll_collect_file_camera).setOnClickListener {
//                            ZXDialogUtil.dismissDialog()
//                            CameraVedioActivity.startAction(this, false, 1, 0x001)
//                        }
//                        view.findViewById<LinearLayout>(R.id.ll_collect_file_video).setOnClickListener {
//                            ZXDialogUtil.dismissDialog()
//                            CameraVedioActivity.startAction(this, false, 2, 0x002)
//                        }
//                        view.findViewById<LinearLayout>(R.id.ll_collect_file_record).setOnClickListener {
//                            ZXDialogUtil.dismissDialog()
//                            val recordView = LayoutInflater.from(mContext).inflate(R.layout.layout_info_dialog_record, null, false)
//                            val touchView = recordView.findViewById<LinearLayout>(R.id.ll_record_touch_start)
//                            recordUtil?.bindView(touchView)
////                            touchView.setOnClickListener {
////                                recordUtil
////                            }
//                            ZXDialogUtil.showCustomViewDialog(mContext, "录音", recordView) { dialog: DialogInterface?, which: Int -> }
//                        }
//                        ZXDialogUtil.showCustomViewDialog(mContext, "请选择文件类型", view) { dialog, which -> }
//                    }
//                } else {
//                    when (fileList[position].type) {
//                        1 -> {//图片
//                            FilePreviewActivity.startAction(requireActivity(), false, fileList[position].name, fileList[position].path)
//                        }
//                        2 -> {//视频
//                            FilePreviewActivity.startAction(requireActivity(), false, fileList[position].name, fileList[position].path)
//                        }
//                        3 -> {//录音
//                            recordUtil?.playMedia(fileList[position].path)
//                        }
//                    }
//                }
//            }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 0x001) {
//            if (data != null) {
//                val fileBean = CollectBean.FileInfo(
//                    data.getStringExtra("name"),
//                    data.getStringExtra("path"),
//                    data.getStringExtra("path"),
//                    ZXTimeUtil.getCurrentTime(),
//                    1
//                )
//                fileList.add(fileList.size - 1, fileBean)
//                fileAdapter.notifyDataSetChanged()
//                saveCollect()
//            }
//        } else if (requestCode == 0x002) {
//            if (data != null) {
//                val fileBean = CollectBean.FileInfo(
//                    data.getStringExtra("name"),
//                    data.getStringExtra("vedioPath"),
//                    data.getStringExtra("path"),
//                    ZXTimeUtil.getCurrentTime(),
//                    2
//                )
//                fileList.add(fileList.size - 1, fileBean)
//                fileAdapter.notifyDataSetChanged()
//                saveCollect()
//            }
//        }
//    }
}
