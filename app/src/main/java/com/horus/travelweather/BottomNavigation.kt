package com.horus.travelweather

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.horus.travelweather.activity.*
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigation : AppCompatActivity() {

    val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                getSupportActionBar()!!.setTitle("Home")
                val homeFragment = HomeFragment.newInstance()
                openFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                getSupportActionBar()!!.setTitle("Favorite Places")
                val favouriteFragment = FavoritePlaceFragment.newInstance()
                openFragment(favouriteFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_direction -> {
                getSupportActionBar()!!.setTitle("Direction")
                val directionFragment = DirectionsFragment.newInstance("")
                openFragment(directionFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                getSupportActionBar()!!.setTitle("History")
                val historyFragment = HistoryFragment.newInstance()
                openFragment(historyFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                getSupportActionBar()!!.setTitle("Profile")
                val profileFragment = NewProfileFragment.newInstance()
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
