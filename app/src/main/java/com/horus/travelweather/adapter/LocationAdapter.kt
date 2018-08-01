package com.horus.travelweather.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.R
import com.horus.travelweather.database.PlaceData
import com.horus.travelweather.model.LocationDbO
import kotlinx.android.synthetic.main.locaiton_item.view.*

class LocationAdapter(private val listLocation : List<PlaceData>, private val onItemClick : (Int)-> Unit )
    : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.locaiton_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listLocation.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listLocation[position],position)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(location : PlaceData, position: Int ) {
            itemView.btn_delete_location.setOnClickListener { onItemClick(location.id) }
            itemView.txt_chose_location.text = location.name
        }


    }

}