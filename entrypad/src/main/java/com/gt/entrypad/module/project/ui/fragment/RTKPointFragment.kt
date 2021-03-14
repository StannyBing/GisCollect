package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.bean.SiteBean
import com.gt.entrypad.module.project.func.adapter.RtkPointAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.module_map.tool.PointTool
import com.stanny.module_rtk.tool.RTKTool
import com.stanny.module_rtk.tool.WHandTool
import kotlinx.android.synthetic.main.fragment_rtk_point.*

class RTKPointFragment : BaseFragment<SketchMainPresenter, SketchMainModel>(),
    SketchMainContract.View {
    private var rtkData = arrayListOf<RtkPointBean>()
    private var rtkAdapter = RtkPointAdapter(rtkData)
    var fragChangeListener: FragChangeListener? = null

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): RTKPointFragment {
            val fragment = RTKPointFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        rtkPointRv.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = rtkAdapter
            addItemDecoration(SimpleDecoration(mContext, height = 2))
        }
        rtkAdapter.bindToRecyclerView(rtkPointRv)
        RTKTool.rtkActualLocationTest()
    }

    override fun onViewListener() {
        btnGraphic.setOnClickListener {
            rtkData.forEach {
                if (!(it.distance != 0.0 && it.point.x.toDouble() != 0.0 && it.point.y.toDouble() != 0.0)) {
                    showToast("${it.title}的坐标或者距离不能为0")
                    return@setOnClickListener
                }
            }
            if (rtkData.size == 4) {
                val rtkList = arrayListOf<DoubleArray>()
                rtkData.forEach {
                    val doubleArray = doubleArrayOf(it.sitePoint.x,it.sitePoint.y,it.sitePoint.z,it.distance)
                    rtkList.add(doubleArray)
                }
                val doubles = RTKTool.meaUtil(rtkList)
                doubles?.let { double->
                    rtkData.forEach {
                        it.resultSitePoint = Point(double[0],double[1])
                    }
                }
                fragChangeListener?.onFragBack(LoadMainFragment.RTK_Point, rtkData)
            }
        }
        rtkAdapter.addTextChangeListener { i, s ->
            rtkData[i].distance = if (s.isNotEmpty()) s.toDouble() else 0.0
        }
        rtkAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.rtkTv -> {
                    if (WHandTool.isConnect && WHandTool.isOpen) {
                        val info = WHandTool.getDeviceInfoOneTime()
                        if (info != null) {
                            //TODO:Rtk生成的x y z坐标 z的值赋给Point的z
                            val location = PointTool.change4326To3857(
                                Point(
                                    info.longitude,
                                    info.latitude,
                                    0.0,
                                    SpatialReference.create(4326)
                                )
                            )
                            rtkData[position].sitePoint = location
                            rtkAdapter.notifyDataSetChanged()
                        }
                    } else {
                        fragChangeListener?.onFragGoto(LoadMainFragment.RTK_Set)
                    }
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_rtk_point
    }

    fun showData(any: Any?) {
        any?.let {
            rtkData.clear()
            if (it is SiteBean) {
                if (it.rtkList.isNullOrEmpty()) {
                    rtkData.add(RtkPointBean(title = "参考点P0", parentId = it.id))
                    rtkData.add(RtkPointBean(title = "参考点P1", parentId = it.id))
                    rtkData.add(RtkPointBean(title = "参考点P2", parentId = it.id))
                    rtkData.add(RtkPointBean(title = "参考点P3", parentId = it.id))
                } else {
                    it.rtkList?.let { pointList ->
                        for (index in pointList.indices) {
                            val rtkPointBean = pointList[index]
                            rtkData.add(
                                RtkPointBean(
                                    title = "参考点P$index",
                                    parentId = it.id,
                                    distance = rtkPointBean.distance,
                                    point = rtkPointBean.point,
                                    sitePoint = rtkPointBean.sitePoint,
                                    resultSitePoint = rtkPointBean.resultSitePoint
                                )
                            )
                        }
                    }
                }
            }
            rtkAdapter.notifyDataSetChanged()
        }
    }

}