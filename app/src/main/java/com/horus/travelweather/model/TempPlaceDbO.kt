package com.horus.travelweather.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

data class TempPlaceDbO (@ColumnInfo(name = "id") var id : String
                         ,@ColumnInfo(name = "isacity") var isacity : Boolean
                         ,@ColumnInfo(name = "latitude") var latitude : Double
                         ,@ColumnInfo(name = "longitude") var longitude : Double
                     ,@ColumnInfo(name = "name") var name : String
                     ,@ColumnInfo(name = "numofsearch") var numofsearch : Int
                         ,@ColumnInfo(name = "numofvisit") var numofvisit : Int
                         ,@ColumnInfo(name = "uri") var uri : String){
    constructor():this("",false,0.0,0.0,"",0,0,"")
}