package com.gt.module_map.view.measure

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.Graphic
import com.gt.module_map.R
import com.zx.zxutils.util.ZXLogUtil

/**
 * 绘制操作管理器
 */
class MeaOptManager(private var context: Context, private var meaListener: MeaListener) {

    companion object {
        private const val MENU_ADD = "添加"
        private const val MENU_DELETE = "删除"
        private const val MENU_INSERT = "插入"
        private const val MENU_CUT = "裁切"
        private const val MENU_TRANS = "平移"
        private const val MENU_ROTATE = "旋转"
        private const val MENU_STOPTRANS = "结束平移"
    }

    /**
     * 操作实体类
     */
    data class OptBean(
        var optType: OptType? = null,

        var pointAdd: Point? = null,
        var pointMoveFrom: Point? = null,
        var pointMoveTo: Point? = null,
        var pointInsert: Point? = null,
        var pointDelete: Point? = null,
        var pointTransStart: Point? = null,
        var pointTransEnd: Point? = null,

        var addIndex: Int? = null,
        var moveIndex: Int? = null,
        var insertIndex: Int? = null,
        var deleteIndex: Int? = null
    ) {
        enum class OptType {
            ADD,
            MOVE,
            INSERT,
            DELETE,
            TRNAS
        }
    }

    private val optUndoList = arrayListOf<OptBean>()//上一步操作集合
    private val optRedoList = arrayListOf<OptBean>()//下一步操作集合

    var popMenuPoint: Point? = null//菜单弹出点位置
    var funcIndex = -1 //操作点


    /**
     * 加入添加操作
     */
    fun optAdd(point: Point) {
        val bean = OptBean().apply {
            optType = OptBean.OptType.ADD
            pointAdd = point
            addIndex = meaListener.getPointList().size
        }
        optUndoList.add(bean)
        doOpt(bean)
        //清空下一步操作序列
        optRedoList.clear()
        meaListener.onMapReAdd()
    }

    /**
     * 加入移动操作
     */
    fun optMove(moveIndex: Int, pointFrom: Point, pointTo: Point) {
        val bean = OptBean().apply {
            optType = OptBean.OptType.MOVE
            pointMoveFrom = pointFrom
            pointMoveTo = pointTo
            this.moveIndex = moveIndex
        }
        optUndoList.add(bean)
        //清空下一步操作序列
        optRedoList.clear()
        meaListener.onMapReAdd()
    }

    /**
     * 加入插入操作
     */
    fun optInsert(point: Point, index: Int) {
        val bean = OptBean().apply {
            optType = OptBean.OptType.INSERT
            pointInsert = point
            insertIndex = index
        }
        optUndoList.add(bean)
        doOpt(bean)
        //清空下一步操作序列
        optRedoList.clear()
        meaListener.onMapReAdd()
    }

    /**
     * 加入删除操作
     */
    fun optDelete(point: Point, index: Int) {
        val bean = OptBean().apply {
            optType = OptBean.OptType.DELETE
            pointDelete = point
            deleteIndex = index
        }
        optUndoList.add(bean)
        doOpt(bean)
        //清空下一步操作序列
        optRedoList.clear()
        meaListener.onMapReAdd()
    }

    /**
     * 加入平移操作
     */
    fun optTrans(pointStart: Point, pointEnd: Point) {
        val bean = OptBean().apply {
            optType = OptBean.OptType.TRNAS
            pointTransStart = pointStart
            pointTransEnd = pointEnd
        }
        optUndoList.add(bean)
        //清空下一步操作序列
        optRedoList.clear()
    }

    /**
     * 执行上一步操作
     * @return 是否已为最后一步
     */
    fun postUndo(): Boolean {
        meaListener.stopTranslate()
        if (optUndoList.isNotEmpty()) {
            doOpt(optUndoList.last(), false, true)
            return optUndoList.isEmpty()
        }
        return false
    }

    /**
     * 执行下一步操作
     * @return 是否已为最后一步
     */
    fun postRedo(): Boolean {
        meaListener.stopTranslate()
        if (optRedoList.isNotEmpty()) {
            doOpt(optRedoList.last(), true, true)
            return optRedoList.isEmpty()
        }
        return false
    }

    /**
     * 清除
     */
    fun clear() {
        optUndoList.clear()
        optRedoList.clear()
    }

