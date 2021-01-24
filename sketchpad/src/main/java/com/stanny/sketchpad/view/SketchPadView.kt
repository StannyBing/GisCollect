package com.stanny.sketchpad.view

import android.content.Context
import android.content.DialogInterface
import android.graphics.PointF
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadLabelAdapter
import com.stanny.sketchpad.bean.SketchLabelBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXScreenUtil
import com.zx.zxutils.util.ZXToastUtil
import kotlinx.android.synthetic.main.layout_sketchpad_view.view.*

/**
 * 房屋画板主View
 */
class SketchPadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), SketchPadListener {

    init {
        setWillNotDraw(false)
        View.inflate(context, R.layout.layout_sketchpad_view, this)

        initListener()
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        sketch_content.sketchPadListener = this
        sketch_func.sketchPadListener = this
        sketch_graphic.sketchPadListener = this
        sketch_propedit.sketchPadListener = this
        sketch_label.sketchPadListener = this
    }

    /**
     * 图形插入
     */
    override fun graphicInsert(graphicBean: SketchPadGraphicBean) {
        sketch_content.insertGraphic(graphicBean)
    }

    /**
     * 图形编辑
     */
    override fun graphicEdit(graphicBean: SketchPadGraphicBean) {
        sketch_propedit.editGraphic(graphicBean)
    }

    /**
     * 关闭编辑
     */
    override fun closeEdit() {
        sketch_content.closeEdit()
    }

    /**
     * 刷新图形
     */
    override fun refreshGraphic() {
        sketch_content.refreshGraphic()
    }

    /**
     * 重置中央
     */
    override fun resetCenter() {
        sketch_content.resetCenter()
    }

    /**
     * 保存图形
     */
    override fun saveGraphicInfo() {
        resetCenter()
        sketch_content.saveGraphicInfo {
            ZXToastUtil.showToast("保存成功")
        }
    }

    /**
     * 标注开关
     */
    override fun switchLabel(isLabel: Boolean) {
        sketch_label.visibility = if (isLabel)View.VISIBLE else View.GONE
    }

    /**
     * 开始标注
      */
    override fun drawLabel(pointF: PointF) {
        var selectValue = ""
        val data = arrayListOf<SketchLabelBean>()
        val labelAdapter = SketchPadLabelAdapter(data).apply {
            setOnItemChildClickListener { adapter, view, position ->
                kotlin.run label@{
                    data.forEachIndexed { index, sketchLabelBean ->
                        if (index==position){
                            sketchLabelBean.isChecked = !sketchLabelBean.isChecked
                            if (sketchLabelBean.isChecked)selectValue= sketchLabelBean.value
                        }else{
                            sketchLabelBean.isChecked = false
                        }
                    }
                }
                notifyDataSetChanged()
            }
        }
        val view = LayoutInflater.from(context).inflate(R.layout.layout_label_dialog,null).apply {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = labelAdapter
            findViewById<EditText>(R.id.otherEt).addTextChangedListener(object :TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    selectValue=s?.toString()?.trim()?:""
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

        sketch_content.getCurrentGraphic(pointF)?.let {
            //弹窗 图形内
            view.findViewById<EditText>(R.id.otherEt).visibility=View.GONE
            data.addAll(showInData())
        }?:data.addAll(showOutData())
        ZXDialogUtil.showCustomViewDialog(context,"",view, { dialog, which ->
            //获取所选项
            sketch_label.drawLabel(selectValue)
        }, { dialog, which ->

        }).apply {
            val window = window
            val layoutParams = window?.attributes
            layoutParams?.width = ZXScreenUtil.getScreenWidth()/3
            layoutParams?.gravity = Gravity.RIGHT
            window?.attributes = layoutParams
            show()
        }
    }
    private fun showInData():ArrayList<SketchLabelBean>{
      return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1","阳台"))
            add(SketchLabelBean("2","内阳台"))
            add(SketchLabelBean("3","砖湿"))
            add(SketchLabelBean("4","砖瓦"))
            add(SketchLabelBean("5","滴水"))
            add(SketchLabelBean("6","猪圈"))
        }
    }
    private fun showOutData():ArrayList<SketchLabelBean>{
        return arrayListOf<SketchLabelBean>().apply {
            add(SketchLabelBean("1","坝"))
            add(SketchLabelBean("2","人行道"))
            add(SketchLabelBean("3","水沟"))
            add(SketchLabelBean("4","巷道"))
            add(SketchLabelBean("5","林地"))
            add(SketchLabelBean("6","耕地"))
        }
    }
}