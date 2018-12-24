package com.horus.travelweather.model

data class TransitDbO(val departure_stop: String, val departure_time: String, val arrival_stop: String, val arrival_time: String,
                      val line_busname: String, val headsign: String, val num_stops: String)