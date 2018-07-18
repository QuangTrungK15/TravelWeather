package com.example.dell.travelweather.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.dell.travelweather.fragment.HourInfoFragment

//
//class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
//
//    private val mFragmentList = ArrayList<Fragment>()
//    private val mFragmentTitleList = ArrayList<String>()
//
//
//
//    override fun getItem(position: Int): Fragment {
//        return mFragmentList[position]
//    }
//
//    override fun getCount(): Int {
//        return mFragmentList.size
//    }
//
//
//    fun addFragment(fragment: Fragment, title: String) {
//        mFragmentList.add(fragment)
//        mFragmentTitleList.add(title)
//    }
//
//    override fun getPageTitle(position: Int): CharSequence {
//        return mFragmentTitleList[position]
//    }
//
//
//}

class ViewPagerAdapter(fragmentManager: FragmentManager, var titles: List<String>) :
        FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return newInstance(position)
    }

    override fun getCount(): Int {
        return titles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles.get(position)
    }

    companion object {
        fun newInstance(position: Int): HourInfoFragment {
            val fragment = HourInfoFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }
}