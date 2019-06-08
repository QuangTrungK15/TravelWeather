package com.horus.travelweather.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.text.DateFormat
import java.util.*

data class TempFavPlaceDbO (@ColumnInfo(name = "id") var id : String
                            , @ColumnInfo(name = "name") var name : String
                        ,@ColumnInfo(name = "address") var address : String
                            ,@ColumnInfo(name = "uri") var uri : String
                            , @ColumnInfo(name = "numofsearch") var numofsearch : Int
                            , @ColumnInfo(name = "numofvisit") var numofvisit : Int
                            , @ColumnInfo(name = "numofask") var numofask : Int
                            ,@ColumnInfo(name = "numsearch_after_ask") var numsearch_after_ask : Int
                            , @ColumnInfo(name = "askdate") var askdate : String){
    constructor():this("","","","",0,0,0, 0,"")
}