package com.horus.travelweather.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        private val imgView_direction = itemView.findViewById<View>(R.id.imgView_direction) as AppCompatImageView
        private val tv_instructions = itemView.findViewById<View>(R.id.tv_instructions) as TextView
        private val tv_attention = itemView.findViewById<View>(R.id.tv_attention) as TextView

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
            } else{
                imgView_direction.setImageResource(R.drawable.ic24_head)
            }

            tv_instructions.text = step.instructions
            tv_attention.text = step.attention

        }
    }
}