package com.horus.travelweather.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = arrayOf(
        PlaceEntity::class,
        ProfileEntity::class), version = 2)
abstract class TravelWeatherDB : RoomDatabase() {

    abstract fun placeDataDao(): PlaceDAO
    abstract fun profileDataDao(): ProfileDAO

    companion object {
        private var INSTANCE: TravelWeatherDB? = null

        @Synchronized
        fun getInstance(context: Context): TravelWeatherDB {
            if (INSTANCE == null) {
                synchronized(TravelWeatherDB::class) {
                    INSTANCE = create(context)
                }
            }
            return INSTANCE as TravelWeatherDB
        }

        fun create(context : Context) : TravelWeatherDB{
            return Room.databaseBuilder(context,
                    TravelWeatherDB::class.java, "Place_horus")
                    .build()
        }
    }
}