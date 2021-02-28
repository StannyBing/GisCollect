package com.gt.entrypad.module.project.ui.fragment

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.fragment_rtk_point.*

class RTKPointFragment :BaseFragment<SketchMainPresenter,SketchMainModel>(),SketchMainContract.View{
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
            layoutManager =LinearLayoutManager(mContext)
            adapter = rtkAdapter
            addItemDecoration(SimpleDecoration(mContext,height=2))
        }
        rtkAdapter.bindToRecyclerView(rtkPointRv)
    }
    override fun onViewListener() {
        btnGraphic.setOnClickListener {
            fragChangeListener?.onFragBack(SketchMainFragment.RTK_Point,rtkData)
        }
        rtkAdapter.addTextChangeListener { i, s ->
            rtkData[i].distance = if (s.isNotEmpty()) s.toDouble() else 0.0
        }
        rtkAdapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id){
                R.id.rtkTv->{
                    //TODO: rtk打点
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_rtk_point
    }

    fun showData(any: Any?){
        rtkData.clear()
        any?.let {
            if (it is SiteBean){
                if (it.rtkList.isNullOrEmpty()){
                    rtkData.add(RtkPointBean(title = "参考点P0",parentId = it.id))
                    rtkData.add(RtkPointBean(title = "参考点P1",parentId = it.id))
                    rtkData.add(RtkPointBean(title = "参考点P2",parentId = it.id))
                }else{
                    it.rtkList?.let { pointList->
                        for (index in pointList.indices){
                            val rtkPointBean = pointList[index]
                            rtkData.add(RtkPointBean(title = "参考点P$index",parentId = it.id,distance = rtkPointBean.distance,point = rtkPointBean.point))
                        }
                    }
                }
            }
        }
        rtkAdapter.notifyDataSetChanged()
    }

}