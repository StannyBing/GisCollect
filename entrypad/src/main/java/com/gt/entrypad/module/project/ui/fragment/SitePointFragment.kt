package com.gt.entrypad.module.project.ui.fragment

import android.content.DialogInterface
import android.graphics.PointF
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gt.base.fragment.BaseFragment
import com.gt.base.listener.FragChangeListener
import com.gt.base.tool.RTKTool
import com.gt.entrypad.R
import com.gt.entrypad.module.project.bean.RtkPointBean
import com.gt.entrypad.module.project.bean.SiteBean
import com.gt.entrypad.module.project.func.adapter.SitePointAdapter
import com.gt.entrypad.module.project.mvp.contract.SketchMainContract
import com.gt.entrypad.module.project.mvp.model.SketchMainModel
import com.gt.entrypad.module.project.mvp.presenter.SketchMainPresenter
import com.gt.entrypad.module.project.ui.activity.SketchLoadActivity
import com.gt.entrypad.tool.SimpleDecoration
import com.zx.zxutils.util.ZXDialogUtil
import kotlinx.android.synthetic.main.fragment_site_point.*

class SitePointFragment : BaseFragment<SketchMainPresenter, SketchMainModel>(), SketchMainContract.View {
    private var siteData = arrayListOf<SiteBean>()
    private var siteAdapter = SitePointAdapter(siteData)
    var fragChangeListener: FragChangeListener? = null

    companion object {
        /**
         * 启动器
         */
        fun newInstance(): SitePointFragment {
            val fragment = SitePointFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewListener() {
        tvSitAdd.setOnClickListener {
            if (siteData.size<2){
                showSiteDialog()
            }else{
                showToast("最多选择两个界址点")
            }
        }
        btnGraphic.setOnClickListener {
            if (siteData.size<2){
                showToast("请添加界址点")
            }else{
                mSharedPrefUtil.putString("siteList",Gson().toJson(siteData))
                SketchLoadActivity.startAction(mActivity,false)
            }
        }
        siteAdapter.setOnItemClickListener { adapter, view, position ->
            fragChangeListener?.onFragGoto(LoadMainFragment.RTK_Point,siteData[position])
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        sitRv.apply {
            layoutManager = LinearLayoutManager(mContext)
            adapter = siteAdapter
            addItemDecoration(SimpleDecoration(mContext))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_site_point
    }
    private fun showSiteDialog(){
        var data = arrayListOf<String>()
        val siteHashmap = LinkedHashMap<String, PointF>()
        mSharedPrefUtil.getString("graphicList")?.let {
            val points =   Gson().fromJson<List<PointF>>(it,object : TypeToken<List<PointF>>(){}.type)
            if (!points.isNullOrEmpty()){
                points.forEachIndexed { index, pointF ->
                    val key = "界址点${index+1}"
                    siteHashmap[key]=pointF
                    data.add(key)
                }
                ZXDialogUtil.showListDialog(mContext,"请选择","确定",data,
                    DialogInterface.OnClickListener { dialog, which ->
                        siteHashmap[data[which]]?.let {point->
                            //是否相同
                            siteData.forEach {
                                if (it.title==data[which]&&it.point==point){
                                    showToast("界址点不能为同一个点")
                                    return@let
                                }
                            }
                            val siteBean = SiteBean(title = data[which], point = point)
                            siteData.add(siteBean)
                            siteAdapter.notifyDataSetChanged()
                            fragChangeListener?.onFragGoto(LoadMainFragment.RTK_Point,siteBean)
                        }
                    },
                    DialogInterface.OnClickListener { dialog, which ->

                    },false)
            }
        }

    }
    fun refreshData(any: Any?){
       any?.let {
           if (it is ArrayList<*>){
               val list = it as ArrayList<RtkPointBean>
               if (list.isNotEmpty()&&siteData.isNotEmpty()){
                   siteData.forEach {
                       if (it.id==list[0].parentId){
                           it.rtkList?.clear()
                           it.rtkList?.addAll(list)
                           return@forEach
                       }
                   }
               }
           }
       }
        siteAdapter.notifyDataSetChanged()
    }
}