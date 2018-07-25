package com.horus.travelweather.service

import com.horus.travelweather.model.DailyWeatherDetailResponse
import com.horus.travelweather.model.WeatherDetailsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("forecast")
    fun getDailyWeatherDetails(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<DailyWeatherDetailResponse>

    @GET("forecast")
    fun getDailyWeatherCoordinates(@Query("lat") latitude : Double,
                                   @Query("lon") longitude : Double,
                                   @Query("APPID") keyAPI : String): Observable<DailyWeatherDetailResponse>

    @GET("weather")
    fun getWeatherDetailsOneLocation(@Query("q") cityName : String,
                          @Query("APPID") keyAPI : String): Observable<WeatherDetailsResponse>


    @GET("weather")
    fun getWeatherDetailsCoordinates(@Query("lat") latitude : Double,
                                     @Query("lon") longitude : Double,
                                     @Query("APPID") keyAPI : String): Observable<WeatherDetailsResponse>





}