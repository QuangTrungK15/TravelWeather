package com.horus.travelweather.model
import com.google.android.gms.maps.model.LatLng

data class DirectionsStepDbO (val id: Int, val direction: String, val instructions: String, val attention: String,
                              val duration: String, val distance: String, val latLng: LatLng)