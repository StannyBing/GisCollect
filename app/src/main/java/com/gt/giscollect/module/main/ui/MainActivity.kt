package com.gt.giscollect.module.main.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.SketchCreationMode
import com.gt.base.activity.BaseActivity
import com.gt.base.app.ConstStrings
import com.gt.giscollect.R
import com.gt.giscollect.app.MyApplication
import com.gt.giscollect.module.collect.ui.CollectMainFragment
import com.gt.giscollect.module.collect.ui.SurveyMainFragment
import com.gt.giscollect.module.layer.ui.LayerFragment
import com.gt.module_map.listener.MapListener
import com.gt.giscollect.module.main.func.tool.IdentifyTool
import com.gt.module_map.tool.MapTool

import com.gt.giscollect.module.main.mvp.contract.MainContract
import com.gt.giscollect.module.main.mvp.model.MainModel
import com.gt.giscollect.module.main.mvp.presenter.MainPresenter
import com.gt.giscollect.module.query.ui.IdentifyFragment
import com.gt.giscollect.module.query.ui.MeasureFragment
import com.gt.giscollect.module.query.ui.SearchFragment
import com.gt.giscollect.module.query.ui.StatisticsFragment
import com.gt.giscollect.module.survey.ui.FileSearchFragment
import com.gt.giscollect.module.survey.ui.SurveyFragment
import com.gt.giscollect.module.system.ui.SettingMainFragment
import com.gt.map.MapFragment
import com.gt.module_map.tool.FileUtils
import com.gt.module_map.view.measure.MeasureView
import com.zx.zxutils.http.ZXHttpListener
import com.zx.zxutils.http.ZXHttpTool
import com.zx.zxutils.util.ZXDialogUtil
import com.zx.zxutils.util.ZXFileUtil
import com.zx.zxutils.util.ZXFragmentUtil
import com.zx.zxutils.util.ZXSystemUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_btn_func.*
import kotlinx.android.synthetic.main.fragment_map.*
import org.json.JSONObject
import rx.functions.Action1


/**
 * Create By XB
 * 功能：主页
 */
