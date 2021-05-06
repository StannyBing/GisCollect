package com.stanny.sketchpad.tool

import android.content.Context
import android.graphics.PointF
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadLabelAdapter
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.bean.SketchPadLabelBean
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXScreenUtil

/**
 * 标注类
 */
class SketchLabelTool(var context: Context, var listener: LabelListener) {

    var labelList = arrayListOf<SketchPadLabelBean>()

    var drawLabel = false//开启标注绘制

    var selectLabel: SketchPadLabelBean? = null//选中标注

    interface LabelListener {
        fun getContentTransX(): Float
        fun getContentTransY(): Float
        fun refreshGraphic()
        fun getGraphicList(): ArrayList<SketchPadGraphicBean>
    }

    private fun showInDialog(labelPoint: PointF, data: ArrayList<SketchLabelBean>) {
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SketchPadLabelAdapter(data).apply {
            addCheckedChangeListener {
                val list = data as ArrayList<SketchLabelBean>
                list.forEachIndexed { index, sketchLabelBean ->
                    if (sketchLabelBean == list[it]) {
                        content = sketchLabelBean.value
                        sketchLabelBean.isChecked = !sketchLabelBean.isChecked
                    } else {
                        sketchLabelBean.isChecked = false
                    }
                }
                notifyDataSetChanged()
            }
        }
        ZXDialogUtil.showCustomViewDialog(context, "", view, { dialog, which ->
            labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                offsetX = -listener.getContentTransX()
                offsetY = -listener.getContentTransY()
            })
            listener.refreshGraphic()
            drawLabel = false
        }, { dialog, which -> }).apply {
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth() / 3
            layoutParams?.gravity = Gravity.RIGHT
            window?.attributes = layoutParams
        }
    }

    private fun showInData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "阳台"))
            add(SketchLabelBean("2", "内阳台"))
            add(SketchLabelBean("3", "砖湿"))
            add(SketchLabelBean("4", "砖瓦"))
            add(SketchLabelBean("5", "滴水"))
            add(SketchLabelBean("6", "猪圈"))
        }
    }

    private fun showOutData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "坝"))
            add(SketchLabelBean("2", "人行道"))
            add(SketchLabelBean("3", "水沟"))
            add(SketchLabelBean("4", "巷道"))
            add(SketchLabelBean("5", "林地"))
            add(SketchLabelBean("6", "耕地"))
        }
    }

    private fun showBoundaryData(): ArrayList<SketchLabelBean> {
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1", "自墙"))
            add(SketchLabelBean("2", "共墙"))
            add(SketchLabelBean("3", "借墙"))
        }
    }

    private fun showOutDialog(labelPoint: PointF) {
        var content = ""
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog, null)
        view.findViewById<EditText>(R.id.otherEt).apply {
            visibility = View.VISIBLE
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    content = s?.toString()?.trim() ?: ""
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SketchPadLabelAdapter(showOutData()).apply {
            addCheckedChangeListener {
                val list = data as ArrayList<SketchLabelBean>
                list.forEachIndexed { index, sketchLabelBean ->
                    if (sketchLabelBean == list[it]) {
                        content = sketchLabelBean.value
                        sketchLabelBean.isChecked = !sketchLabelBean.isChecked
                    } else {
                        sketchLabelBean.isChecked = false
                    }
                }
                notifyDataSetChanged()
            }
        }
        ZXDialogUtil.showCustomViewDialog(context, "", view, { dialog, which ->
            labelList.add(SketchPadLabelBean(content, labelPoint).apply {
                offsetX = -listener.getContentTransX()
                offsetY = -listener.getContentTransY()
            })
            listener.refreshGraphic()
            drawLabel = false
        }, { dialog, which -> }).apply {
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth() / 3
            layoutParams?.gravity = Gravity.RIGHT
            window?.attributes = layoutParams
        }
    }

    /**
     * 处理label的绘制
     */
    fun excuteLabelDraw(event: MotionEvent): Boolean {
        val labelPoint = PointF(event.x, event.y)
        listener.getGraphicList().forEach {
            if (it.isGraphicContainsPoint(
                    event.x - listener.getContentTransX(),
                    event.y - listener.getContentTransY()
                )
            ) {
                showInDialog(labelPoint, showInData())
                return true
            } else if (it.isGraphicContainsPoint(
                    event.x - 40,
                    event.y - 40
                ) || it.isGraphicContainsPoint(event.x + 40, event.y + 40)
            ) {
                showInDialog(labelPoint, showBoundaryData())
                return true
            }
        }
        showOutDialog(labelPoint)
        return true
    }

    /**
     * 处理label的触碰
     */
    fun excuteLabelTouch(event: MotionEvent) {
        labelList.forEach {
            if (it.isLabelInTouch(event.x - listener.getContentTransX(), event.y - listener.getContentTransY())) {
                selectLabel = it
                return@forEach
            }
        }
    }

}