package com.horus.travelweather.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        private val imgView_direction = itemView.findViewById<View>(R.id.imgView_direction) as ImageView
        private val tv_instructions = itemView.findViewById<View>(R.id.tv_instructions) as TextView
        private val tv_attention = itemView.findViewById<View>(R.id.tv_attention) as TextView

        fun bind(step: DirectionsStepDbO) {

            //Lập trình bất đồng bộ
            //Set cho imgView_transportation trên recycleviewer 1 lắng nghe (nhận id bất kỳ để nhận dạng loại ptien)
            itemView.tv_instructions.setOnClickListener { onItemClick(step.id) }

            if(step.id == "head"){
                imgView_direction.setBackgroundResource(R.drawable.ic_turnleft24)
            } else if(step.id == "turnleft"){
                imgView_direction.setBackgroundResource(R.drawable.ic_turnleft24)
            } else if(step.id == "turnright"){
                imgView_direction.setBackgroundResource(R.drawable.ic_turnleft24)
            } else if(step.id == "firstroundabout"){
                imgView_direction.setBackgroundResource(R.drawable.ic_turnleft24)
            } else if(step.id == "5"){
                imgView_direction.setBackgroundResource(R.drawable.ic_turnleft24)
            }

            tv_instructions.text = step.instructions
            tv_attention.text = step.attention

        }
    }
}