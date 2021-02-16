package com.stanny.sketchpad.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.adapter.SketchPadGraphicAdapter
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener


/**
 * 备选图形栏
 */
class SketchPadGraphicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private val graphicList = arrayListOf<SketchPadGraphicBean>()
    private val graphicAdapter = SketchPadGraphicAdapter(graphicList)

    init {
        setWillNotDraw(false)

        //初始化待选数据
        graphicList.add(SketchPadGraphicBean(SketchPadGraphicBean.GraphicType.RECTANGE))
        graphicList.add(SketchPadGraphicBean(SketchPadGraphicBean.GraphicType.TRAPEZIUM_L))
        graphicList.add(SketchPadGraphicBean(SketchPadGraphicBean.GraphicType.TRAPEZIUM_AO))
        graphicList.add(SketchPadGraphicBean(SketchPadGraphicBean.GraphicType.TRAPEZIUM_TU))
        graphicList.add(SketchPadGraphicBean(SketchPadGraphicBean.GraphicType.SQUARE))

        initView()
    }

    /**
     * 初始化界面
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {

        val rvGraphic = RecyclerView(context)
        addView(rvGraphic)
        rvGraphic.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = graphicAdapter
        }

        graphicAdapter.setOnItemClickListener { adapter, view, position ->
            val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100L)
            sketchPadListener?.graphicInsert(graphicList[position])
        }
    }

}