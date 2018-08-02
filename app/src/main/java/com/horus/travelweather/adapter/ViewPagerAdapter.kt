package com.horus.travelweather.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.fragment.AddLocationFragment
import com.horus.travelweather.fragment.WeatherDetailFragment

open class ViewPagerAdapter(fragmentManager: FragmentManager, var listPlaces : List<PlaceData>) :
        FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return newInstance(position,listPlaces)
    }

    override fun getCount(): Int {
        return listPlaces.size+1
    }

    companion object {
        fun newInstance(position: Int, listPlaces : List<PlaceData>) : Fragment {

            if(position!=listPlaces.size) {
               return newInsWeather(position,listPlaces)
            }
            else {
               return newInsAddLocation(position)
            }


        }
        fun newInsWeather(position: Int,listPlaces : List<PlaceData>): WeatherDetailFragment {
            val fragment = WeatherDetailFragment()
            val args = Bundle()
            args.putInt("position", position)
            args.putDouble("lat",listPlaces[position].latitude)
            args.putDouble("lon",listPlaces[position].longitude)
            fragment.arguments = args
            return fragment
        }
        fun newInsAddLocation(position: Int): AddLocationFragment {
            val fragment = AddLocationFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }
}