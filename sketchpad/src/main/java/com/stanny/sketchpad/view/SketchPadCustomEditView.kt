package com.stanny.sketchpad.view

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.stanny.sketchpad.tool.SketchPadConstant
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXToastUtil
import kotlinx.android.synthetic.main.layout_sketchpad_customedit.view.*


/**
 * 图形自定义绘制
 */
class SketchPadCustomEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null
    private var customListener: SketchCustomListener? = null
    private var drawDirection = 1//1上 2下 3左 4右

    private val customTempPoints = arrayListOf<PointF>()

    interface SketchCustomListener {
        fun getCustomGraphic(): SketchPadGraphicBean?
        fun refreshGraphic()
        fun closeCustomEdit()
        fun saveCustomEdit()
    }

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
        resetButtonColor()
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        //放置点击事件穿透
        setOnClickListener(null)
        //关闭按钮
        iv_customedit_close.setOnClickListener {
            if (customListener?.getCustomGraphic()?.points?.isEmpty() == false) {
                ZXDialogUtil.showYesNoDialog(context, "提示", "暂未保存，是否清除自定义打点记录？") { dialog, which ->
                    closeView()
                }
            } else {
                closeView()
            }
        }
        //顺时针
        tv_custom_rotate_1.setOnClickListener {

        }
        //逆时针
        tv_custom_rotate_2.setOnClickListener {

        }
        //初始化-添加
        tv_custom_add.setOnClickListener {
            if (et_custom_distance.text.isEmpty()) {
                ZXToastUtil.showToast("请输入距离")
                return@setOnClickListener
            }
            et_custom_distance.text.toString().toFloatOrNull().let {
                if (it == null) {
                    ZXToastUtil.showToast("请输入正确的距离值")
                    return@setOnClickListener
                }
                val drawPx =
                    it * SketchPadConstant.backgroundGridSpace / SketchPadConstant.graphicRatio
                customListener?.getCustomGraphic()?.points?.last()?.let { point ->
                    var newPointx = point.x
                    var newPointy = point.y
                    when (drawDirection) {
                        1 -> {
                            newPointy -= drawPx
                        }
                        2 -> {
                            newPointx += drawPx
                        }
                        3 -> {
                            newPointy += drawPx
                        }
                        4 -> {
                            newPointx -= drawPx
                        }
                    }
                    val pointNew = PointF(newPointx, newPointy)
                    customListener?.getCustomGraphic()?.points?.add(pointNew)
                    customTempPoints.clear()
                    customListener?.refreshGraphic()
                }
            }
        }
        //上一步
        tv_custom_undo.setOnClickListener {
            customListener?.getCustomGraphic()?.points?.apply {
                if (size > 1) {
                    customTempPoints.add(last())
                    removeAt(lastIndex)
                    customListener?.refreshGraphic()
                }
            }
        }
        //下一步
        tv_custom_redo.setOnClickListener {
            customListener?.getCustomGraphic()?.points?.apply {
                if (customTempPoints.isNotEmpty()) {
                    add(customTempPoints.last())
                    customTempPoints.removeAt(customTempPoints.lastIndex)
                    customListener?.refreshGraphic()
                }
            }
        }
        //清除
        tv_custom_clear.setOnClickListener {
            val firstPointf = customListener?.getCustomGraphic()?.points?.first()
            customListener?.getCustomGraphic()?.points?.clear()
            firstPointf?.let {
                customListener?.getCustomGraphic()?.points?.add(it)
            }
            customTempPoints.clear()
            customListener?.refreshGraphic()
        }
        //封闭
        tv_custom_close.setOnClickListener {
            customListener?.getCustomGraphic()?.points?.apply {
                if (size < 3) {
                    ZXToastUtil.showToast("打点数过少，无法形成封闭图形")
                } else {
                    customListener?.getCustomGraphic()?.customPathClose = true
                    customListener?.refreshGraphic()
                }
            }
        }
        //保存按钮
        btn_customedit_submit.setOnClickListener {
            customListener?.getCustomGraphic()?.points?.apply {
                if (size < 3) {
                    ZXToastUtil.showToast("打点数过少，无法形成封闭图形")
                } else {
                    customListener?.getCustomGraphic()?.customPathClose = true
                    customListener?.saveCustomEdit()
                    closeView()
                }
            }
        }
        //向上
        tv_custom_drawup.setOnClickListener {
            drawDirection = 1
            resetButtonColor()
        }
        //向右
        tv_custom_drawright.setOnClickListener {
            drawDirection = 2
            resetButtonColor()
        }
        //向下
        tv_custom_drawdown.setOnClickListener {
            drawDirection = 3
            resetButtonColor()
        }
        //向左
        tv_custom_drawleft.setOnClickListener {
            drawDirection = 4
            resetButtonColor()
        }
    }

    private fun closeView() {
        customTempPoints.clear()
        animation = TranslateAnimation(0f, width.toFloat(), 0f, 0f).apply {
            duration = 500
            start()
        }
        visibility = View.GONE
        sketchPadListener?.closeEdit()
    }

    private fun resetButtonColor() {
        val drawableUp = tv_custom_drawup.background.mutate()
        val drawableRight = tv_custom_drawright.background.mutate()
        val drawableDown = tv_custom_drawdown.background.mutate()
        val drawableLeft = tv_custom_drawleft.background.mutate()
        drawableUp.setTint(ContextCompat.getColor(context, R.color.colorAccent))
        drawableRight.setTint(ContextCompat.getColor(context, R.color.colorAccent))
        drawableDown.setTint(ContextCompat.getColor(context, R.color.colorAccent))
        drawableLeft.setTint(ContextCompat.getColor(context, R.color.colorAccent))
        when (drawDirection) {
            1 -> drawableUp.setTint(ContextCompat.getColor(context, R.color.colorPrimary))
            2 -> drawableRight.setTint(ContextCompat.getColor(context, R.color.colorPrimary))
            3 -> drawableDown.setTint(ContextCompat.getColor(context, R.color.colorPrimary))
            4 -> drawableLeft.setTint(ContextCompat.getColor(context, R.color.colorPrimary))
        }
    }

    /**
     * 开启自定义绘制
     */
    fun startCustomEdit() {
        visibility = View.VISIBLE
        customListener?.refreshGraphic()
    }

    fun setSketchCustomListener(sketchCustomListener: SketchCustomListener) {
        customListener = sketchCustomListener
    }

}