package com.stanny.sketchpad.adapter

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.zx.zxutils.other.QuickAdapter.ZXBaseHolder
import com.zx.zxutils.other.QuickAdapter.ZXQuickAdapter
import java.text.DecimalFormat

class SketchPadPropEditAdapter(dataList: List<Float>) :
    ZXQuickAdapter<Float, ZXBaseHolder>(R.layout.item_propedit_layout, dataList) {
    var selectGraphic: SketchPadGraphicBean? = null

    override fun convert(helper: ZXBaseHolder, item: Float) {
        helper.setText(R.id.tv_propedit_num, "边${helper.adapterPosition + 1}:")
        helper.setText(R.id.et_propedit_metre, DecimalFormat("#.00").format(item).toString().run {
            if (endsWith(".0")) {
                replace(".0", "")
            } else if (endsWith(".00")) {
                replace(".00", "")
            } else {
                this
            }
        })

        helper.getView<EditText>(R.id.et_propedit_metre)
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

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    try {
                        data[helper.adapterPosition] =
                            helper.getView<EditText>(R.id.et_propedit_metre).text.toString()
                                .toFloat()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        data[helper.adapterPosition] = 0f
                    }
                }
            })
    }


}