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
import kotlinx.android.synthetic.main.layout_sketchpad_customedit.view.*
import kotlinx.android.synthetic.main.layout_sketchpad_propedit.view.*


/**
 * 图形自定义绘制
 */
class SketchPadCustomEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private var selectGraphic: SketchPadGraphicBean? = null

    init {
        setWillNotDraw(false)

        View.inflate(context, R.layout.layout_sketchpad_customedit, this)

        visibility = View.INVISIBLE

        initView()

        initListener()
    }

    /**
     * 初始化界面
     */
    private fun initView() {

    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        //放置点击事件穿透
        setOnClickListener(null)
        //关闭按钮
        iv_customedit_close.setOnClickListener {
            animation = TranslateAnimation(0f, width.toFloat(), 0f, 0f).apply {
                duration = 500
                start()
            }
            visibility = View.GONE
            sketchPadListener?.closeEdit()
        }
        //保存按钮
        btn_customedit_submit.setOnClickListener {

        }
    }

    /**
     * 开启自定义绘制
     */
    fun startCustomEdit(){
        visibility = View.VISIBLE
    }

}