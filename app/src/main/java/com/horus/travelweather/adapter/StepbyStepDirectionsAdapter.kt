package com.horus.travelweather.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.horus.travelweather.R
import com.horus.travelweather.activity.DirectionsActivity
import com.horus.travelweather.model.DirectionsStepDbO
import kotlinx.android.synthetic.main.stepbystep_directions.view.*

class StepbyStepDirectionsAdapter (private var listDirectionsStep : List<DirectionsStepDbO>, private val onItemClick : (String)-> Unit )
    : RecyclerView.Adapter<StepbyStepDirectionsAdapter.ViewHolder>() {

    //Đầu vào là 1 danh sách và 1 cái click (nếu có, click vào nút btn_delete để xóa địa điểm của mình đã thêm)

    private val TAG = DirectionsActivity::class.java.simpleName

    //assigning layout for a recyclerview element.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stepbystep_directions, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listDirectionsStep.size
    }

    //assigning date from listTransportation to ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listDirectionsStep[position])
    }

    //This class controls views better, avoiding findViewByID too time
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //Show info of walking/driving directions
        private val show_stepbystep = itemView.findViewById<View>(R.id.show_stepbystep) as LinearLayout

        private val imgView_direction = itemView.findViewById<View>(R.id.imgView_direction) as AppCompatImageView
        private val tv_instructions = itemView.findViewById<View>(R.id.tv_instructions) as TextView
        private val tv_attention = itemView.findViewById<View>(R.id.tv_attention) as TextView
        private val imgv_attention_icon = itemView.findViewById<View>(R.id.imgv_attention_icon) as ImageView
        private val tv_showtime = itemView.findViewById<View>(R.id.tv_showtime) as TextView

        //Show info of transit directions
        private val show_transitsteps = itemView.findViewById<View>(R.id.show_transitsteps) as LinearLayout

        private val tv_departure_name = itemView.findViewById<View>(R.id.tv_departure_name) as TextView
        private val tv_busname = itemView.findViewById<View>(R.id.tv_busname) as TextView
        private val tv_headsign = itemView.findViewById<View>(R.id.tv_headsign) as TextView
        private val tv_transit_distance = itemView.findViewById<View>(R.id.tv_transit_distance) as TextView
        private val tv_numstops = itemView.findViewById<View>(R.id.tv_numstops) as TextView
        private val tv_arrival_name = itemView.findViewById<View>(R.id.tv_arrival_name) as TextView

        fun bind(step: DirectionsStepDbO) {

            //Lập trình bất đồng bộ
            //Set cho imgView_transportation trên recycleviewer 1 lắng nghe (nhận id bất kỳ để nhận dạng loại ptien)
            itemView.tv_instructions.setOnClickListener { onItemClick(step.id.toString()) }

            if(step.direction == "Head" || step.direction == "Straight"){
                imgView_direction.setImageResource(R.drawable.ic24_head)
            } else if(step.direction == "turn-left"){
                imgView_direction.setImageResource(R.drawable.ic24_turnleft)
            } else if(step.direction == "turn-right"){
                imgView_direction.setImageResource(R.drawable.ic24_turnright)
            } else if(step.direction == "turn-slight-right"){ //chếch sang phải
                imgView_direction.setImageResource(R.drawable.ic24_turnslightright)
            } else if(step.direction == "turn-slight-left"){
                imgView_direction.setImageResource(R.drawable.ic24_turnslightleft)
            }else if(step.direction == "turn-sharp-right"){ // ngoặc phải
                imgView_direction.setImageResource(R.drawable.ic24_turnsharpright)
            } else if(step.direction == "turn-sharp-left"){
                imgView_direction.setImageResource(R.drawable.ic24_turnsharpleft)
            } else if(step.direction == "ferry"){
                imgView_direction.setImageResource(R.drawable.ic24_ferry)
            } else if(step.direction == "ferry-train"){
                imgView_direction.setImageResource(R.drawable.ic24_ferry)
            } else if(step.direction == "ramp-right"){ //tại nút giao thông
                imgView_direction.setImageResource(R.drawable.ic24_rampleft)
            } else if(step.direction == "ramp-left"){
                imgView_direction.setImageResource(R.drawable.ic24_rampleft)
            } else if(step.direction == "fork-right"){ //tại nút giao thông
                imgView_direction.setImageResource(R.drawable.ic24_rampleft)
            } else if(step.direction == "fork-left"){
                imgView_direction.setImageResource(R.drawable.ic24_rampleft)
            } else if(step.direction == "uturn-right"){
                imgView_direction.setImageResource(R.drawable.ic24_uturnright)
            } else if(step.direction == "uturn-left"){
                imgView_direction.setImageResource(R.drawable.ic24_uturnleft)
            } else if(step.direction == "merge"){
                imgView_direction.setImageResource(R.drawable.ic24_merge)
            } else if(step.direction == "roundabout-right"){
                imgView_direction.setImageResource(R.drawable.ic24_roundabout)
            } else if(step.direction == "roundabout-left"){
                imgView_direction.setImageResource(R.drawable.ic24_roundabout)
            } else if(step.direction == "keep-right"){
                imgView_direction.setImageResource(R.drawable.ic24_keepright)
            } else if(step.direction == "keep-left"){
                imgView_direction.setImageResource(R.drawable.ic24_keepleft)
            } else if(step.direction == "walking"){
                imgView_direction.setImageResource(R.drawable.ic24_walking)
            } else if(step.direction == "transit"){
                imgView_direction.setImageResource(R.drawable.ic24_bus)
            }else{
                imgView_direction.setImageResource(R.drawable.ic24_head)
            }

            if(step.direction != "transit")
            {
                show_stepbystep.visibility = View.VISIBLE
                show_transitsteps.visibility = View.GONE

                tv_instructions.text = step.instructions
                if(!step.attention.isEmpty()){
                    //imgv_attention_icon.setImageResource(R.drawable.ic_attention24)
                    tv_attention.text = step.attention
                }else {
                    imgv_attention_icon.visibility = View.GONE
                    tv_attention.visibility = View.GONE
                }
                tv_showtime.text = step.distance.plus(" ("+step.duration+')')
            } else {
                show_stepbystep.visibility = View.GONE
                show_transitsteps.visibility = View.VISIBLE

                val temp = "Đi qua "
                tv_departure_name.text = step.transit.departure_stop
                tv_busname.text = step.transit.line_busname
                tv_headsign.text = step.transit.headsign
                tv_transit_distance.text = step.distance
                tv_numstops.text = temp.plus(step.transit.num_stops + " trạm dừng (trong "+ step.duration + ")")
                tv_arrival_name.text = step.transit.arrival_stop
            }


        }
    }
}