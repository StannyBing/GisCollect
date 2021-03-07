package com.stanny.sketchpad.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.stanny.sketchpad.R
import com.stanny.sketchpad.bean.SketchPadFloorBean
import com.stanny.sketchpad.bean.SketchPadGraphicBean
import com.stanny.sketchpad.listener.SketchPadListener
import com.zx.zxutils.util.ZXToastUtil
import kotlinx.android.synthetic.main.layout_sketchpad_view.view.*
import java.util.*

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
        sketch_floor.sketchPadListener = this
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
     * 开始绘制标注
     */
    override fun drawLabel() {
        sketch_content.drawLabel()
    }

    /**
     * 保存图形
     */
    override fun saveGraphicInfo() {
        resetCenter()
        sketch_content.saveGraphicInfo{
            ZXToastUtil.showToast("保存成功")
        }
    }

    /**
     * 显示界址
     */
    override fun showSite(isCheck: Boolean) {
        sketch_content.showSite(isCheck)
    }

    /**
     * 楼层设置
     */
    override fun floorSetting(isCheck: Boolean) {
        sketch_floor.editFloor()
    }

    /**
     * 点击图形
     */
    override fun floorEdit(sketchPadFloorBean: SketchPadFloorBean) {
        sketch_content.floorEdit(sketchPadFloorBean)
        ZXToastUtil.showToast("请点击图形")
    }



    /**
     * 保存添加的图层信息
     */
    override fun saveFloor(sketchPadFloorBean: SketchPadFloorBean) {
        sketch_floor.insertFloorGraphic(sketchPadFloorBean)
    }

    override fun stopFloorEdit() {
        sketch_content.stopFloorEdit()
    }

    override fun showSizeInfo(checked: Boolean) {
        sketch_content.showSizeInfo(checked)
    }

    override fun deleteGraphic(id: UUID) {
        sketch_content.deleteGraphic(id)
    }
}