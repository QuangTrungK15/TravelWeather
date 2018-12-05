package com.horus.travelweather.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface ProfileDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profileEntity : ProfileEntity)

    @Query("SELECT * FROM profileData WHERE uid == :uid")
    fun getProfileInfo(uid : Int): Flowable<ProfileEntity>

    @Query("SELECT * from profileData")
    fun getAllProfileUser() : Flowable<List<ProfileEntity>>
}