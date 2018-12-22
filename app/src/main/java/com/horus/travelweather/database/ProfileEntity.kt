package com.horus.travelweather.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity(tableName = "profileData")
data class ProfileEntity (@PrimaryKey var uid : String,
                          @ColumnInfo(name = "name") var name : String,
                          @ColumnInfo(name = "email") var email: String,
                          @ColumnInfo(name = "phone") var phoneNumber : String){
    constructor():this("","","","")
}