package com.horus.travelweather.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


@Database(entities = arrayOf(PlaceData::class), version = 1)
abstract class PlaceDatabase : RoomDatabase() {

    abstract fun placeDataDao(): PlaceDAO

    companion object {
        private var INSTANCE: PlaceDatabase? = null

        @Synchronized
        fun getInstance(context: Context): PlaceDatabase {
            if (INSTANCE == null) {
                synchronized(PlaceDatabase::class) {
                    INSTANCE = create(context)
                }
            }
            return INSTANCE as PlaceDatabase
        }

        fun create(context : Context) : PlaceDatabase{
            return Room.databaseBuilder(context,
                    PlaceDatabase::class.java, "Place_horus")
                    .build()
        }
    }
}