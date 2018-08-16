package com.horus.travelweather.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable


@Dao
interface PlaceDAO {

    @Query("SELECT * from placeData")
    fun getAll() : Flowable<List<PlaceData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(placeData : PlaceData)

    @Query("DELETE from placeData")
    fun deleteAll()

    @Query("DELETE FROM placeData WHERE id = :placeID")
    fun deleteByPlaceId(placeID : String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPlace(placeList : ArrayList<PlaceData>)

}