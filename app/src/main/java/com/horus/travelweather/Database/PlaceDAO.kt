package com.horus.travelweather.Database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query


@Dao
interface PlaceDAO {
    @Query("SELECT * from placeData")
    fun getAll() : List<PlaceData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(placeData : PlaceData)

    @Query("DELETE from placeData")
    fun deleteAll()

}