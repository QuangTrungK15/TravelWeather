package com.example.dell.travelweather.service

import com.example.dell.travelweather.model.AndroidVersion
import com.example.dell.travelweather.model.WeatherDetails
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {



    //URL specified in the annotation
//    @GET("android/jsonarray/")
//    fun getAndroidVersion(): Observable<List<AndroidVersion>>

    @GET("forecast")
    fun getWeatherDetails(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<List<WeatherDetails>>



    @GET("weather")
    fun getWeatherDetailsOneLocation(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<WeatherDetails>




}