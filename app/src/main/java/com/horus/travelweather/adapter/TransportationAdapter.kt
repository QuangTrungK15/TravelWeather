package com.horus.travelweather.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.horus.travelweather.R
import com.horus.travelweather.activity.DirectionsFragment
import com.horus.travelweather.model.TransportationDbO
import kotlinx.android.synthetic.main.transportation_list.view.*

class TransportationAdapter (private var listTransportation : List<TransportationDbO>, private val onItemClick : (String)-> Unit )
    : RecyclerView.Adapter<TransportationAdapter.ViewHolder>() {
    //Đầu vào là 1 danh sách và 1 cái click (nếu có, click vào nút btn_delete để xóa địa điểm của mình đã thêm)

    private val TAG = DirectionsFragment::class.java.simpleName

    //assigning layout for a recyclerview element.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transportation_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listTransportation.size
    }

    //assigning date from listTransportation to ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listTransportation[position])
    }

    //This class controls views better, avoiding findViewByID too time
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgView_transportation = itemView.findViewById<View>(R.id.imgView_transportation) as AppCompatImageView
        private val tv_duration = itemView.findViewById<View>(R.id.tv_duration) as TextView
        fun bind(transkind : TransportationDbO) {

            //Lập trình bất đồng bộ
            //Set cho imgView_transportation trên recycleviewer 1 lắng nghe (nhận id bất kỳ để nhận dạng loại ptien)
            itemView.tv_duration.setOnClickListener { onItemClick(transkind.id) }

            if(transkind.id == "driving"){
                imgView_transportation.setImageResource(R.drawable.ic24_car)
            } else if(transkind.id == "walking"){
                imgView_transportation.setImageResource(R.drawable.ic24_walking)
            } else if(transkind.id == "transit"){
                imgView_transportation.setImageResource(R.drawable.ic24_bus)
            } else if(transkind.id == "bicycling"){
                imgView_transportation.setImageResource(R.drawable.ic24_walking)
            } else if(transkind.id == "5"){
                imgView_transportation.setImageResource(R.drawable.originpoint_icon24)
            }

            tv_duration.text = transkind.duration
        }
    }
}