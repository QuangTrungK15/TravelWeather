package com.horus.travelweather

import android.Manifest
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.activity.FavouritePlaceFragment
import com.horus.travelweather.activity.HomeFragment
import com.horus.travelweather.activity.ProfileFragment
import com.horus.travelweather.adapter.ViewPagerAdapter
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bottom_navigation.*

class BottomNavigation : AppCompatActivity() {
    private val TAG = BottomNavigation::class.java.simpleName
    private val compositeDisposable = CompositeDisposable()
    private val menu : MutableList<PlaceEntity> = mutableListOf()
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val homeFragment = HomeFragment.newInstance()
                openFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                val favouriteFragment = FavouritePlaceFragment.newInstance()
                openFragment(favouriteFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_direction -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                val profileFragment = ProfileFragment.newInstance()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
        val homeFragment = HomeFragment.newInstance()
        openFragment(homeFragment)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        database = FirebaseDatabase.getInstance()
//        mAuth = FirebaseAuth.getInstance()
//        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
//        val rxPermissions = RxPermissions(this)
//        rxPermissions
//                .request(Manifest.permission.ACCESS_FINE_LOCATION)
//                .subscribe { granted ->
//                    if (granted) {
//                        //to read firebase date, we need ValueEventListener
//                        place_list.addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onCancelled(p0: DatabaseError) {
//                                Log.e(TAG,"Error : "+p0.message)
//                            }
//                            override fun onDataChange(dataSnapshot : DataSnapshot) {
//                                val placeList = ArrayList<PlaceEntity>()
//                                // Result will be holded Here
//                                for (dsp in dataSnapshot.children) {
//                                    //add result into array list
//                                    val item : PlaceEntity? = dsp.getValue(PlaceEntity::class.java)
//                                    if (item != null) {
//                                        placeList.add(item)
//                                    }
//                                }
//                                Log.e(TAG,"Size : "+placeList.size)
//                                insertAllPlace().execute(placeList)
//                                //Bind fragments on viewpager
//                            }
//                        })
//                    } else {
//                        // Oups permission denied
//                        Log.e(TAG, "Access fail")
//                    }
//                }

    }

//    private fun getDataFromLocal()
//    {
//        // RxJava là thư viện mã nguồn mở implement ReactiveX trên Java. Có 2 lớp chính là Observable và Subscriber:
//        // Observable là một lớp đưa ra dòng dữ liệu hoặc sự kiện (event). Flow của Observable là đưa ra một
//        // hoặc nhiều các items, sau đó gọi kết thúc thành công hoặc lỗi.
//        // Subscriber lắng nghe flow, thực thi các hành động trên dòng dữ liệu hoặc sự kiện được đưa ra bởi Observable
//
//        //get all places from database room
//        val getAllPlace = TravelWeatherDB.getInstance(this@BottomNavigation).placeDataDao()
//
//        // Probably, you already know that all UI code is done on Android Main thread.
//        // RxJava is java library and it does not know about Android Main thread. That is the reason why we use RxAndroid.
//        // RxAndroid gives us the possibility to choose Android Main thread as the thread where our code will be executed.
//        // Obviously, our Observer should operate on Android Main thread.
//        compositeDisposable.add(getAllPlace.getAll()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ //excute event
//                    val adapter = ViewPagerAdapter(supportFragmentManager,it)
//                    new_view_pager.adapter = adapter
//                }, {
//                    Log.e(TAG, "" + it.message)
//                }))
//    }
//    inner class insertAllPlace() : AsyncTask<ArrayList<PlaceEntity>, Void, Void>() {
//        override fun doInBackground(vararg params : ArrayList<PlaceEntity>): Void? {
//            TravelWeatherDB.getInstance(this@BottomNavigation).placeDataDao().insertAllPlace(params[0])
//            getDataFromLocal()
//            return null
//        }
//    }
}
