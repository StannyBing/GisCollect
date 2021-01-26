package com.stanny.sketchpad.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.DialogInterface
import android.graphics.PointF
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadFuncAdapter
import com.stanny.sketchpad.adapter.SketchPadSettingAdapter
import com.stanny.sketchpad.bean.SketchPadFuncBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.bean.SketchPadSettingBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchPadConstant
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXSystemUtil


/**
 * 画板操作栏
 */
class SketchPadOperationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private val funcList = arrayListOf<SketchPadFuncBean>()
    private val funcAdapter = SketchPadFuncAdapter(funcList)

    init {
        setWillNotDraw(false)

        initFuncList()

        initListener()
    }

    /**
     * 初始化界面
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initFuncList() {
        val rvList = RecyclerView(context).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(0, ZXSystemUtil.dp2px(20f), 0, 0)
        }
        addView(rvList)
        funcList.add(
            SketchPadFuncBean(
                "",
                R.drawable.icon_sketch_finish,
                R.drawable.icon_sketch_finish
            ,true)
        )
        rvList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = funcAdapter
        }
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        funcAdapter.setOnItemClickListener { adapter, view, position ->
            val funcBean = funcList[position]
            when (funcBean.name) {
                ""->{
                    sketchPadListener?.finish()
                }
            }
            funcAdapter.notifyDataSetChanged()
        }
    }
}