package com.horus.travelweather.model

import com.google.gson.annotations.SerializedName

data class WeatherDetailsResponse(val weather : List<WeatherItem> = emptyList(),
                                  @SerializedName("main")
                                  val temperature : TemperatureItem =
                                          TemperatureItem(0.0,0.0,0.0,0.0),
                                  @SerializedName("name")
                                  val nameCity : String="",
                                  @SerializedName("dt")
                                  val dateTime : Long=0,
                                  val wind : WindItem = WindItem(0.0,""),
                                  val clouds : CloudsItem = CloudsItem(0.0))