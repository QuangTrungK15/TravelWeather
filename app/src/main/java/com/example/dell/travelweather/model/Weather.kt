package com.example.dell.travelweather.model

import com.google.gson.annotations.SerializedName

class Weather{


     lateinit var id : String
     @SerializedName("main")
     lateinit var nameWeather : String
     lateinit var icon : String


}