    /**
     * 执行操作
     * @param bean
     * @param idRedo 是否为下一步操作，是则正向执行，否则反向执行
     * @param isPost 是否是通过postRedo或者postUndo执行的
     */
    private fun doOpt(bean: OptBean, isRedo: Boolean = true, isPost: Boolean = false) {
        when (bean.optType) {
            OptBean.OptType.ADD -> {
                if (isRedo) {
                    meaListener.getPointList().add(bean.pointAdd!!)
                } else {
                    meaListener.getPointList().removeAt(bean.addIndex!!)
//                    MeaUtil.removeLast(meaListener.getPointList(), bean.pointAdd!!)
                }
            }
            OptBean.OptType.MOVE -> {
                if (isRedo) {
                    meaListener.getPointList()[bean.moveIndex!!] = bean.pointMoveTo!!
                } else {
                    meaListener.getPointList()[bean.moveIndex!!] = bean.pointMoveFrom!!
                }
            }
            OptBean.OptType.INSERT -> {
                if (isRedo) {
                    meaListener.getPointList().add(bean.insertIndex!!, bean.pointInsert!!)
                } else {
                    meaListener.getPointList().removeAt(bean.insertIndex!!)
                }
            }
            OptBean.OptType.DELETE -> {
                if (isRedo) {
                    meaListener.getPointList().removeAt(bean.deleteIndex!!)
                } else {
                    meaListener.getPointList().add(bean.deleteIndex!!, bean.pointDelete!!)
                }
            }
            OptBean.OptType.TRNAS -> {
                val transLon = bean.pointTransEnd!!.x - bean.pointTransStart!!.x
                val transLat = bean.pointTransEnd!!.y - bean.pointTransStart!!.y
                if (isRedo) {
                    meaListener.getPointList().forEachIndexed { index, it ->
                        meaListener.getPointList()[index] = Point(
                            it.x + transLon,
                            it.y + transLat,
                            it.spatialReference
                        )
                    }
                } else {
                    meaListener.getPointList().forEachIndexed { index, it ->
                        meaListener.getPointList()[index] = Point(
                            it.x - transLon,
                            it.y - transLat,
                            it.spatialReference
                        )
                    }
                }
            }
        }
        meaListener.updateGraphic()
        if (isPost) {
            if (isRedo) {
                optRedoList.remove(bean)
                optUndoList.add(bean)
            } else {
                optRedoList.add(bean)
                optUndoList.remove(bean)
            }
        }
    }

    /**
     * 执行长按事件-弹出操作菜单
     */
    fun showMeaMenu(graphic: Graphic) {
        closeMarker()
        if (popMenuPoint != null) {
            val callout = meaListener.getMapView().callout
//            val view = LayoutInflater.from(context).inflate(R.layout.map_meamenu_layout, null, false)
//            val llMenu = view.findViewById<LinearLayout>(R.id.ll_mea_menu)
            val llMenu = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val funcList = arrayListOf<String>()
            if (graphic.geometry is Point) {
                funcList.add(MENU_DELETE)
            } else if (graphic.geometry is Polyline) {
                funcList.add(MENU_INSERT)
            } else if (graphic.geometry is Polygon) {
                if (meaListener.isTranslate()) {
                    funcList.add(MENU_STOPTRANS)
                } else {
                    funcList.add(MENU_TRANS)
                }
            }
            if (funcList.isNotEmpty()) {
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, MeaUtil.dp2px(context, 20f))
                params.gravity = Gravity.CENTER
                funcList.forEachIndexed { index, it ->
                    llMenu.addView(TextView(context).apply {
                        text = it
                        setTextColor(Color.WHITE)
                        textSize = 12f
                        gravity = Gravity.CENTER
                        minWidth = MeaUtil.dp2px(context, 30f)
                        layoutParams = params
                        tag = it
                        setOnClickListener(MenuClickListener())
                    })
                    if (funcList.size > 1 && index < funcList.size - 1) {
                        val divider = View(context)
                        divider.layoutParams = LinearLayout.LayoutParams(MeaUtil.dp2px(context, 1f), MeaUtil.dp2px(context, 20f))
                        divider.setBackgroundColor(Color.WHITE)
                        llMenu.addView(divider)
                    }
                }
                callout.refresh()
                meaListener.getMapView().callout.style.apply {
                    borderColor = ContextCompat.getColor(context, R.color.colorPrimary)
                    backgroundColor = ContextCompat.getColor(context, R.color.colorPrimary)
//                    minHeight = MeaUtil.dp2px(context, 30f)
                    minHeight = 5
                    cornerRadius = 5
                    leaderLength = 8
                }
                callout.show(llMenu, popMenuPoint)
            }
        }
    }

    /**
     * 关闭弹窗
     */
    fun closeMarker(): Boolean {
        if (meaListener.getMapView().callout.isShowing) {
            meaListener.getMapView().callout.dismiss()
            return true
        }
        return false
    }


    //菜单点击事件
    private inner class MenuClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            when (v.tag.toString()) {
                "删除" -> if (funcIndex != -1) {
                    optDelete(popMenuPoint!!, funcIndex)
                }
                "插入" -> if (funcIndex != -1) {
                    optInsert(popMenuPoint!!, funcIndex)
                }
                "裁切" -> {
                }
                "平移" -> meaListener.startTranslate()
                "结束平移" -> meaListener.stopTranslate()
                "旋转" -> {
                }
            }
            closeMarker()
        }
    }
}