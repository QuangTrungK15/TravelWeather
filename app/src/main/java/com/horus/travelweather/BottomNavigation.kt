package com.horus.travelweather

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.horus.travelweather.R.id.progress_loading
import com.horus.travelweather.activity.*
import kotlinx.android.synthetic.main.activity_bottom_navigation.*
import kotlinx.android.synthetic.main.fragment_weather_details.*

class BottomNavigation : AppCompatActivity() {

    val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                getSupportActionBar()!!.setTitle("Home")
                val homeFragment = HomeFragment.newInstance()
                openFragment(homeFragment)
                //progress_loading.visibility = View.VISIBLE
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                getSupportActionBar()!!.setTitle("Địa Điểm Yêu Thích")
                val favouriteFragment = FavoritePlaceFragment.newInstance()
                //progress_loading.visibility = View.GONE
                openFragment(favouriteFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_direction -> {
                getSupportActionBar()!!.setTitle("Chỉ Đường")
                val directionFragment = DirectionsFragment.newInstance("")
                //progress_loading.visibility = View.GONE
                openFragment(directionFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                getSupportActionBar()!!.setTitle("Nhật Ký Hoạt Động")
                val historyFragment = HistoryFragment.newInstance()
                //progress_loading.visibility = View.GONE
                openFragment(historyFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                getSupportActionBar()!!.setTitle("Profile")
                val profileFragment = NewProfileFragment.newInstance()
                //progress_loading.visibility = View.GONE
                openFragment(profileFragment)
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_home
        val homeFragment = HomeFragment.newInstance()
        openFragment(homeFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
        getSupportActionBar()!!.setTitle("Home");
        val homeFragment = HomeFragment.newInstance()
        openFragment(homeFragment)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
