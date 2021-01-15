package com.gt.giscollect.base

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class VerticalSwipeRefreshLayout(context: Context, attrs: AttributeSet?) : SwipeRefreshLayout(context, attrs) {
    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop

    // 上一次触摸时的X坐标
    private var mPrevX = 0f
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> mPrevX = event.getX()
            MotionEvent.ACTION_MOVE -> {
                val eventX: Float = event.getX()
                val xDiff = Math.abs(eventX - mPrevX)
                // Log.d("refresh" ,"move----" + eventX + "   " + mPrevX + "   " + mTouchSlop);
                // 增加60的容差，让下拉刷新在竖直滑动时就可以触发
                if (xDiff > mTouchSlop + 60) {
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    init {

        // 触发移动事件的最短距离，如果小于这个距离就不触发移动控件
    }
}