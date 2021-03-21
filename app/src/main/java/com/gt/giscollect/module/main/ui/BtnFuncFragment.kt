package com.gt.giscollect.module.main.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.location.LocationDataSource
import com.gt.base.fragment.BaseFragment
import com.stanny.module_rtk.tool.WHandTool
import com.gt.giscollect.R
import com.gt.base.app.ConstStrings
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.module.main.bean.FuncBean
import com.gt.giscollect.module.main.func.adapter.BtnFuncAdapter
import com.gt.giscollect.module.main.func.tool.IdentifyTool
import com.gt.module_map.tool.MapTool
import com.gt.giscollect.module.main.mvp.contract.BtnFuncContract
import com.gt.giscollect.module.main.mvp.model.BtnFuncModel
import com.gt.giscollect.module.main.mvp.presenter.BtnFuncPresenter
import com.gt.giscollect.module.query.ui.IdentifyFragment
import com.gt.giscollect.module.system.ui.SplashActivity
import com.gt.giscollect.tool.SimpleDecoration
import com.woncan.whand.WHandInfo
import com.zx.zxutils.util.ZXToastUtil
import kotlinx.android.synthetic.main.fragment_btn_func.*
import rx.functions.Action1


/**
 * Create By XB
 * 功能：按钮栏目
 */
class BtnFuncFragment : BaseFragment<BtnFuncPresenter, BtnFuncModel>(), BtnFuncContract.View {
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): BtnFuncFragment {
            val fragment = BtnFuncFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }

        enum class DataType {
            Layer,
            Collect,
            Survey,
            Search,
            Identify,
            Statistics,
            Setting,
            Measure,
        }
    }

    private val funcList = arrayListOf<FuncBean>()
    private val funcAdapter = BtnFuncAdapter(funcList)

    private var call: (DataType) -> Fragment? = { null }


    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.fragment_btn_func
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {

        if (MyApplication.isOfflineMode) {
            tv_func_offlinemode.visibility = View.VISIBLE
        } else {
            tv_func_offlinemode.visibility = View.GONE
        }

        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm01" } != null) funcList.add(
            FuncBean("图层", R.drawable.btn_func_layer)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm02" } != null) funcList.add(
            FuncBean("查询", R.drawable.btn_func_search)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm03" } != null) funcList.add(
            FuncBean("统计", R.drawable.btn_func_statistics)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm04" } != null) funcList.add(
            FuncBean("采集", R.drawable.btn_func_collect)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm09" } != null) funcList.add(
            FuncBean("调查", R.drawable.btn_func_survey)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm05" } != null) funcList.add(
            FuncBean("查要素", R.drawable.btn_func_identify)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm06" } != null) funcList.add(
            FuncBean("测量", R.drawable.btn_func_measure)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm07" } != null) funcList.add(
            FuncBean("定位", R.drawable.btn_func_location)
        )
        if (ConstStrings.appfuncList.firstOrNull { it.url == "appm08" } != null) funcList.add(
            FuncBean("设置", R.drawable.btn_func_setting)
        )
        rv_func_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = funcAdapter
            addItemDecoration(SimpleDecoration(requireActivity()))
        }

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        tv_func_offlinemode.setOnClickListener {
            SplashActivity.startAction(requireActivity(), true)
        }

        funcAdapter.setOnItemClickListener { adapter, view, position ->
            when (funcList[position].name) {
                "查询" -> {
                    call(DataType.Search)
                }
                "图层" -> {
                    call(DataType.Layer)
                }
                "统计" -> {
                    call(DataType.Statistics)
                }
                "采集" -> {
//                    MapTool.mapListener?.apply {
//                        measure_view.initMea(getMapView().onTouchListener as MapView.OnTouchListener, getMapView())
//                    }
                    call(DataType.Collect)
                }
                "调查" -> {
                    call(DataType.Survey)
                }
                "查要素" -> {
                    val identifyFragment = call(DataType.Identify) as IdentifyFragment
                    ZXToastUtil.showToast("请点击地图查询要素！")
                    IdentifyTool.startQueryIdentify(mContext) {
                        try {
                            identifyFragment.initIdentifyList(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                "测量" -> {
                    call(DataType.Measure)
                }
                "定位" -> {
                    MapTool.mapListener?.doLocation()
                }
                "设置" -> {
                    call(DataType.Setting)
                }
            }
        }
        rv_func_list.setOnClickListener(null)

        mRxManager.on("WHand", Action1<Boolean> {
            tv_rtk_info?.visibility = if (it) View.VISIBLE else View.GONE
        })

        WHandTool.addDeviceInfoListener(object : WHandTool.WHandDeviceListener {
            override fun onDeviceInfoCallBack(info: WHandInfo?) {
                try {
                    if (info == null){
                        tv_rtk_info?.visibility = View.GONE
                    }else{
                        tv_rtk_info?.visibility = View.VISIBLE
                    }
                    if (info?.longitude != null && info.longitude != 0.0) {
                        val method =
                            Class.forName("com.esri.arcgisruntime.location.LocationDataSource")
                                .getDeclaredMethod(
                                    "updateLocation",
                                    LocationDataSource.Location::class.java
                                )
                        MapTool.mapListener?.getMapView()
                            ?.locationDisplay?.locationDataSource?.apply {
                            method.isAccessible = true
                            method.invoke(
                                this,
                                LocationDataSource.Location(
                                    Point(
                                        info.longitude,
                                        info.latitude,
                                        SpatialReference.create(4326)
                                    )
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    tv_rtk_info?.visibility = View.GONE
                    e.printStackTrace()
                }


                handler.post { tv_rtk_info?.text = WHandTool.getRtkInfo() }
            }
        })
    }

    fun setDrawerCall(call: (DataType) -> Fragment?) {
        this.call = call
    }

    fun showMeasure() {
        measure_view.startMeasure()
    }

    fun closeMeasure() {
        measure_view.closeMeasure()
    }
}
