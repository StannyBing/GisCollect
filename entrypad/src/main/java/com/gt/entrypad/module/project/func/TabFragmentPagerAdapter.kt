package com.gt.entrypad.module.project.func

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabFragmentPagerAdapter(var fragmentManager: FragmentManager,var fragmentList:List<Fragment>) :FragmentPagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

}