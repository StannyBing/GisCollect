package com.gt.giscollect.module.survey.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.esri.arcgisruntime.mapping.view.SketchEditor
import com.frame.zxmvp.http.unzip.ZipUtils
import com.gt.base.app.AppInfoManager
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.tool.CopyAssetsToSd
import com.gt.base.tool.MyUtil
import com.gt.camera.module.CameraVedioActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.func.tool.InScrollGridLayoutManager
import com.gt.giscollect.module.collect.ui.FilePreviewActivity
import com.gt.giscollect.module.main.ui.MainActivity
import com.gt.giscollect.module.survey.bean.FileInfoBean
import com.gt.giscollect.module.survey.func.adapter.SurveyFieldEditAdapter
import com.gt.giscollect.module.survey.func.adapter.SurveyFileAdapter
import com.gt.giscollect.module.survey.mvp.contract.SurveyContract
import com.gt.giscollect.module.survey.mvp.modle.SurveyModel
import com.gt.giscollect.module.survey.mvp.presenter.SurveyPresenter
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.tool.GeoPackageTool
import com.gt.module_map.tool.MapTool
import com.zx.zxutils.listener.ZXRecordListener
import com.zx.zxutils.other.ZXInScrollRecylerManager
import com.zx.zxutils.util.*
import kotlinx.android.synthetic.main.fragment_survey.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 踏勘功能
 */
