package com.horus.travelweather.adapter

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.horus.travelweather.R
import com.horus.travelweather.model.PlaceDbO
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.locaiton_item.view.*
import kotlinx.android.synthetic.main.my_places_layout.view.*

class FavouritePlaceAdapter( options: FirebaseRecyclerOptions<PlaceDbO>,private val onItemPopupClick : (Context,TextView,Int)-> Unit)
    : FirebaseRecyclerAdapter<PlaceDbO, FavouritePlaceAdapter.PlaceViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_places_layout, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int, model: PlaceDbO) {
        holder.bind(model)
    }

    inner class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view){
        fun bind(place : PlaceDbO) {
            Picasso.with(itemView.context).load(place.uri).into(itemView.img_my_place)
            itemView.my_place_name.text = place.name
            itemView.txt_option_menu.setOnClickListener {
                onItemPopupClick(itemView.context,itemView.txt_option_menu,adapterPosition)
            }
        }

    }


}