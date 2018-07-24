package com.horus.travelweather.model

import com.google.gson.annotations.SerializedName

data class DailyWeatherDetailResponse(@SerializedName("cnt")val sizeList : Int,
                                      val list : List<WeatherDetailsResponse>)