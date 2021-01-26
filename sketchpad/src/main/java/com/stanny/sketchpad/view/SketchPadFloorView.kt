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
import com.stanny.sketchpad.adapter.SketchPadFloorAdapter
import com.stanny.sketchpad.adapter.SketchPadGraphicAdapter
import com.stanny.sketchpad.adapter.SketchPadPropEditAdapter
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.zx.zxutils.util.ZXScreenUtil
import com.zx.zxutils.util.ZXSystemUtil
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
        floorAdapter.addCheckedChangeListener {position->
            floorData.forEach {
                if (it==floorData[position]){
                    it.isChecked = !it.isChecked
                    sketchPadListener?.floorEdit(it)
                }else{
                    it.isChecked =false
                }
            }
            floorAdapter.notifyDataSetChanged()
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
        }
        //添加
        floorAddBtn.setOnClickListener {
            floorData.add(floorData.size, SketchPadFloorBean(name = "${floorData.size+1}楼",sketchList = arrayListOf()))
            floorAdapter.notifyDataSetChanged()
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
     * 默认初始化数据 三层
     */
    private fun initData(){
        floorData.apply {
            add(SketchPadFloorBean(name = "1楼",sketchList = arrayListOf()))
            add(SketchPadFloorBean(name = "2楼",sketchList = arrayListOf()))
            add(SketchPadFloorBean(name = "3楼",sketchList = arrayListOf()))
        }
    }
    fun dismiss(){
        animation = TranslateAnimation(0f, width.toFloat(), 0f, 0f).apply {
            duration = 500
            start()
        }
        visibility = View.GONE
    }
}