package com.horus.travelweather.model

import java.io.Serializable


data class TempPlaceDbO (var placeId : String=""
                     , var name : String= ""
                     , var numofSearch : String=""
                         , var numofVisit : String=""
                         , var isACity : String="") : Serializable