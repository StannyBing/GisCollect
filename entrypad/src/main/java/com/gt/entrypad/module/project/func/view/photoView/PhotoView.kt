package com.gt.entrypad.module.project.func.view.photoView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gt.entrypad.R
import com.gt.base.view.BaseCustomView
import com.gt.entrypad.databinding.LayoutPhotoViewBinding

class PhotoView  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr:Int = 0): BaseCustomView<LayoutPhotoViewBinding, PhotoViewViewModel>(context, attrs, defStyleAttr){
    override fun getLayoutId(): Int {
        return R.layout.layout_photo_view
    }

    override fun onRootClick(view: View) {

    }

    override fun setDataToView(data: PhotoViewViewModel) {
        getDataBinding().viewModel = data
        Glide.with(context)
            .load(if (data.url.isEmpty()&&data.resId!=0){
                data.resId
            } else data.url)
            .apply(
               RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(com.zx.zxutils.R.drawable.__picker_default_weixin)
                    .error(R.drawable.icon_img_error)
            )
            .into(getDataBinding().photoViewIv.apply {
                val params = layoutParams as ConstraintLayout.LayoutParams
                params.width =data.width
                params.height=data.height
                layoutParams= params
            })
        getDataBinding().photoViewDeleteIv.visibility = if (data.resId!=0) View.GONE else View.VISIBLE
    }

    override fun setStyle(resId: Int) {

    }

}