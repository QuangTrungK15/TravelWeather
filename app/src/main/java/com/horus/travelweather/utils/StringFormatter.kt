package com.horus.travelweather.utils

import android.util.Log
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object StringFormatter {

    /*val unitPercentage = "%"
    val unitDegrees = "\u00b0"
    val unitsMetresPerSecond = "m/s"*/
    val unitDegreesCelsius = "\u2103"


    fun convertToValueWithUnit(precision: Int, unitSymbol: String, value: Double?): String{
        return getPrecision(precision).format(value) + unitSymbol
    }

    private fun getPrecision(precision: Int) : String{
        return "%." + precision + "f"
    }

    fun convertTimestampToDayAndHourFormat(timestamp: Long): String{
        val DAY_HOUR_MINUTE = "dd/MM/yyyy, HH:mm"
        val formatter = SimpleDateFormat (DAY_HOUR_MINUTE, Locale.ENGLISH)
        //formatter.timeZone = TimeZone.getTimeZone("UTC")
        val dateFormat = formatter.format(Date(timestamp))
        return dateFormat


        /*var stamp = timestamp
        var date = Date(timestamp)
        stamp = date.time

        return  stamp.toString()
*/
    }
}