class MainActivity : BaseActivity<MainPresenter, MainModel>(), MainContract.View, MapListener,MapListener.OnSingleTapCall {

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    private lateinit var mapFragment: MapFragment
    private lateinit var btnFuncFragment: BtnFuncFragment
    private var layerFragment: LayerFragment? = null
    private var collectMainFragment: CollectMainFragment? = null
    private var surveyMainFragment: SurveyMainFragment? = null
    private var searchFragment: SearchFragment? = null
    private var identifyFragment: IdentifyFragment? = null
    private var statisticsFragment: StatisticsFragment? = null
    private var settingFragment: SettingMainFragment? = null
    private var measureFragment: MeasureFragment? = null
    private var surveyFragment:SurveyFragment?=null
    private var fileSearchFragment:FileSearchFragment?=null
    /**
     * layout配置
     */
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    /**
     * 初始化
     */
    override fun initView(savedInstanceState: Bundle?) {
        //地图
        ZXFragmentUtil.addFragment(
            supportFragmentManager,
            MapFragment.newInstance().apply {
                mapFragment = this
                mapFragment.addSingleTap(this@MainActivity)
            },
            R.id.fm_map
        )
        //功能按钮
        ZXFragmentUtil.addFragment(supportFragmentManager, BtnFuncFragment.newInstance().apply {
            btnFuncFragment = this
        }, R.id.fm_func)

        fm_data.setOnClickListener(null)
        fm_data.setOnTouchListener(null)

        MapTool.mapListener = this

        ZXHttpTool.getHttp(
            "http://49.233.40.212:20000/nearpal/file/temp/stanny.json",
            hashMapOf(),
            object : ZXHttpListener<String>() {
                override fun onResult(t: String?) {
                    try {
                        val obj = JSONObject(t)
                        if (!obj.getBoolean("isCorrect")) {
                            MyApplication.instance.finishAll()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(msg: String?) {
                }
            })

        mRxManager.on("toSettingRtk", Action1<Boolean> {
            excuteFuncCall(BtnFuncFragment.Companion.DataType.Setting)
        })

        super.initView(savedInstanceState)
    }

    /**
     * View事件设置
     */
    override fun onViewListener() {
        btnFuncFragment.setDrawerCall {
            return@setDrawerCall excuteFuncCall(it)
        }
        //收起菜单
        iv_data_hide.setOnClickListener {
            IdentifyTool.stopQueryIdentify()
            MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
            rl_main_data.animation =
                TranslateAnimation(0f, ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f)
                    .apply {
                        duration = 500
                        start()
                    }
            rl_main_data.visibility = View.GONE
            iv_data_show.visibility = View.VISIBLE
        }
        //打开菜单
        iv_data_show.setOnClickListener {
            if (rl_main_data.visibility != View.VISIBLE) {
                rl_main_data.animation =
                    TranslateAnimation(ZXSystemUtil.dp2px(260f).toFloat(), 0f, 0f, 0f)
                        .apply {
                            duration = 500
                            start()
                        }
                rl_main_data.visibility = View.VISIBLE
            }
            iv_data_show.visibility = View.GONE
        }
        mRxManager.on("surveySearch", Action1<String> {
            excuteFuncCall(BtnFuncFragment.Companion.DataType.SurveySearch)
        })
        mRxManager.on("surveySearchBack", Action1<String> {
            excuteFuncCall(BtnFuncFragment.Companion.DataType.SurveyFunction)
        })
        mRxManager.on("close", Action1 <String>{
            iv_data_hide.performClick()
        })
    }

    private fun excuteFuncCall(it: BtnFuncFragment.Companion.DataType): Fragment? {
        var showFragment: Fragment? = null
        layerFragment?.let { ZXFragmentUtil.hideFragment(it) }
        collectMainFragment?.let { ZXFragmentUtil.hideFragment(it) }
        surveyMainFragment?.let { ZXFragmentUtil.hideFragment(it) }
        searchFragment?.let { ZXFragmentUtil.hideFragment(it) }
        identifyFragment?.let { ZXFragmentUtil.hideFragment(it) }
        statisticsFragment?.let { ZXFragmentUtil.hideFragment(it) }
        settingFragment?.let { ZXFragmentUtil.hideFragment(it) }
        measureFragment?.let { ZXFragmentUtil.hideFragment(it) }
        surveyFragment?.let {
            ZXFragmentUtil.hideFragment(it) }
        fileSearchFragment?.let {
            ZXFragmentUtil.hideFragment(it)
        }
        when (it) {
            BtnFuncFragment.Companion.DataType.Layer -> {
                if (layerFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        LayerFragment.newInstance().apply {
                            layerFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = layerFragment
            }
            BtnFuncFragment.Companion.DataType.Collect -> {
                if (collectMainFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        CollectMainFragment.newInstance().apply {
                            collectMainFragment = this
                        },
                        R.id.fm_data
                    )
                }
                collectMainFragment?.reInit()
                showFragment = collectMainFragment
            }
            BtnFuncFragment.Companion.DataType.Survey -> {
                if (surveyMainFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        SurveyMainFragment.newInstance().apply {
                            surveyMainFragment = this
                        },
                        R.id.fm_data
                    )
                }
                surveyMainFragment?.reInit()
                showFragment = surveyMainFragment
            }
            BtnFuncFragment.Companion.DataType.Search -> {
                if (searchFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        SearchFragment.newInstance().apply {
                            searchFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = searchFragment
            }
            BtnFuncFragment.Companion.DataType.Statistics -> {
                if (statisticsFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        StatisticsFragment.newInstance().apply {
                            statisticsFragment = this
                        },
                        R.id.fm_data
                    )
                }
                statisticsFragment?.reInit()
                showFragment = statisticsFragment
            }
            BtnFuncFragment.Companion.DataType.Identify -> {
                if (identifyFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        IdentifyFragment.newInstance().apply {
                            identifyFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = identifyFragment
            }
            BtnFuncFragment.Companion.DataType.Setting -> {
                if (settingFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        SettingMainFragment.newInstance().apply {
                            settingFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = settingFragment
            }
            BtnFuncFragment.Companion.DataType.Measure -> {
                if (measureFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        MeasureFragment.newInstance().apply {
                            measureFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = measureFragment
            }
            //踏勘
            BtnFuncFragment.Companion.DataType.SurveyFunction->{
               surveyFragment?.apply {
                   ZXFragmentUtil.showFragment(this)
               }?: ZXFragmentUtil.addFragment(supportFragmentManager,SurveyFragment.newInstance().apply {
                   surveyFragment = this
               },R.id.fm_data)
               showFragment = surveyFragment
            }
            BtnFuncFragment.Companion.DataType.SurveySearch -> {
                if (fileSearchFragment == null) {
                    ZXFragmentUtil.addFragment(
                        supportFragmentManager,
                        FileSearchFragment.newInstance().apply {
                            fileSearchFragment = this
                        },
                        R.id.fm_data
                    )
                }
                showFragment = fileSearchFragment
            }
        }
        iv_data_show.performClick()
        if (showFragment != null) {
            ZXFragmentUtil.showFragment(showFragment)
        }
        //切换菜单，关闭要素模块
        if (showFragment !is IdentifyFragment) {
            if (!(showFragment is FileSearchFragment||showFragment is SurveyFragment)){
                IdentifyTool.stopQueryIdentify()
                MapTool.mapListener?.getMapView()?.sketchEditor?.stop()
            }
        }
        if (showFragment is MeasureFragment) {
            measureFragment?.startMeasure()
        }
        return showFragment
    }


    override fun doLocation() {
        mapFragment.doLocation()
    }

    override fun getMapView(): MapView? {
        return mapFragment.map_view
    }

    override fun getMap(): ArcGISMap? {
        return mapFragment.map_view?.map
    }

    override fun addSingleTapListener(singleTap: MapListener.OnSingleTapCall) {
        mapFragment.addSingleTap(singleTap)
    }

    override fun getMeasure(): MeasureView? {
        return btnFuncFragment.measure_view
    }

    override fun onLongPress(x: Float, y: Float) {
        excuteFuncCall(BtnFuncFragment.Companion.DataType.SurveyFunction)
//        surveyFragment?.setSurveyModule(SketchCreationMode.POINT, getMapView()?.screenToLocation(Point(x.toInt(),y.toInt())))
    }

    override fun onSingleTap(x: Float, y: Float) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        surveyFragment?.onActivityResult(requestCode, resultCode, data)
    }
}
