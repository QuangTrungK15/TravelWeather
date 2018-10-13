package com.horus.travelweather.service

import com.horus.travelweather.model.DailyWeatherDetailResponse
import com.horus.travelweather.model.WeatherDetailsResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
//we always use RxJava for our data layer
    //Using RxJava error will be returned in onError() method so we can show an appropriate error message to the user.

    //In RxJava2 You can think about Observable as the source of the data and Observer the one that gets the data.
    //Once when an Observer subscribed to the Observable onSubscribe method will be called.
    // Note that we have Disposable as a parameter of onSubscribe method.
    // First, instead of Observer, we can use DisposableObserver that implements Disposable and have dispose() method.
    // So we don’t need onSubscribe()
    // But we use another smarter way: *** CompositeDisposable ***
    // CompositeDisposable, a disposable container that can hold onto multiple other disposables
    // So, each time when we create Disposable we should hold it into CompositeDisposable:
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