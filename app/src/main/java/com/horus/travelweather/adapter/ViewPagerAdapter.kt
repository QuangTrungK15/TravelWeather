package com.horus.travelweather.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.fragment.AddLocationFragment
import com.horus.travelweather.fragment.WeatherDetailFragment

open class ViewPagerAdapter(fragmentManager: FragmentManager, private var listPlaces : List<PlaceData>) :
        FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return newInstance(position,listPlaces)
    }

    override fun getCount(): Int {
        return listPlaces.size+2
    }

    companion object {
        fun newInstance(position: Int, listPlaces : List<PlaceData>) : Fragment {

            //Vuốt khi nào hết các placelist fragment của user đó thì nó sẽ hiển thị add location fragment
            return if(position >= 0 && position < (listPlaces.size+1))
                newInsWeather(position,listPlaces)
            else
                newInsAddLocation(position)
        }
        private fun newInsWeather(position: Int, listPlaces : List<PlaceData>): WeatherDetailFragment {
            val fragment = WeatherDetailFragment()
            //use Bundle() to exchange among intent
            //this activity receives "position" from getDataFromLocal() of HomeActivity
            val args = Bundle()
            args.putInt("position", position)
            if(position!=0) {
                args.putDouble("lat", listPlaces[position - 1].latitude)
                args.putDouble("lon", listPlaces[position - 1].longitude)
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
    }
}