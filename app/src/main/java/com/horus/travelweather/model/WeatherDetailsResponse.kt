package com.horus.travelweather.model

import com.google.gson.annotations.SerializedName

class WeatherDetailsResponse {

    lateinit var weather : List<WeatherItem>

    @SerializedName("main")
    lateinit var temperature : TemperatureItem

    @SerializedName("name")
    lateinit var nameCity : String

    @SerializedName("dt")
    var dateTime : Long = 0
}