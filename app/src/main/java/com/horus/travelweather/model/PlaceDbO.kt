package com.horus.travelweather.model

import java.io.Serializable


data class PlaceDbO (var placeId : String=""
                     , var name : String= ""
                     , var address: String=""
                     , var uri : String="") : Serializable