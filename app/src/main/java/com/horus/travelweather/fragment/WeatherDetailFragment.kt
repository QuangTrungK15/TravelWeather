package com.horus.travelweather.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.ActivityChooserView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.horus.travelweather.R
import com.horus.travelweather.activity.MainActivity
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.WeatherDetailsResponse
import com.horus.travelweather.repository.Repository
import com.horus.travelweather.service.ApiService
import com.horus.travelweather.utils.StringFormatter.convertTimestampToDayAndHourFormat
import com.horus.travelweather.utils.StringFormatter.convertToValueWithUnit
import com.horus.travelweather.utils.StringFormatter.unitDegreesCelsius
import com.horus.travelweather.utils.StringFormatter.unitPercentage
import com.horus.travelweather.utils.StringFormatter.unitsMetresPerSecond
import com.squareup.picasso.Picasso
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weather_details.*
import android.location.Geocoder
import java.util.*
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.net.LocalSocketAddress
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.horus.travelweather.adapter.DailyWeatherAdapter
import com.horus.travelweather.model.DailyWeatherDetailResponse
import io.reactivex.Single
import ru.solodovnikov.rx2locationmanager.LocationTime
import ru.solodovnikov.rx2locationmanager.RxLocationManager
import java.util.concurrent.TimeUnit


class WeatherDetailFragment : Fragment() {



    private val TAG = MainActivity::class.java.simpleName

    private fun initRecyclerView() {
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        rv_hourly_weather_list.layoutManager = layoutManager
    }

    private fun requestWeatherDetails(lat : Double,long :Double) {

        Repository.createService(ApiService::class.java).getWeatherDetailsCoordinates(lat,long, TWConstant.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            //request thành công
                            processResponseData(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )


        Repository.createService(ApiService::class.java).getDailyWeatherCoordinates(lat,long, TWConstant.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            //request thành công
                            processResponseDataDaily(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )

    }




   /* private fun requestDailytWeatherDetails() {

        Repository.createService(ApiService::class.java).getDailyWeatherDetails("Ha Noi", TWConstant.ACCESS_API_KEY)
                .observeOn(AndroidSchedulers.mainThread()) // Chi dinh du lieu chinh tren mainthread
                .subscribeOn(Schedulers.io())//chi dinh cho request lam viec tren I/O Thread(request to api ,  download a file,...)
                .subscribe(
                        //cú pháp của rxjava trong kotlin
                        { result ->
                            //request thành công
                            processResponseDataDaily(result)
                        },
                        { error ->
                            //request thất bai
                            handlerErrorWeatherDetails(error)
                        }
                )

    }
*/



    private fun handlerErrorWeatherDetails(error: Throwable?) {
        Log.e(TAG,""+error.toString())
    }

    private fun processResponseDataDaily(result: DailyWeatherDetailResponse?) {
        Log.e(TAG,"111111111"+ result!!.list[0].temperature.temp)
        val adapter = DailyWeatherAdapter(result.list)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        rv_hourly_weather_list.layoutManager = layoutManager
        rv_hourly_weather_list.adapter = adapter
    }

    private fun processResponseData(result : WeatherDetailsResponse)
    {
        txt_date_time.text = convertTimestampToDayAndHourFormat(result.dateTime)
        txt_city_name.text = result.nameCity.toString()
        txt_temperature.text = convertToValueWithUnit(0, unitDegreesCelsius, convertKelvinToCelsius(result.temperature.temp))
        txt_temp_max.text = convertToValueWithUnit(0, unitDegreesCelsius,convertKelvinToCelsius(result.temperature.temp_max))
        txt_temp_min.text = convertToValueWithUnit(0, unitDegreesCelsius,convertKelvinToCelsius(result.temperature.temp_min))
        txt_main_weather.text = result.weather[0].nameWeather

        txt_humidity.text = convertToValueWithUnit(0, unitPercentage,result.temperature.humidity)
        txt_wind.text = convertToValueWithUnit(0, unitsMetresPerSecond,result.wind.speed)
        txt_cloud_cover.text = result.clouds.all.toString()
        Picasso.with(this.context).load(TWConstant.BASE_URL_UPLOAD + result.weather[0].icon + ".png").into(img_weather_icon)
    }

    private fun convertKelvinToCelsius(temperatue: Double): Double {
        val temp = (temperatue - 273.15)
        return temp
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requestLocation()
        //initRecyclerView()

        return inflater.inflate(R.layout.fragment_weather_details, container, false)
    }

    private fun requestLocation() {
        val rxLocationManager   =  RxLocationManager(this.context!!);
        rxLocationManager.requestLocation(LocationManager.NETWORK_PROVIDER, LocationTime(15, TimeUnit.SECONDS))
                .subscribe ({
                        val geocoder = Geocoder(this.context, Locale.getDefault())
                        val address = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        //Log.e(TAG,"111111111"+address.get(0).getAddressLine(0))
                        requestWeatherDetails(address.get(0).latitude, address.get(0).longitude)

                }, {
                    Log.e(TAG,"Error" + it.message)
                })

    }


}