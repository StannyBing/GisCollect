package com.gt.base

import android.os.Bundle
import android.view.View
import com.frame.zxmvp.base.BaseModel
import com.frame.zxmvp.base.BasePresenter
import com.gt.base.fragment.BaseFragment

/**
Create By Pxb on 2019/7/2
 */
abstract class BaseLazyFragment<T : BasePresenter<*, *>, E : BaseModel>() : BaseFragment<T, E>() {
    //是否复用
    private var isReuseView: Boolean = true
    private var mRootView: View? = null
    //是否第一次可见
    private var isFirstVisible: Boolean = false
    //Fragment 是否可见
    private var isFragmentVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariable()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mRootView == null) {
            mRootView = view
            //第一次调用 onFragmentVisibleChange在之后调用 是方便支持ui操作
            if (userVisibleHint) {
                if (isFirstVisible) {
                    onFragmentFirstVisible()
                    isFirstVisible = false
                }
                setFragmentVisibleChange(true)
            }
        }
        super.onViewCreated((if (isReuseView && mRootView != null) mRootView else view)!!, savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        //setUserVisibleHint()有可能在Fragment生命周期外调用
        if (mRootView == null) {
            return
        }
        if (isFirstVisible && isVisibleToUser) {
            onFragmentFirstVisible()
            isFirstVisible = false
        }
        if (isVisibleToUser) {
            setFragmentVisibleChange(true)
            return
        }
        if (isFragmentVisible) {
            setFragmentVisibleChange(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        initVariable()
    }

    private fun setFragmentVisibleChange(isVisible: Boolean) {
        onFragmentVisibleChange(isVisible)
        isFragmentVisible = isVisible
    }

    /**
     * 初始化
     */
    private fun initVariable() {
        isFirstVisible = true
        isFragmentVisible = false
        mRootView = null
        isReuseView = true
    }

    /**
     *去除setUserVisibleHint()多余得回调场景，保证只有当前Fragment可见状态发生变化时才回调
     * 回调时机再View创建完成之后，支持ui操作 解决在setUserVisibleHint()中进行ui操作可能报null
     *@param isVisible true 不可见->可见
     *                   false 可见->不可见
     */
    private  fun onFragmentVisibleChange(isVisible: Boolean){
    }

    /**
     *再Fragment首次可见时回调，可在这里进行懒加载数据
     * 此方法在onFragmentVisibleChange(isVisible: Boolean)之前调用
     *
     */
    protected  fun onFragmentFirstVisible(){
        lazyLoad()
    }

    abstract fun lazyLoad()
    /**
     * 设置是否复用View 默认开启
     * ViewPager在销毁和 重建Fragment时会不断调用onCreateView->onDestoryView()之间的生命函数
     * 会出现重复创建View得情况，导致页面上显示多个得Fragment
     *
     */
    protected fun setIsReuseView(isReuse: Boolean) {
        this.isReuseView = isReuse
    }
}