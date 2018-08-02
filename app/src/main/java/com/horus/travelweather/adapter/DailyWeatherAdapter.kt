package com.horus.travelweather.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.WeatherDetailsResponse
import com.horus.travelweather.utils.StringFormatter
import com.horus.travelweather.utils.StringFormatter.convertKelvinToCelsius
import com.horus.travelweather.utils.StringFormatter.convertTimestampHourFormat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.daily_weather.view.*
import kotlinx.android.synthetic.main.fragment_weather_details.*

class DailyWeatherAdapter (private val dailyWeather : List<WeatherDetailsResponse>)
    : RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dailyWeather[position],position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_weather, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return dailyWeather.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(daily : WeatherDetailsResponse, position: Int ) {
            itemView.txt_hourly.text = convertTimestampHourFormat(daily.dateTime)
            Picasso.with(itemView.context).load(TWConstant.BASE_URL_UPLOAD + daily.weather[0].icon + ".png").into(itemView.img_daily_weather_icon)
            itemView.txt_daily_humidity.text = StringFormatter.convertToValueWithUnit(0, StringFormatter.unitPercentage, daily.temperature.humidity)
            itemView.txt_daily_temperature.text = StringFormatter.convertToValueWithUnit(0, StringFormatter.unitDegreesCelsius, convertKelvinToCelsius(daily.temperature.temp))
        }
    }
}