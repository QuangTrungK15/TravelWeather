package com.example.dell.travelweather.service

import com.example.dell.travelweather.model.WeatherDetailsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("forecast")
    fun getWeatherDetails(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<List<WeatherDetailsResponse>>

    @GET("weather")
    fun getWeatherDetailsOneLocation(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<WeatherDetailsResponse>




}