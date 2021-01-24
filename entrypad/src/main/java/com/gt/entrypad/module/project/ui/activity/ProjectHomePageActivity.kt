package com.gt.entrypad.module.project.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.gt.base.activity.BaseActivity
import com.gt.entrypad.R
import com.gt.base.view.ICustomViewActionListener
import com.gt.base.viewModel.BaseCustomViewModel
import com.gt.entrypad.module.project.func.TabFragmentPagerAdapter
import com.gt.entrypad.module.project.mvp.contract.ProjectHomePageContract
import com.gt.entrypad.module.project.mvp.model.ProjectHomePageModel
import com.gt.entrypad.module.project.mvp.presenter.ProjectHomePagePresenter
import com.gt.entrypad.module.project.ui.fragment.*
import com.gt.entrypad.module.project.ui.view.titleView.TitleViewViewModel
import kotlinx.android.synthetic.main.activity_project_home_page.*
import kotlinx.android.synthetic.main.layout_tool_bar.*

class ProjectHomePageActivity : BaseActivity<ProjectHomePagePresenter, ProjectHomePageModel>(),ProjectHomePageContract.View{
    private var fragmentList = arrayListOf<Fragment>()
    private var pagerAdapter = TabFragmentPagerAdapter(supportFragmentManager,fragmentList)
    private var currentPageNum = 0//当前页面
    private var takePhotoFragment:TakePhotoFragment?=null
    private var infoInputFragment:InfoInputFragment?=null

    companion object {
        /**
         * 启动器
         */
        fun startAction(activity: Activity, isFinish: Boolean) {
            val intent = Intent(activity, ProjectHomePageActivity::class.java)
            activity.startActivity(intent)
            if (isFinish) activity.finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        leftTv.apply {
            setData(TitleViewViewModel(getString(R.string.lastStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    when(currentPageNum){
                        0->{
                            finish()
                        }
                        else->{
                            currentPageNum--
                            tabViewPager.currentItem = currentPageNum
                        }
                    }
                }

            })
        }
        rightTv.apply {
            visibility =View.VISIBLE
            setData(TitleViewViewModel(getString(R.string.nextStep)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    if (currentPageNum<fragmentList.size-1){
                        currentPageNum++
                        tabViewPager.currentItem = currentPageNum
                    }
                }

            })
        }
        finishTv.apply {
            setData(TitleViewViewModel(getString(R.string.finish)))
            setActionListener(object : ICustomViewActionListener {
                override fun onAction(action: String, view: View, viewModel: BaseCustomViewModel) {
                    mRxManager.post("finish",infoInputFragment?.getData())
                }

            })
        }
        initFragment()
        initViewPager()
    }
    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_project_home_page
    }

    private fun initFragment(){
        fragmentList.apply {
            add(ScanIdCardFragment.newInstance())
            add(InfoInputFragment.newInstance().apply {
                infoInputFragment=this
            })
            add(TakePhotoFragment.newInstance().apply {
                takePhotoFragment = this
            })
            add(DrawSketchFragment.newInstance())
            add(GroundFigureFragment.newInstance())
            add(ResultShowFragment.newInstance())
        }
    }


    private fun initViewPager(){
        tabViewPager.apply {
            setScanScroll(false)
            offscreenPageLimit=fragmentList.size
            setOnPageChangeListener(object :ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                @SuppressLint("SetTextI18n")
                override fun onPageSelected(position: Int) {
                    currentPageNum = position
                    rightTv.visibility = View.VISIBLE
                    finishTv.visibility = View.VISIBLE
                    when(position){
                        0->{
                            toolBarTitleTv.text = getString(R.string.registrationPlatform)
                            finishTv.visibility =View.GONE
                        }
                        1->{
                            toolBarTitleTv.text = getString(R.string.registrationInfo)+"$currentPageNum/${fragmentList.size-1}"
                            finishTv.visibility =View.GONE
                        }
                        2->{
                            toolBarTitleTv.text =getString(R.string.takePhoto)+"$currentPageNum/${fragmentList.size-1}"
                        }
                        3->{
                            toolBarTitleTv.text =getString(R.string.sketchDraw)+"$currentPageNum/${fragmentList.size-1}"
                        }
                        4->{
                            toolBarTitleTv.text =getString(R.string.groundFigure)+"$currentPageNum/${fragmentList.size-1}"

                        }
                        5->{
                            toolBarTitleTv.text =getString(R.string.resultShow)+"$currentPageNum/${fragmentList.size-1}"
                            rightTv.visibility = View.INVISIBLE
                        }
                    }
                }

            })
            adapter = pagerAdapter
            currentItem = 0
        }
        toolBarTitleTv.text = getString(R.string.registrationPlatform)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        takePhotoFragment?.onActivityResult(requestCode,resultCode,data)
    }

}