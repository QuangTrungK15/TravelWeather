package com.horus.travelweather.model

import java.io.Serializable

data class HistoryDbO(var historyId : String=""
                      , var name : String= ""
                      , var address : String=""
                      , var placeTypes: String="") : Serializable