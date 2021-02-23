package com.gt.entrypad.module.project.ui.fragment

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.data.*
import com.gt.base.app.AppInfoManager
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.camera.module.CameraVedioActivity
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.FileInfoBean
import com.gt.entrypad.module.project.func.adapter.SketchFieldEditAdapter
import com.gt.entrypad.module.project.func.adapter.SketchFileAdapter
import com.gt.entrypad.module.project.func.tool.FileUtils
import com.gt.entrypad.module.project.mvp.contract.SketchFiledContract
import com.gt.entrypad.module.project.mvp.model.SketchFiledModel
import com.gt.entrypad.module.project.mvp.presenter.SketchFiledPresenter
import com.gt.entrypad.module.project.ui.activity.FilePreviewActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.zx.zxutils.util.*
import kotlinx.android.synthetic.main.fragment_sketch_filed.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SketchFiledFragment :BaseFragment<SketchFiledPresenter,SketchFiledModel>(),SketchFiledContract.View {
    private val fieldList = arrayListOf<Pair<Field, Any?>>()
    private val fieldAdapter =
        SketchFieldEditAdapter(fieldList)
    private val fileList = arrayListOf<FileInfoBean>()
    private val fileAdapter =
        SketchFileAdapter(fileList)
    private var recordUtil: ZXRecordUtil? = null
    private var filePath = ""
    private var currentFeature: Feature? = null
    private var uploadTempFile: File? = null
    var fragChangeListener: FragChangeListener? = null

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SketchFiledFragment {
            val fragment = SketchFiledFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rv_sketch_filed_list.apply {
            layoutManager = LinearLayoutManager(mContext)
            addItemDecoration(SimpleDecoration(mContext))
            adapter = fieldAdapter
        }
        rv_sketch_file_list.apply {
            layoutManager = GridLayoutManager(mContext, 2)
            adapter = fileAdapter
        }
        recordUtil = ZXRecordUtil(requireActivity())
    }
    override fun onViewListener() {
        fieldAdapter.setOnItemChildClickListener { adapter, view, position ->
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

        //文字编辑
        fieldAdapter.addTextChangedCall { position, value ->
            fieldList[position] = fieldList[position].first to value
            saveField(fieldList[position].first.name)
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
                    val type = fileList[position].type
                    fileList.removeAt(position)
                    fileAdapter.notifyItemRemoved(position)
                    fileAdapter.notifyItemRangeChanged(position, 5)
                    saveField(type)
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
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_filed
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
                saveField("camera")
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
                saveField("video")
            }
        }
    }

    /**
     * 保存文件信息
     */
    private var lastMills = 0L
    private fun saveField(name: String) {
        if (System.currentTimeMillis() - lastMills < 150) {
            //50毫秒内禁止重复编辑
            lastMills = System.currentTimeMillis()
            return
        }
        lastMills = System.currentTimeMillis()
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
                    applyEdit(name, fieldValue, Field.Type.TEXT)
                }
            }
            else -> {
                var type: Field.Type? = null
                var fieldValue: Any? = null
                fieldList.forEach {
                    if (it.first.name == name) {
                        type = it.first.fieldType
                        fieldValue = it.second
                        return@forEach
                    }
                }
                try {
                    if (currentFeature is ArcGISFeature) {
                        (currentFeature as ArcGISFeature).loadAsync()
                        (currentFeature as ArcGISFeature).addDoneLoadingListener {
                            applyEdit(name, fieldValue, type)
                        }
                    } else {
                        applyEdit(name, fieldValue, type)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun applyEdit(name: String, fieldValue: Any?, type: Field.Type?) {
        if (currentFeature?.attributes?.get(name) == fieldValue) {
            return
        }
        val fields =
            currentFeature?.featureTable?.fields?.filter { it.name.toUpperCase() == name.toUpperCase() && it.isEditable }
        if (fields.isNullOrEmpty()) {
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
                dismissLoading()
                applyFeatureUpdateInfo()
                loadServiceFeatureFiles()
            }
        } else if (currentFeature?.attributes?.containsKey(name) == true) {
            currentFeature?.attributes?.set(
                name, when (type) {
                    Field.Type.INTEGER -> fieldValue.toString().toInt()
                    Field.Type.DOUBLE -> fieldValue.toString().toDouble()
                    Field.Type.DATE -> fieldValue
                    else -> fieldValue
                }
            )
        } else {
            currentFeature?.attributes?.put(
                name, when (type) {
                    Field.Type.INTEGER -> fieldValue.toString().toInt()
                    Field.Type.DOUBLE -> fieldValue.toString().toDouble()
                    Field.Type.DATE -> fieldValue
                    else -> fieldValue
                }
            )
        }

        applyFeatureUpdateInfo()
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
    private fun applyFeatureUpdateInfo() {
        if (currentFeature?.featureTable?.canUpdate(currentFeature) == true) {
            currentFeature?.featureTable?.updateFeatureAsync(currentFeature)?.addDoneListener {
                if (currentFeature?.featureTable is ServiceFeatureTable) {
                    (currentFeature?.featureTable as ServiceFeatureTable).applyEditsAsync()
                }
                currentFeature?.refresh()
            }
        }
    }

    fun  excuteField(featureLayer: Feature, editable: Boolean) {
        fieldList.clear()
        fileList.clear()
        fieldAdapter.notifyDataSetChanged()
        fileAdapter.notifyDataSetChanged()
        if (featureLayer is ArcGISFeature) {
            showLoading("正在加载在线属性信息")
            featureLayer.loadAsync()
            featureLayer.addDoneLoadingListener {
                loadFeature(featureLayer, editable)
                dismissLoading()
            }
        } else {
            loadFeature(featureLayer, editable)
        }
    }
    private fun loadFeature(featureLayer: Feature, editable: Boolean) {
        currentFeature = featureLayer
        fieldAdapter.editable = editable
        fileAdapter.editable = editable
        fieldAdapter.readonlyList.clear()
        filePath = ConstStrings.getSketchLayersPath() + featureLayer.featureTable.featureLayer.name + "/file"
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
}