package com.gt.entrypad.module.project.ui.fragment

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.gt.base.fragment.BaseFragment
import com.gt.entrypad.R
import com.gt.entrypad.app.RouterPath
import com.gt.entrypad.module.project.mvp.contract.DrawSketchContract
import com.gt.entrypad.module.project.mvp.model.DrawSketchModel
import com.gt.entrypad.module.project.mvp.presenter.DrawSketchPresenter

@Route(path =RouterPath.DRAW_SKETCH)
class DrawSketchFragment : BaseFragment<DrawSketchPresenter, DrawSketchModel>(),DrawSketchContract.View{
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): DrawSketchFragment {
            val fragment = DrawSketchFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onViewListener() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_sketch_draw
    }

}