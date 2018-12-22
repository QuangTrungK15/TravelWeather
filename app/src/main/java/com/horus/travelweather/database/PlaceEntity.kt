package com.horus.travelweather.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "placeData")
data class PlaceEntity(@PrimaryKey var id : String,
                       @ColumnInfo(name = "name") var name : String,
                       @ColumnInfo(name = "lat") var latitude: Double,
                       @ColumnInfo(name = "lon") var longitude: Double){
    constructor():this("","",0.0,0.0)
}