package com.horus.travelweather.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.R
import com.horus.travelweather.database.PlaceEntity
import kotlinx.android.synthetic.main.locaiton_item.view.*

class LocationAdapter(private val listLocation : List<PlaceEntity>, private val onItemClick : (String)-> Unit )
    : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
    //Đầu vào là 1 danh sách và 1 cái click (nếu có, click vào nút btn_delete để xóa địa điểm của mình đã thêm)

    //assigning layout for a recyclerview element.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.locaiton_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listLocation.size
    }

    //assigning date from listLocation to ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listLocation[position])
    }

    //This class controls views better, avoiding findViewByID too time
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(location : PlaceEntity) {
            //Lập trình bất đồng bộ
            //Set cho btn_delete trên recycleviewer 1 lắng nghe (nhận id bất kỳ)
            itemView.btn_delete_location.setOnClickListener { onItemClick(location.id) }
            itemView.txt_chose_location.text = location.name
        }
    }
}