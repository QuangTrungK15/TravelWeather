package com.example.dell.travelweather.model

import com.google.gson.annotations.SerializedName

class WeatherDetails {

    lateinit var weather : List<Weather>

    @SerializedName("main")
    lateinit var temperature : Temperature

    @SerializedName("name")
    lateinit var nameCity : String
}