class SurveyFragment : BaseFragment<SurveyPresenter, SurveyModel>(),
    SurveyContract.View {
    private val sketchEditor = SketchEditor()
    private val fieldList = arrayListOf<Pair<Field, Any?>>()
    private val fieldAdapter = SurveyFieldEditAdapter(fieldList)
    private val fileList = arrayListOf<FileInfoBean>()
    private val fileAdapter = SurveyFileAdapter(fileList)
    private var uploadTempFile: File? = null
    private var currentFeature: Feature? = null

    private var recordUtil: ZXRecordUtil? = null
    private var filePath = ""

    private var saveIndex = 0
    private var layerName="踏勘"
    private var templeteId = ""
    private var uuid =""
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SurveyFragment {
            val fragment = SurveyFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordUtil = ZXRecordUtil(requireActivity())
        super.initView(savedInstanceState)
        rv_survey_filed_list.apply {
            layoutManager = ZXInScrollRecylerManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = fieldAdapter
        }

        rv_survey_file_list.apply {
            layoutManager = InScrollGridLayoutManager(mContext, 2)
            adapter = fileAdapter
        }
        downloadModule()
    }

    /**
     * 模板下载
     */
    private fun downloadModule(){
        if (!ZXFileUtil.isFileExists("${ConstStrings.getSurveyTemplatePath()}通用采集模板.gpkg")){
            showLoading("模板下载中...")
            CopyAssetsToSd.copy(mContext,"generalCollect.gpkg", ConstStrings.getSurveyTemplatePath(),"通用采集模板.gpkg")
            dismissLoading()
        }
    }

    override fun onViewListener() {
        //field文件添加事件
        fieldAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.tv_collect_edit_field_link) {
                val url = fieldList[position].second.toString()
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                val contentUri = Uri.parse(url)
                intent.data = contentUri
                startActivity(intent)
            } else {
                when (fieldList[position].first.name) {
                    "camera", "CAMERA" -> {
                        getPermission(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {
                            CameraVedioActivity.startAction(this, false, 1, 0x001, filePath)
                        }
                    }
                    "video", "VIDEO" -> {
                        getPermission(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO
                            )
                        ) {
                            CameraVedioActivity.startAction(this, false, 2, 0x002, filePath)
                        }
                    }
                    "record", "RECORD" -> {
                        getPermission(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {
                            ZXDialogUtil.dismissDialog()
                            val recordView = LayoutInflater.from(mContext)
                                .inflate(R.layout.layout_info_dialog_record, null, false)
                            val touchView =
                                recordView.findViewById<LinearLayout>(R.id.ll_record_touch_start)
                            recordUtil?.bindView(touchView)
                            ZXDialogUtil.showCustomViewDialog(
                                mContext,
                                "录音",
                                recordView
                            ) { dialog: DialogInterface?, which: Int ->

                            }
                        }
                    }
                }
            }
        }
        //文件删除事件
        fileAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.iv_collect_file_delete) {
                if (currentFeature is ArcGISFeature) {
                    showLoading("删除中...")
                    (currentFeature as ArcGISFeature).deleteAttachmentAsync(fileList[position].attachment)
                        .addDoneListener {
                            dismissLoading()
                            loadServiceFeatureFiles()
                            applyFeatureUpdateInfo()
                        }
                } else {
                    fileList.removeAt(position)
                    fileAdapter.notifyItemRemoved(position)
                    fileAdapter.notifyItemRangeChanged(position, 5)
                }
            }
        }
        //文件点击事件
        fileAdapter.setOnItemClickListener { adapter, view, position ->
            if (fileList[position].type == "record" || fileList[position].type == "RECORD") {
                recordUtil?.playMedia(filePath + "/" + fileList[position].path)
            } else {
                FilePreviewActivity.startAction(
                    requireActivity(),
                    false,
                    "文件预览",
                    filePath + "/" + fileList[position].path
                )
            }
        }
        //录音
        recordUtil?.setOnRecordListener(object : ZXRecordListener {
            override fun onSuccess(file: File?) {
                if (file != null) {
                    val fileBean = FileInfoBean(
                        file.name,
                        file.path.substring(file.path.lastIndexOf("/") + 1),
                       "",
                        ZXTimeUtil.getCurrentTime(),
                        "record"
                    )
                    if (currentFeature is ArcGISFeature) {
                        uploadTempFile = File(file.path)
                    } else {
                        fileList.add(fileBean)
                    }
                    fileAdapter.notifyDataSetChanged()
                    handler.postDelayed({
                        ZXDialogUtil.dismissDialog()
                    }, 100)
                }
            }

            override fun onInitPath(): String {
                val path = filePath + "/" + System.currentTimeMillis() + "x.mp3"
                ZXFileUtil.createNewFile(path)
                return path
            }
        })
        //上一步
        tv_survey_feature_undo.setOnClickListener {
            if (sketchEditor.canUndo()) {
                sketchEditor.undo()
            }
        }
        //下一步
        tv_survey_feature_redo.setOnClickListener {
            if (sketchEditor.canRedo()) {
                sketchEditor.redo()
            }
        }
        //清除
        tv_survey_feature_clear.setOnClickListener {
            sketchEditor.clearGeometry()
        }
        //确定
        tv_survey_feature_type.setOnClickListener {
            if (sketchEditor.geometry?.isEmpty == false) {
                if ((sketchEditor.geometry.geometryType == GeometryType.POLYGON && (sketchEditor.geometry as Polygon).parts.partsAsPoints.toList().size < 3) ||
                    (sketchEditor.geometry.geometryType == GeometryType.POLYLINE && (sketchEditor.geometry as Polyline).parts.partsAsPoints.toList().size < 2)||
                    (sketchEditor.geometry.geometryType == GeometryType.POINT && (sketchEditor.geometry as com.esri.arcgisruntime.geometry.Point).isEmpty)
                ) {
                    showToast("当前绘制范围不全，无法保存")
                    return@setOnClickListener
                }
                //TODO:
                copyGpkgFromTemplate(layerName)?.let {
                    it.loadAsync()
                    it.addDoneLoadingListener {
                        if (it.loadStatus == LoadStatus.LOADED) {
                            val featureTables = it.geoPackageFeatureTables
                            if (!featureTables.isNullOrEmpty()){
                                featureTables.forEachIndexed { index, it ->
                                    val featureLayer = FeatureLayer(it)
                                    featureLayer.loadAsync()
                                    featureLayer.addDoneLoadingListener {
                                        currentFeature = featureLayer.featureTable.createFeature()
                                       if (featureLayer.featureTable.geometryType==sketchEditor.geometry.geometryType){
                                           currentFeature?.let {
                                               if (it.attributes?.containsKey("UUID") == true&&it.attributes.get("UUID")==null) {
                                                   uuid =UUID.randomUUID().toString()
                                                   it.attributes?.put("UUID", uuid)
                                               }
                                               it.geometry=sketchEditor.geometry
                                               excuteField(it)
                                           }
                                       }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                showToast("暂未添加要素")
            }
        }
        ivClose.setOnClickListener {
            exit()
            mRxManager.post("close","")
        }
        //提交
        btnSubmit.setOnClickListener {
            saveIndex = 0
            postSave()
            //提交
            ZXDialogUtil.showYesNoDialog(
                mContext,
                "提示",
                "是否上传该条踏勘数据？"
            ) { dialog, which ->
                val files = FileUtils.getFilesByName(
                    ConstStrings.getSurveyTemplatePath() + layerName,
                    "file"
                )
                files.firstOrNull { it.isDirectory }?.apply {
                    val path = ZipUtils.zip(path, false)
                    if (path != null) {
                        mPresenter.uploadSurvey(path,"",templeteId,"",uuid)
                    }
                }
            }
        }
        //文字编辑
        fieldAdapter.addTextChangedCall { position, value ->
            fieldList[position] = fieldList[position].first to value
        }
        btnSearch.setOnClickListener {
            mRxManager.post("surveySearch","")
        }
        //切换模式
        tvChangeMode.setOnClickListener {
           when(tvChangeMode.text){
               "点"-> {
                   setSurveyModule(SketchCreationMode.POLYLINE,null)
                   tvChangeMode.text="线"
               }
               "线"-> {
                   setSurveyModule(SketchCreationMode.POLYGON,null)
                   tvChangeMode.text="面"
               }
               "面"-> {
                   setSurveyModule(SketchCreationMode.POINT,null)
                   tvChangeMode.text="点"
               }
           }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_survey
    }

    fun setSurveyModule(creationMode: SketchCreationMode,point: Point?){
        MapTool.mapListener?.getMapView()?.sketchEditor = sketchEditor
        sketchEditor.clearGeometry()
        sketchEditor.geometry.
        sketchEditor.start(creationMode)
        templeteId = getTemplateId()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x001) {
            if (data != null) {
                val fileBean = FileInfoBean(
                    data.getStringExtra("name"),
                    data.getStringExtra("path").substring(data.getStringExtra("path").lastIndexOf("/") + 1),
                    data.getStringExtra("path").substring(data.getStringExtra("path").lastIndexOf("/") + 1),
                    ZXTimeUtil.getCurrentTime(),
                    "camera"
                )
                if (currentFeature is ArcGISFeature) {
                    uploadTempFile = File(data.getStringExtra("path"))
                } else {
                    fileList.add(fileBean)
                }
                fileAdapter.notifyDataSetChanged()
            }
        } else if (requestCode == 0x002) {
            if (data != null) {
                val fileBean = FileInfoBean(
                    data.getStringExtra("name"),
                    data.getStringExtra("vedioPath").substring(
                        data.getStringExtra("vedioPath").lastIndexOf(
                            "/"
                        ) + 1
                    ),
                    data.getStringExtra("path").substring(data.getStringExtra("path").lastIndexOf("/") + 1),
                    ZXTimeUtil.getCurrentTime(),
                    "video"
                )
                if (currentFeature is ArcGISFeature) {
                    uploadTempFile = File(data.getStringExtra("vedioPath"))
                } else {
                    fileList.add(fileBean)
                }
                fileAdapter.notifyDataSetChanged()
            }
        }
    }

    fun exit() {
        MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
    }

    /**
     * 从模板分钟复制gpkg
     */
    private fun copyGpkgFromTemplate(name: String): GeoPackage? {
        val file = File(ConstStrings.getSurveyTemplatePath()+"/通用采集模板.gpkg")
        if (file.exists() && file.isFile) {
            val destFile =
                File(ConstStrings.getSurveyLayersPath() + name + "/")
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

    fun excuteField(featureLayer:Feature){
        fieldList.clear()
        fileList.clear()
        fieldAdapter.notifyDataSetChanged()
        fileAdapter.notifyDataSetChanged()
        if (featureLayer is ArcGISFeature) {
            showLoading("正在加载在线属性信息")
            featureLayer.loadAsync()
            featureLayer.addDoneLoadingListener {
                loadFeature(featureLayer)
                dismissLoading()
            }
        } else {
            loadFeature(featureLayer)
        }
    }
    private fun loadFeature(featureLayer: Feature) {
        fieldAdapter.readonlyList.clear()
        filePath = ConstStrings.getSurveyTemplatePath() + layerName + "/file"
        fileAdapter.fileParentPath = filePath
        //处理属性信息
        fieldList.clear()
        fieldAdapter.notifyDataSetChanged()
        val filedTemp = arrayListOf<Pair<Field, Any?>>()
        currentFeature?.featureTable?.fields?.forEach {
            if (it.name in arrayOf("camera", "video", "record", "CAMERA", "VIDEO", "RECORD")) {
                filedTemp.add(it to currentFeature!!.attributes[it.name])
            } else {
                if (currentFeature!!.attributes[it.name] == null) {
                    fieldList.add(it to "")
                } else {
                    fieldList.add(it to currentFeature!!.attributes[it.name])
                }
            }
        }

        //获取筛选内容
        try {
            var spinnerMap = hashMapOf<String, List<String>>()
            AppInfoManager.appInfo?.identifystyle?.forEach {
                val obj = JSONObject(it)
                if (obj.getString("itemName") == featureLayer.featureTable.tableName) {
                    if (obj.has("readonly")) {
                        for (i in 0 until obj.getJSONArray("readonly").length()) {
                            fieldAdapter.readonlyList.add(obj.getJSONArray("readonly").getString(i))
                        }
                    }
                    obj.keys().forEach { child ->
                        if (child !in arrayOf("default", "size", "itemName", "readonly", "wkid")) {
                            val keyList = arrayListOf<String>()
                            if (obj.get(child) is JSONArray) {
                                for (i in 0 until obj.getJSONArray(child).length()) {
                                    keyList.add(obj.getJSONArray(child).getString(i))
                                }
                            }
                            spinnerMap[child] = keyList
                        }
                    }
                    fieldAdapter.spinnerMap.clear()
                    fieldAdapter.spinnerMap.putAll(spinnerMap)
                    return@forEach
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fieldList.addAll(filedTemp)//将文件相关的移动到最下方
        fieldAdapter.notifyDataSetChanged()
        //处理文件信息
        fileList.clear()
        if (currentFeature is ArcGISFeature) {
            //添加在线图层文件信息
            loadServiceFeatureFiles()
        } else {
            filedTemp.forEach {
                val fileValues = it.second.toString().split(ConstStrings.File_Split_Char)
                fileValues.forEach { path ->
                    if (path.isNotEmpty() && path != "null" && path.length > 1) {
                        fileList.add(
                            FileInfoBean(
                                "",
                                path = path,
                                pathImage = path,
                                type = it.first.name
                            )
                        )
                    }
                }
            }
            fileAdapter.notifyDataSetChanged()
        }
    }
    private fun loadServiceFeatureFiles() {
        fileList.clear()
        val fetchListenable = (currentFeature as ArcGISFeature).fetchAttachmentsAsync()
        fetchListenable.addDoneListener {
            fetchListenable.get().forEach {
                var file = FileUtils.getFileByName(filePath, it.name)
                if (file == null) {
                    val bytes = it.fetchDataAsync().get().readBytes(20 * 1024)
                    file = FileUtils.bytes2file(bytes, filePath + "/" + it.name)
                }
                fileList.add(
                    FileInfoBean(
                        it.name,
                        file.name,
                        file.name,
                        type = if (it.contentType.contains("image")) {
                            "camera"
                        } else if (it.contentType.contains("audio") || it.contentType.contains("mp3")) {
                            "record"
                        } else if (it.contentType.contains("video")) {
                            "video"
                        } else {
                            "camera"
                        },
                        attachment = it
                    )
                )
            }
            fileAdapter.notifyDataSetChanged()
        }
    }
    private fun postSave() {
        if (saveIndex == fieldList.size + fileList.size) {
            dismissLoading()
            //提交
           // showToast("保存成功")
        } else {
            showLoading("正在保存中...")
            //默认加入一个值
            if (saveIndex < fieldList.size) {
                saveFieldByName(fieldList[saveIndex++].first.name)
            } else {
                saveFieldByName(fileList[saveIndex++ - fieldList.size].type)
            }
        }
    }
    /**
     * 保存字段信息
     */
    private fun saveFieldByName(name: String) {
        when (name) {
            "camera", "video", "record", "CAMERA", "VIDEO", "RECORD" -> {
                var fieldValue = ""
                fileList.forEach {
                    if (it.type.toLowerCase() == name.toLowerCase()) {
                        fieldValue += it.path + ConstStrings.File_Split_Char
                    }
                }
                if (fieldValue.isNotEmpty()) {
                    fieldValue = fieldValue.substring(
                        0,
                        fieldValue.length - ConstStrings.File_Split_Char.length
                    )
                }
                if (currentFeature is ArcGISFeature && uploadTempFile != null) {
                    applyEdit(name, uploadTempFile, Field.Type.TEXT)
                } else {
                    applyEdit(name, if (name=="filled") "true" else fieldValue, Field.Type.TEXT)
                }
            }
            else -> {
                var type: Field.Type? = null
                var fieldValue: Any? = null
                fieldList.firstOrNull { it.first.name == name }?.apply {
                    type = first.fieldType
                    fieldValue = second
                }
                try {
                    if (currentFeature is ArcGISFeature) {
                        (currentFeature as ArcGISFeature).loadAsync()
                        (currentFeature as ArcGISFeature).addDoneLoadingListener {
                            applyEdit(name, if (name=="filled") "true" else fieldValue, type)
                        }
                    } else {
                        applyEdit(name,  if (name=="filled") "true" else fieldValue, type)
                    }
                } catch (e: Exception) {
                    postSave()
                    e.printStackTrace()
                }
            }
        }
    }

    private fun applyEdit(name: String, fieldValue: Any?, type: Field.Type?) {
        if (currentFeature?.attributes?.get(name) == fieldValue) {
            postSave()
            return
        }
        val fields =
            currentFeature?.featureTable?.fields?.filter { it.name.toUpperCase() == name.toUpperCase() && it.isEditable }
        if (fields.isNullOrEmpty()) {
            postSave()
            return
        }
        if (fieldValue is File && currentFeature is ArcGISFeature) {//在线文件
            showLoading("正在在线保存附件中...")
            val fileByte = FileUtils.file2bytes(fieldValue)
            val contentType = when (ZXFileUtil.getFileExtension(fieldValue).toLowerCase()) {
                "png" -> "image/png"
                "jpg", "jepg" -> "image/jpg"
                "3gp" -> "video/3gpp"
                "avi" -> "video/x-msvideo"
                "mp4" -> "video/mp4"
                "mp3" -> "video/mp3"
                "amr" -> "audio/amr"
                else -> "*/*"
            }
            (currentFeature as ArcGISFeature).addAttachmentAsync(
                fileByte,
                contentType,
                fieldValue.name
            ).addDoneListener {
                applyFeatureUpdateInfo()
                loadServiceFeatureFiles()
            }
            return
        } else if (currentFeature?.attributes?.containsKey(name) == true) {
            currentFeature?.attributes?.set(
                name, when (type) {
                    Field.Type.INTEGER -> if (fieldValue == null || fieldValue == "") 0 else fieldValue.toString().toInt()
                    Field.Type.DOUBLE -> if (fieldValue == null || fieldValue == "") 0.0 else fieldValue.toString().toDouble()
                    Field.Type.DATE -> fieldValue
                    else -> fieldValue
                }
            )
            ZXLogUtil.loge("保存：${name}->${fieldValue}")
        } else {
            currentFeature?.attributes?.put(
                name, when (type) {
                    Field.Type.INTEGER -> if (fieldValue == null || fieldValue == "") 0 else fieldValue.toString().toInt()
                    Field.Type.DOUBLE -> if (fieldValue == null || fieldValue == "") 0.0 else fieldValue.toString().toDouble()
                    Field.Type.DATE -> fieldValue
                    else -> fieldValue
                }
            )
        }

        applyFeatureUpdateInfo()
    }

    private fun applyFeatureUpdateInfo() {
        if (currentFeature?.featureTable?.canUpdate(currentFeature) == true) {
            currentFeature?.featureTable?.updateFeatureAsync(currentFeature)?.addDoneListener {
                postSave()
                if (currentFeature?.featureTable is ServiceFeatureTable) {
                    (currentFeature?.featureTable as ServiceFeatureTable).applyEditsAsync()
                }
            }
//            currentFeature?.refresh()
        } else {
            postSave()
        }
    }
    private fun getTemplateId():String{
        var templateId=""
        mSharedPrefUtil.getString("fieldShow")?.let {
            if (it.isNotEmpty()){
                val jsonToLinkedHashMap = MyUtil.jsonToLinkedHashMap(JSONObject(it))
                jsonToLinkedHashMap.entries.forEach {
                    if (it.key=="通用采集"){
                        MyUtil.jsonToLinkedHashMap(JSONObject(it.value)).entries.forEach temp@{
                            when(sketchEditor.sketchCreationMode){
                                SketchCreationMode.POINT->{
                                    if (it.key.toLowerCase()=="pointtemplateid"){
                                        templateId = it.value
                                        return@temp
                                    }
                                }
                                SketchCreationMode.POLYLINE->{
                                    if (it.key.toLowerCase()=="linetemplateid"){
                                        templateId = it.value
                                        return@temp
                                    }
                                }
                                SketchCreationMode.POLYGON->{
                                    if (it.key.toLowerCase()=="polygontemplateid"){
                                        templateId = it.value
                                        return@temp
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return templateId
    }

    override fun onSurveyUpload(name: String) {
        //关闭页面
        showToast("上传成功")
        mRxManager.post("close","")
    }
}