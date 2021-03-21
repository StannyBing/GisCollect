package com.gt.giscollect.module.collect.func.adapter

import android.app.DatePickerDialog
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Field
import com.gt.giscollect.R
import com.gt.giscollect.module.collect.bean.CollectBean
import com.zx.zxutils.entity.KeyValueEntity
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXLogUtil
import com.zx.zxutils.util.ZXTimeUtil
import com.zx.zxutils.views.ZXSpinner
import java.text.SimpleDateFormat
import java.util.*

class CollectFieldEditAdapter(dataList: List<Pair<Field, Any?>>) :
    ZXQuickAdapter<Pair<Field, Any?>, ZXBaseHolder>(R.layout.item_collect_edit_field, dataList) {

    var editable: Boolean = true

    private var call: (Int, Any) -> Unit = { _, _ -> }

    var spinnerMap = hashMapOf<String, List<String>>()

    var readonlyList = arrayListOf<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun convert(helper: ZXBaseHolder, item: Pair<Field, Any?>) {
        helper.setText(R.id.tv_collect_edit_field_name, item.first.name)
        when (item.first.fieldType) {
            Field.Type.TEXT -> "字符型"
            Field.Type.INTEGER -> "整型"
            Field.Type.FLOAT -> "浮点型"
            else -> "字符型"
        }
        helper.getView<EditText>(R.id.et_collect_edit_field_value).apply {
            visibility = View.GONE
            hint="请输入${item.first.name}"
        }
        helper.getView<TextView>(R.id.tv_collect_edit_field_date).visibility = View.GONE
        helper.getView<Button>(R.id.btn_collect_edit_filed_file).visibility = View.GONE
        helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value).visibility = View.GONE
        helper.getView<TextView>(R.id.tv_collect_edit_field_link).visibility = View.GONE

        if (item.first.name in arrayOf(
                "camera",
                "video",
                "record",
                "CAMERA",
                "VIDEO",
                "RECORD"
            )
        ) {//文件选择
            helper.getView<Button>(R.id.btn_collect_edit_filed_file).visibility =
                if (editable) View.VISIBLE else View.GONE
            helper.setText(
                R.id.btn_collect_edit_filed_file, when (item.first.name) {
                    "camera", "CAMERA" -> "添加照片"
                    "video", "VIDEO" -> "添加视频"
                    "record", "RECORD" -> "添加录音"
                    else -> ""
                }
            )
            helper.addOnClickListener(R.id.btn_collect_edit_filed_file)
        } else if (spinnerMap.containsKey(item.first.name)) {//下拉列表
            helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value).visibility = View.VISIBLE
            helper.setEnabled(
                R.id.sp_collect_edit_field_value,
                editable && !readonlyList.contains(item.first.name)
            )
            helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value)
                .setData(arrayListOf<KeyValueEntity>().apply {
                    spinnerMap[item.first.name]?.forEach {
                        add(KeyValueEntity(it, it))
                    }
                })
                .setDefaultItem(if (editable) "请选择..." else "不可选")
                .showUnderineColor(false)
                .setItemHeightDp(40)
                .setItemTextSizeSp(13)
                .showSelectedTextColor(true)
                .build()
            helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value)
                .dataList.forEachIndexed { index, it ->
                if (it.value.toString() == item.second.toString()) {
                    helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value).setSelection(index)
                    return@forEachIndexed
                }
            }
            helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value).onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        call(
                            helper.adapterPosition,
                            helper.getView<ZXSpinner>(R.id.sp_collect_edit_field_value).selectedValue.toString()
                        )
                    }
                }
        } else if (item.first.fieldType == Field.Type.DATE) {//日期选择
            helper.setEnabled(
                R.id.tv_collect_edit_field_date,
                editable && !readonlyList.contains(item.first.name)
            )
            helper.setText(
                R.id.tv_collect_edit_field_date,
                if (!editable) "不可选" else if (item.second == null || item.second !is GregorianCalendar) "点击选择时间" else {
                    ZXTimeUtil.getTime(
                        (item.second as GregorianCalendar).timeInMillis,
                        SimpleDateFormat("yyyy/MM/dd")
                    )
                }
            )
            helper.getView<TextView>(R.id.tv_collect_edit_field_date).visibility = View.VISIBLE
            helper.getView<TextView>(R.id.tv_collect_edit_field_date).setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    mContext,
                    DatePickerDialog.THEME_HOLO_LIGHT,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        call(helper.adapterPosition, GregorianCalendar(year, month, dayOfMonth))
                        helper.setText(
                            R.id.tv_collect_edit_field_date,
                            "$year/${month + 1}/$dayOfMonth"
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        } else {//默认输入
            helper.getView<EditText>(R.id.et_collect_edit_field_value).visibility = View.VISIBLE
            helper.getView<EditText>(R.id.et_collect_edit_field_value).isEnabled =
                item.first.isEditable && editable && !readonlyList.contains(item.first.name) && item.first.name != "uuid" && item.first.name != "UUID"
            helper.setText(
                R.id.et_collect_edit_field_value, if (item.second == null) "" else {
                    if (item.first.fieldType == Field.Type.DOUBLE && (item.second == "" || item.second == null)) {
                        "0.0"
                    } else if (item.first.fieldType == Field.Type.INTEGER && (item.second == "" || item.second == null)) {
                        "0"
                    } else {
                        item.second.toString()
                    }
                }
            )
            if (if (item.second == null) "" else {
                    item.second.toString()
                }.startsWith("http")
            ) {
                helper.getView<TextView>(R.id.tv_collect_edit_field_link).visibility = View.VISIBLE
                helper.addOnClickListener(R.id.tv_collect_edit_field_link)
            }

            helper.getView<EditText>(R.id.et_collect_edit_field_value)
                .addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        call(
                            helper.adapterPosition,
                            helper.getView<EditText>(R.id.et_collect_edit_field_value).text.toString()
                        )
                    }
                })

            helper.getView<EditText>(R.id.et_collect_edit_field_value).inputType =
                when (item.first.fieldType) {
                    Field.Type.INTEGER -> InputType.TYPE_CLASS_NUMBER
                    Field.Type.DOUBLE -> InputType.TYPE_NUMBER_FLAG_DECIMAL
                    else -> InputType.TYPE_CLASS_TEXT
                }
        }
    }

    fun addTextChangedCall(call: (Int, Any) -> Unit) {
        this.call = call
    }
}