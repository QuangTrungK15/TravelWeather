package com.horus.travelweather.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import android.view.View
import com.horus.travelweather.R
import com.horus.travelweather.R.id.progress_loading
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.fragment.AddLocationFragment
import com.horus.travelweather.fragment.WeatherDetailFragment
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

open class ViewPagerAdapter(fragmentManager: FragmentManager, private var listPlaces: List<PlaceEntity>) :
        FragmentPagerAdapter(fragmentManager) {

    private val TAG = ViewPagerAdapter::class.java.simpleName;

    override fun getItem(position: Int): Fragment {
        Log.e(TAG, " Test : " + position);
        return newInstance(position, listPlaces)
    }

    override fun getCount(): Int {
        return listPlaces.size + 2
    }

    //    companion object {
    fun newInstance(position: Int, listPlaces: List<PlaceEntity>): Fragment {

        //Vuốt khi nào hết các placelist fragment của user đó thì nó sẽ hiển thị add location fragment
        return if (position >= 0 && position < (listPlaces.size + 1))
            newInsWeather(position, listPlaces)
        else
            newInsAddLocation(position)
    }

    private fun newInsWeather(position: Int, listPlaces: List<PlaceEntity>): WeatherDetailFragment {
        val fragment = WeatherDetailFragment()
        //use Bundle() to exchange among intent
        //this activity receives "position" from getDataFromLocal() of HomeActivity
        val args = Bundle()
        args.putInt("position", position)
        if (position != 0) {
            args.putDouble("lat", listPlaces[position - 1].latitude)
            args.putDouble("lon", listPlaces[position - 1].longitude)
            Log.e(TAG, " lat : " + listPlaces[position - 1].latitude);
            Log.e(TAG, " lon : " + listPlaces[position - 1].latitude);
        }
        fragment.arguments = args
        return fragment
    }

    private fun newInsAddLocation(position: Int): AddLocationFragment {
        val fragment = AddLocationFragment()
        val args = Bundle()
        args.putInt("position", position)
        fragment.arguments = args
        return fragment
    }

//    }
}