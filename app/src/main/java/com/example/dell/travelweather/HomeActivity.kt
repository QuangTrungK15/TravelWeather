package com.example.dell.travelweather

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.dell.travelweather.adapter.ViewPagerAdapter
import com.example.dell.travelweather.common.Common
import com.example.dell.travelweather.model.WeatherDetailsResponse
import com.example.dell.travelweather.repository.Repository
import com.example.dell.travelweather.service.ApiService
import com.example.dell.travelweather.utils.StringFormatter.convertTimestampToDayAndHourFormat
import com.example.dell.travelweather.utils.StringFormatter.convertToValueWithUnit
import com.example.dell.travelweather.utils.StringFormatter.unitDegreesCelsius
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        requestWeatherDetails()

        fab.setOnClickListener { view ->
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)

        //Bind fragments on viewpager
        var titles = ArrayList<String>()
        titles.add("One")
        titles.add("Two")
        titles.add("Three")
        titles.add("Four")
        titles.add("Five")
        titles.add("Six")

        val adapter  = ViewPagerAdapter(getSupportFragmentManager(), titles)
        view_pager.adapter = adapter

        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setupWithViewPager(view_pager)

        var editName = headerView.findViewById<TextView>(R.id.txtName)
        editName.text = Common.currentUser.name

        nav_view.setNavigationItemSelectedListener(this)
    }


    private fun requestWeatherDetails() {
        Repository.createService(ApiService::class.java).getWeatherDetailsOneLocation("Ha Noi",Common.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            //request thành công
                            handleSuccessWeatherDetails(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )
    }

    private fun handlerErrorWeatherDetails(error: Throwable?) {

    }

    private fun handleSuccessWeatherDetails(result: WeatherDetailsResponse?) {
        //Log.e(TAG,"Successfull")
        setupMainWeatherDetailsInfo(result)

    }



    private fun setupMainWeatherDetailsInfo(result: WeatherDetailsResponse?)
    {
        Log.e("TAG", result!!.dateTime.toString())
        txt_date_time.text = convertTimestampToDayAndHourFormat(result!!.dateTime)
        txt_city_name.text = result!!.nameCity.toString()
        val temperatue : Double = convertFahrenheitToCelsius(result.temperature.temp)
        txt_temperature.text = convertToValueWithUnit(0, unitDegreesCelsius, temperatue)
        txt_main_weather.text = result.weather[0].nameWeather


        Picasso.with(getBaseContext()).load(Common.BASE_URL_UPLOAD+result.weather[0].icon+".png").into(img_weather_icon)

    }




    private fun convertFahrenheitToCelsius(temperatue : Double) : Double
    {
        val temp = (temperatue - 273.15)
        return temp
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
                val intent = Intent(this@HomeActivity,ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_places -> {

            }
            R.id.nav_notification -> {

            }
           /* R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }*/
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
