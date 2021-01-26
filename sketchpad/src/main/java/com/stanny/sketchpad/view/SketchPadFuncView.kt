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
 * 画板工具栏
 */
class SketchPadFuncView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null

    private val funcList = arrayListOf<SketchPadFuncBean>()
    private val funcAdapter = SketchPadFuncAdapter(funcList)

    private var selectGraphic: SketchPadGraphicBean? = null
    private var selectGraphicView: ImageView? = null
    private var touchPoint: PointF? = null

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
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(0, ZXSystemUtil.dp2px(20f), 0, 0)
        }
        addView(rvList)
        funcList.add(
            SketchPadFuncBean(
                "保存",
                R.drawable.icon_sketch_save,
                R.drawable.icon_sketch_noraml_save
            )
        )
        funcList.add(
            SketchPadFuncBean(
                "居中",
                R.drawable.icon_sketch_recenter,
                R.drawable.icon_sketch_normal_recenter
            )
        )

        funcList.add(
            SketchPadFuncBean(
                "界址",
                R.drawable.icon_sketch_setting,
                R.drawable.icon_sketch_normal_setting
            )
        )
        funcList.add(
            SketchPadFuncBean(
                "标注",
                R.drawable.icon_sketch_setting,
                R.drawable.icon_sketch_normal_setting
            )
        )
        funcList.add(
            SketchPadFuncBean(
                "楼层",
                R.drawable.icon_sketch_setting,
                R.drawable.icon_sketch_normal_setting
            )
        )
        funcList.add(
            SketchPadFuncBean(
                "备注",
                R.drawable.icon_sketch_setting,
                R.drawable.icon_sketch_normal_setting
            )
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
//            funcBean.isChecked = !funcBean.isChecked
            when (funcBean.name) {
                "保存" -> {
                    sketchPadListener?.saveGraphicInfo()
                }
                "居中" -> {
                    sketchPadListener?.resetCenter()
                }
                "配置" -> {
                    showSetting()
                }
                "界址"->{
                    sketchPadListener?.showSite()
                }
                "标注" -> {
                    sketchPadListener?.drawLabel()
                }
                "楼层"->{
                    sketchPadListener?.floorSetting()
                }
            }
            funcAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 打开设置栏
     */
    private fun showSetting() {
        val sketchSettingList = arrayListOf<SketchPadSettingBean>()
        sketchSettingList.add(SketchPadSettingBean("网格颜色", SketchPadSettingBean.SettingType.Switch))
        sketchSettingList.add(SketchPadSettingBean("图形颜色", SketchPadSettingBean.SettingType.Switch))
        sketchSettingList.add(SketchPadSettingBean("选中颜色", SketchPadSettingBean.SettingType.Switch))
        sketchSettingList.add(SketchPadSettingBean("图形宽度", SketchPadSettingBean.SettingType.Switch))
        sketchSettingList.add(SketchPadSettingBean("缩放比例", SketchPadSettingBean.SettingType.Switch))
        sketchSettingList.add(SketchPadSettingBean("自动贴边", SketchPadSettingBean.SettingType.Switch))
        val rvSetting = RecyclerView(context)
        rvSetting.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SketchPadSettingAdapter(sketchSettingList)
        }
        ZXDialogUtil.showCustomViewDialog(
            context,
            "配置",
            rvSetting
        ) { dialog: DialogInterface?, which: Int ->
            dialog?.dismiss()
        }
    }

}