package com.horus.travelweather.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.model.DailyWeatherDetailResponse
import com.horus.travelweather.model.WeatherDetailsResponse

class DailyWeatherAdapter (private val dailyWeather : List<WeatherDetailsResponse>)
    : RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dailyWeather[position],position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_daily_weather, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        dailyWeather.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(daily : WeatherDetailsResponse, position: Int ) {


        }


    }
}