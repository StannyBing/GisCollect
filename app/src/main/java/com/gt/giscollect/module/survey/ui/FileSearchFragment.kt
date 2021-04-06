package com.gt.giscollect.module.survey.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gt.base.app.ConstStrings
import com.gt.base.fragment.BaseFragment
import com.gt.entrypad.tool.SimpleDecoration
import com.gt.giscollect.R
import com.gt.giscollect.module.survey.bean.FileInfoBean
import com.gt.giscollect.module.survey.bean.FileResultBean
import com.gt.giscollect.module.survey.func.adapter.FileResultAdapter
import com.gt.giscollect.module.survey.mvp.contract.FileSearchContract
import com.gt.giscollect.module.survey.mvp.modle.FileSearchModel
import com.gt.giscollect.module.survey.mvp.presenter.FileSearchPresenter
import com.zx.zxutils.util.ZXFileUtil
import kotlinx.android.synthetic.main.fragment_file_search.*
import java.io.File

class FileSearchFragment :BaseFragment<FileSearchPresenter,FileSearchModel>(),FileSearchContract.View{
    private var fileList = arrayListOf<FileResultBean>()
    private var tempList = arrayListOf<FileResultBean>()
    private var fileAdapter = FileResultAdapter(fileList)
    private var isFile = false
    companion object {
        /**
         * 启动器
         */
        fun newInstance(): FileSearchFragment {
            val fragment = FileSearchFragment()
            val bundle = Bundle()

            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initData()
    }

    override fun onViewListener() {
        rv_search_list.apply {
            layoutManager = GridLayoutManager(mContext,3).apply {
                spanSizeLookup = object :GridLayoutManager.SpanSizeLookup(){
                    override fun getSpanSize(position: Int): Int {
                        when(fileList[position].itemType){
                            1->{
                                return  3
                            }
                            2->{
                                return  1
                            }
                        }
                        return 3
                    }
                }
            }
            adapter = fileAdapter

        }
        iv_search_do.setOnClickListener {
            if (et_search_text?.text.toString().trim().isNullOrEmpty()){
               restData()
            }else{
                getDirectory(et_search_text?.text.toString().trim())
            }
        }
        fileAdapter.setOnItemClickListener { adapter, view, position ->
            when(fileList[position].itemType){
                1->{
                    getFileByPath(fileList[position].fileInfoBean.path)
                    isFile = true
                }
                2->{

                }
            }
        }
        et_search_text.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()){
                   restData()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        ivReturn.setOnClickListener {
           if (isFile){
               if (et_search_text.text.toString().isNullOrEmpty()){
                 restData()
               }else{
                   getDirectory(et_search_text?.text.toString().trim())
               }
           }else{
               mRxManager.post("surveySearchBack","")
           }
            isFile= false
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_file_search
    }

    /**
     * 重置数据
     */
    private fun restData(){
        fileList.clear()
        fileList.addAll(tempList)
        fileAdapter.notifyDataSetChanged()
    }

    private fun initData(){
        File(ConstStrings.getSurveySearchPath())?.apply {
            listFiles().forEach {
                //如果是文件夹
                if (it.isDirectory){
                    val index = it.path.lastIndexOf("/")
                    var name = it.absolutePath.substring(index+1,it.absolutePath.length)
                    fileList.add(FileResultBean("",1,FileInfoBean(name,path=it.absolutePath)))
                }
            }
        }
        tempList.addAll(fileList)
        fileAdapter.notifyDataSetChanged()
    }
    /**
     * 获取文件夹
     */
    private fun getDirectory(dirName:String){
       fileList.clear()
       File(ConstStrings.getSurveySearchPath())?.apply {
            listFiles().forEach {
                //如果是文件夹
                if (it.isDirectory){
                    val index = it.path.lastIndexOf("/")
                    var name = it.absolutePath.substring(index+1,it.absolutePath.length)
                    if (name.contains(dirName)){
                        fileList.add(FileResultBean(dirName,1,FileInfoBean(name,path=it.absolutePath)))
                    }
                }
            }
        }
        fileAdapter.notifyDataSetChanged()
    }
    /**
     * 获取文件夹下面所有文件
     */
    private fun getFileByPath(path:String){
        fileList.clear()
        File(path)?.apply {
            listFiles().forEach {
                //如果是文件
                if (it.isFile&&(ZXFileUtil.getFileExtension(it)=="jpg"||ZXFileUtil.getFileExtension(it)=="png"||ZXFileUtil.getFileExtension(it)=="jpeg")){
                    val index = it.path.lastIndexOf("/")
                    var name = it.absolutePath.substring(index+1,it.absolutePath.length)
                    fileList.add(FileResultBean(itemStyle=2, fileInfoBean = FileInfoBean(name,pathImage = it.absolutePath)))
                }
            }
        }
        fileAdapter.notifyDataSetChanged()
    }
}
