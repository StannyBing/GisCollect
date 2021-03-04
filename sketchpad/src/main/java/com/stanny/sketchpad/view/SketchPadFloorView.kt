package com.stanny.sketchpad.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Color
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.stanny.sketchpad.R
import com.stanny.sketchpad.adapter.SketchPadFloorAdapter
import com.stanny.sketchpad.adapter.SketchPadFloorGraphicAdapter
import com.stanny.sketchpad.adapter.SketchPadGraphicAdapter
import com.stanny.sketchpad.adapter.SketchPadPropEditAdapter
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.zx.zxutils.util.ZXScreenUtil
import com.zx.zxutils.util.ZXSharedPrefUtil
import com.zx.zxutils.util.ZXSystemUtil
import com.zx.zxutils.util.ZXToastUtil
import kotlinx.android.synthetic.main.layout_sketchpad_floor.view.*
import kotlinx.android.synthetic.main.layout_sketchpad_propedit.view.*


/**
 * 楼层编辑栏
 */
class SketchPadFloorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    var sketchPadListener: SketchPadListener? = null
    private var floorData = arrayListOf<SketchPadFloorBean>()
    private var floorAdapter = SketchPadFloorAdapter(floorData)

    private var floorPos = 0

    private var floorGraphicData = arrayListOf<SketchPadGraphicBean>()
    private var floorGraphicAdapter = SketchPadFloorGraphicAdapter(floorGraphicData)

    init {
        setWillNotDraw(false)

        View.inflate(context, R.layout.layout_sketchpad_floor, this)

        visibility = View.INVISIBLE

        initView()

        initListener()
    }

    /**
     * 初始化界面
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        floorRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = floorAdapter
        }
        floorGraphicRv.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = floorGraphicAdapter
        }
        floorAdapter.setOnItemClickListener { adapter, view, position ->
            floorPos = position
            sketchPadListener?.floorEdit(floorData[floorPos])
            titleFloorTv.text = floorData[floorPos].name
            floorAdapter.notifyDataSetChanged()
            floorGraphicData.clear()
            floorGraphicData.addAll(floorData[floorPos].sketchList)
            floorGraphicAdapter.notifyDataSetChanged()
            floorBackIv.visibility = View.VISIBLE
            floorGraphicRv.visibility = View.VISIBLE
            floorRv.visibility = View.GONE
            floorAddBtn.text = "保存"
        }
        initData()
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        //放置点击事件穿透
        setOnClickListener(null)
        //关闭按钮
        floorCloseIv.setOnClickListener {
            dismiss()
            sketchPadListener?.stopFloorEdit()
        }
        //添加
        floorAddBtn.setOnClickListener {
            if (floorRv.visibility == View.VISIBLE) {
                floorData.add(
                    floorData.size,
                    SketchPadFloorBean(
                        name = "${floorData.size + 1}楼",
                        sketchList = arrayListOf<SketchPadGraphicBean>()
                    )
                )
                floorAdapter.notifyDataSetChanged()
            } else {
                ZXSharedPrefUtil().putString("floorGraphicList", Gson().toJson(floorGraphicData))
                ZXSharedPrefUtil().remove("floorList")
                ZXSharedPrefUtil().putString("floorList", Gson().toJson(floorData))
                ZXToastUtil.showToast("保存成功")
                floorBackIv.performClick()
                floorGraphicData.clear()
                floorGraphicAdapter.notifyDataSetChanged()
            }
            sketchPadListener?.stopFloorEdit()
        }
        //返回 更换数据
        floorBackIv.setOnClickListener {
            floorBackIv.visibility = View.GONE
            floorGraphicRv.visibility = View.GONE
            floorRv.visibility = View.VISIBLE
            titleFloorTv.text = "楼层"
            floorAddBtn.text = "添加"
            floorGraphicRv.visibility = View.GONE
            sketchPadListener?.stopFloorEdit()
            floorGraphicData.clear()
            floorGraphicAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 编辑楼层数据
     */
    fun editFloor() {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            animation = TranslateAnimation(width.toFloat(), 0f, 0f, 0f).apply {
                duration = 500
                start()
            }
        }
    }

    /**
     * 保存添加楼层下的图形信息
     */
    fun insertFloorGraphic(sketchPadFloorBean: SketchPadFloorBean) {
        floorGraphicData.clear()
//        floorData[floorPos].sketchList.clear()
//        floorData[floorPos].sketchList.addAll(sketchPadFloorBean.sketchList)
        floorGraphicData.addAll(sketchPadFloorBean.sketchList)
        floorGraphicAdapter.notifyDataSetChanged()
    }


    /**
     * 默认初始化数据 三层
     */
    private fun initData() {
        floorData.apply {
            add(SketchPadFloorBean(name = "1楼", sketchList = arrayListOf<SketchPadGraphicBean>()))
            add(SketchPadFloorBean(name = "2楼", sketchList = arrayListOf<SketchPadGraphicBean>()))
            add(SketchPadFloorBean(name = "3楼", sketchList = arrayListOf<SketchPadGraphicBean>()))
        }
    }

    fun dismiss() {
        animation = TranslateAnimation(0f, width.toFloat(), 0f, 0f).apply {
            duration = 500
            start()
        }
        visibility = View.GONE
    }
}