package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import kotlinx.android.synthetic.main.item_rtk_point_layout.view.*

class RTKPointFragment : BaseFragment<SketchMainPresenter, SketchMainModel>(),
    SketchMainContract.View {
    private var rtkData = arrayListOf<RtkPointBean>()
    private var rtkAdapter = RtkPointAdapter(rtkData)
     var fragChangeListener: FragChangeListener? = null
    private var rtkPointBean = RtkPointBean()
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
    fun onFragGo(){
        fragChangeListener?.onFragGoto(LoadMainFragment.Site_Point, rtkData)
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
        rtkLayout.itemLatEt.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                var  point = rtkPointBean.sitePoint
                var text = s.toString().trim()
                point = Point(if (s.isNullOrEmpty()) 0.0 else text.toDouble(),point.y)
                rtkPointBean.sitePoint = point
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        rtkLayout.itemLngEt.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                var  point = rtkPointBean.sitePoint
                var text = s.toString().trim()
                point =Point(point.x,if (s.isNullOrEmpty()) 0.0 else text.toDouble())
                rtkPointBean.sitePoint = point
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        rtkLayout.itemDistanceEt.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                rtkPointBean.distance = if (s.toString().trim().isNullOrEmpty()) 0.0 else s.toString().trim().toDouble()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        rgRtk.setOnCheckedChangeListener { group, checkedId ->
            setCheckStatus(checkedId)
        }

        btnGraphic.setOnClickListener {
            if (rbReferPoint.isChecked){
                rtkData.forEach {
                    if (!(it.distance != 0.0 && it.sitePoint.x != 0.0 && it.sitePoint.y!= 0.0)) {
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
            }else if (rbDirectPoint.isChecked){
                if (!(rtkPointBean.distance != 0.0 && rtkPointBean.sitePoint.x != 0.0 && rtkPointBean.sitePoint.y!= 0.0)) {
                    showToast("坐标或者距离不能为0")
                    return@setOnClickListener
                }
                rtkPointBean.resultSitePoint = rtkPointBean.sitePoint
                fragChangeListener?.onFragBack(LoadMainFragment.RTK_Point, rtkPointBean)
            }
        }
        rtkAdapter.addTextChangeListener { i, s ->
            rtkData[i].distance = if (s.isNotEmpty()) s.toDouble() else 0.0
        }

        rtkAdapter.addLatTextChangeListener { i, s ->
            var  sitePoint = rtkData[i].sitePoint
            sitePoint = Point(if (s.isNotEmpty()) s.toDouble() else 0.0,sitePoint.y)
            rtkData[i].sitePoint = sitePoint
        }

        rtkAdapter.addLngTextChangeListener { i, s ->
            var  sitePoint = rtkData[i].sitePoint
            sitePoint = Point(sitePoint.x,if (s.isNotEmpty()) s.toDouble() else 0.0)
            rtkData[i].sitePoint = sitePoint
        }

        rtkAdapter.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.rtkTv -> {
                    if (WHandTool.isOpen) {
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
        if (rbReferPoint.isChecked){
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
        }else {
            any?.let {
                rtkPointBean = RtkPointBean()
                if (it is SiteBean) {
                    if (it.rtkList.isNullOrEmpty()) {
                       rtkPointBean = RtkPointBean(title = "参考点P0", parentId = it.id)
                    } else {
                        it.rtkList?.let { pointList ->
                            for (index in pointList.indices) {
                                val temP = pointList[index]
                                rtkPointBean =  RtkPointBean(
                                    title = "参考点P$index",
                                    parentId = it.id,
                                    distance = temP.distance,
                                    point = temP.point,
                                    sitePoint = temP.sitePoint,
                                    resultSitePoint = temP.resultSitePoint
                                )
                            }
                        }
                    }
                }
            }
            rtkLayout.itemLatEt.setText(if (rtkPointBean.sitePoint.x==0.0) "" else "${rtkPointBean.sitePoint.x}")
            rtkLayout.itemLngEt.setText(if (rtkPointBean.sitePoint.y==0.0) "" else "${rtkPointBean.sitePoint.y}")
            rtkLayout.itemDistanceEt.setText(if (rtkPointBean.distance==0.0) "" else "${rtkPointBean.distance}")
        }
    }


    private fun setCheckStatus(viewId:Int){
        when(viewId){
            //参考打点
            rbReferPoint.id->{
                rtkPointRv.visibility= View.VISIBLE
                rtkLayout.visibility=View.GONE
            }
            //直接打点
            rbDirectPoint.id->{
                rtkLayout.visibility=View.VISIBLE
                rtkLayout.rtkTv.visibility=View.GONE
                rtkPointRv.visibility=View.GONE
            }
        }
    }

}