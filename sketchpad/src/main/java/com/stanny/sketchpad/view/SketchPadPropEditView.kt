package com.stanny.sketchpad.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Color
import android.os.Vibrator
import android.util.AttributeSet
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadGraphicAdapter
import com.stanny.sketchpad.adapter.SketchPadPropEditAdapter
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.zx.zxutils.util.ZXScreenUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.layout_sketchpad_propedit.view.*


/**
 * 图形编辑栏
 */
class SketchPadPropEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private val propEditList = arrayListOf<Float>()
    private val propEditAdapter = SketchPadPropEditAdapter(propEditList)

    private var selectGraphic: SketchPadGraphicBean? = null

    init {
        setWillNotDraw(false)

        View.inflate(context, R.layout.layout_sketchpad_propedit, this)

        visibility = View.INVISIBLE

        initView()

        initListener()
    }

    /**
     * 初始化界面
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        rv_propedit_prop.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = propEditAdapter
        }
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        //放置点击事件穿透
        setOnClickListener(null)
        //关闭按钮
        iv_propedit_close.setOnClickListener {
            animation = TranslateAnimation(0f, width.toFloat(), 0f, 0f).apply {
                duration = 500
                start()
            }
            visibility = View.GONE
            sketchPadListener?.closeEdit()
        }
        //保存按钮
        btn_propedit_submit.setOnClickListener {
            selectGraphic?.setGraphicInfo(propEditList)
            sketchPadListener?.refreshGraphic()
            editGraphic(selectGraphic!!)
//            iv_propedit_close.performClick()
        }
    }

    /**
     * 编辑图形数据
     */
    fun editGraphic(graphicBean: SketchPadGraphicBean) {
        selectGraphic = graphicBean
        propEditAdapter.selectGraphic = graphicBean
        propEditList.clear()
        propEditList.addAll(graphicBean.getGraphicMetreList())
        propEditAdapter.notifyDataSetChanged()

        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            animation = TranslateAnimation(width.toFloat(), 0f, 0f, 0f).apply {
                duration = 500
                start()
            }
        }
    }

}