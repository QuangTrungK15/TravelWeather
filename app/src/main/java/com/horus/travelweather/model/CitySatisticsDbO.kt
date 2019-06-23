package com.horus.travelweather.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.text.DateFormat
import java.util.*

data class CitySatisticsDbO (@ColumnInfo(name = "name") var name : String
                             , @ColumnInfo(name = "numofsearch") var numofsearch : Int){
    constructor():this("",0)
}