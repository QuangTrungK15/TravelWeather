package com.horus.travelweather.Database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity


@Entity(tableName = "placeData")
data class PlaceData(@ColumnInfo(name = "name") var humidity: String,
                     @ColumnInfo(name = "lat") var latitude: Double,
                     @ColumnInfo(name = "lon") var longitude: Double)