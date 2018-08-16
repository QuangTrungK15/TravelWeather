package com.horus.travelweather.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.R
import com.horus.travelweather.adapter.ViewPagerAdapter
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.database.PlaceDatabase
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import com.google.firebase.database.DataSnapshot




class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = MainActivity::class.java.simpleName
    private val compositeDisposable = CompositeDisposable()
    private val menu : MutableList<PlaceData> = mutableListOf()
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
        fab.setOnClickListener { view ->
        }
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        val rxPermissions = RxPermissions(this)
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted ->
                    if (granted) {
                        place_list.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                Log.e(TAG,"Error : "+p0.message)
                            }
                            override fun onDataChange(dataSnapshot : DataSnapshot) {
                                val placeList = ArrayList<PlaceData>()
                                // Result will be holded Here
                                for (dsp in dataSnapshot.getChildren()) {
                                    //add result into array list
                                    val item : PlaceData? = dsp.getValue(PlaceData::class.java)
                                    if (item != null) {
                                        placeList.add(item)
                                    }
                                }
                                Log.e(TAG,"Size : "+placeList.size)
                                insertAllPlace().execute(placeList)
                                //Bind fragments on viewpager
                            }
                        })
                    } else {
                        // Oups permission denied
                        Log.e(TAG, "Access fail")
                    }
                }
        val editName = headerView.findViewById<TextView>(R.id.txtName)
        editName.text = TWConstant.currentUser.name
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_profile -> {
                // Handle the profile action
                enterProfileView()
            }
            R.id.nav_places -> {
                enterMyPlaces()
            }
            R.id.exit -> {
                enterLoginPage()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getDataFromLocal()
    {
        val getAllPlace = PlaceDatabase.getInstance(this@HomeActivity).placeDataDao()
        compositeDisposable.add(getAllPlace.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val adapter = ViewPagerAdapter(getSupportFragmentManager(),it)
                    view_pager.adapter = adapter
                }, {
                    Log.e(TAG, "" + it.message)
                }))
    }



    private fun enterMyPlaces() {
        val intent = Intent(this@HomeActivity, FavouritePlaceActivity::class.java)
        startActivity(intent)
    }
    private fun enterProfileView() {
        val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
        startActivity(intent)
    }
    private fun enterLoginPage() {
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    inner class insertAllPlace() : AsyncTask<ArrayList<PlaceData>, Void, Void>() {
        override fun doInBackground(vararg params : ArrayList<PlaceData>): Void? {
            PlaceDatabase.getInstance(this@HomeActivity).placeDataDao().insertAllPlace(params[0])
            getDataFromLocal()
            return null
        }
    }
}
