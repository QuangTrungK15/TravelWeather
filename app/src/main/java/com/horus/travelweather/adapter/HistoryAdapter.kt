package com.horus.travelweather.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.horus.travelweather.R
import com.horus.travelweather.model.HistoryDbO
import kotlinx.android.synthetic.main.eachhistory_layout.view.*
import java.util.*


/**
 * Created by onlyo on 12/26/2018.
 */
class HistoryAdapter(options: FirebaseRecyclerOptions<HistoryDbO>, private val onItemClickListener : (Context, TextView, Int)-> Unit)
    : FirebaseRecyclerAdapter<HistoryDbO, HistoryAdapter.HistoryViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.eachhistory_layout, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int, model: HistoryDbO) {
        holder.tempbind(model)
        holder.bind(model)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }



    val historyList = ArrayList<HistoryDbO>()
    var temp_date = "" //to check next historyid if the same date
    var count = 0
    var length_history = 0
    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view){
        fun tempbind(history3: HistoryDbO){
            //val placeList = ArrayList<PlaceEntity>()
            //val item : PlaceEntity? = history.getValue(PlaceEntity::class.java)
            historyList.add(history3)
            length_history ++
            //}

        }

        fun bind(history2: HistoryDbO) {
            Collections.reverse(historyList)
            val historyList9 = ArrayList<HistoryDbO>(length_history)
            var len = length_history
                for (history in historyList) {
                    if(len !=0){
                        historyList9.add(history)
                        len--
                    }
                }
            /*var len9 = length_history
            for (history in historyList) {
                if(len9 !=0){
                    len9 = len9 - 1
                    historyList9.removeAt(len9)
                    historyList9.add(len9,history)
                    len9--
                }
            }*/

            /*historyList9.reversed().forEach { //Log.e("historyid9:", historyList9)
            }*/
            historyList9.reverse()
            historyList9.reversed()
            historyList9.asReversed()
            for (history in historyList9) {
                Log.e("historyid9:", history.name)
            }
            for (history in historyList) {
                    if(length_history != 0 ) {
                        Log.e("historyid:", history.name)
                        Log.e("historyid2:",history.date)
                        itemView.tv_name.text = history.name
                        itemView.tv_address.text = history.address

                        val date = getCurrentDateTime()
                        //val c = GregorianCalendar(1995, 12, 23)
                        val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)

                        // -> s == "16/03/2019"
                        if (history.date == currenttime) {
                            //Log.e("historyid3:",history.date)
                            if (count == 0) {
                                itemView.txt_history_date.text = "Hôm nay"
                            } /*else if(count != 0){
                                itemView.txt_history_date.text = history.date*/
                            else itemView.txt_history_date.visibility = View.GONE
                            count++
                        } else {
                            if (count == 0) {
                                itemView.txt_history_date.text = history.date
                                count++
                            } else if (temp_date == history.date) {
                                itemView.txt_history_date.visibility = View.GONE
                            } else {
                                itemView.txt_history_date.text = history.date
                            }

                        }
                        temp_date = history.date

                        if (history.placeTypes == "airport") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_airport)
                        } else if (history.placeTypes == "spa") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_spa)
                        } else if (history.placeTypes == "atm") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_atm)
                        } else if (history.placeTypes == "hospital") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_hospital)
                        } else if (history.placeTypes == "cafe") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_cafe)
                        } else if (history.placeTypes == "bar") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_bar)
                        } else if (history.placeTypes == "car_wash") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_carwash)
                        } else if (history.placeTypes == "car_repair") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_carwash)
                        } else if (history.placeTypes == "car_rental") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_carwash)
                        } else if (history.placeTypes == "car_dealer") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_carwash)
                        } else if (history.placeTypes == "convenience_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "clothing_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "restaurant") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_restaurant)
                        } else if (history.placeTypes == "florist") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_florist)
                        } else if (history.placeTypes == "store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_grocery_store)
                        } else if (history.placeTypes == "department_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "home_goods_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "laundry") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_laundry_service)
                        } else if (history.placeTypes == "library") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_library)
                        } else if (history.placeTypes == "shopping_mall") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_mall)
                        } else if (history.placeTypes == "movie_rental") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_movies)
                        } else if (history.placeTypes == "movie_theater") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_movies)
                        } else if (history.placeTypes == "parking") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else if (history.placeTypes == "pharmacy") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_pharmacy)
                        } else if (history.placeTypes == "post_office") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_post_office)
                        } else if (history.placeTypes == "moving_company") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_shipping)
                        } else if (history.placeTypes == "taxi_stand") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_taxi)
                        } else if (history.placeTypes == "bicycle_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_bicycle_store)
                        } else if (history.placeTypes == "casino") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_casino)
                        } else if (history.placeTypes == "lawyer") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_lawyer)
                        } else if (history.placeTypes == "transit_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_transit)
                        } else if (history.placeTypes == "train_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_spa)
                        } else if (history.placeTypes == "bus_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_bus)
                        } else if (history.placeTypes == "fire_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_transit)
                        } else if (history.placeTypes == "gas_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_gassation)
                        } else if (history.placeTypes == "subway_station") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_transit)
                        } else if (history.placeTypes == "bakery") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_bakery)
                        } else if (history.placeTypes == "school") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_school)
                        } else if (history.placeTypes == "real_estate_agency") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "museum") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "local_government_office") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "night_club") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_bar)
                        } else if (history.placeTypes == "insurance_agency") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "hindu_temple") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "embassy") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "courthouse") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "city_hall") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "bank") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "accounting") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_account_balance)
                        } else if (history.placeTypes == "travel_agency") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "supermarket") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "zoo") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_spa)
                        } else if (history.placeTypes == "veterinary_care") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "synagogue") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "storage") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "stadium") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else if (history.placeTypes == "pack") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else if (history.placeTypes == "shoe_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "rv_park") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else if (history.placeTypes == "pet_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "mosque") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "meal_takeaway") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_restaurant)
                        } else if (history.placeTypes == "meal_delivery") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_restaurant)
                        } else if (history.placeTypes == "lodging") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "liquor_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "jewelry_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "hardware_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "hair_care") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_spa)
                        } else if (history.placeTypes == "gym") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "furniture_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "funeral_home") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "electronics_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "church") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_city)
                        } else if (history.placeTypes == "cemetery") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_florist)
                        } else if (history.placeTypes == "campground") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else if (history.placeTypes == "bowling_alley") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "book_store") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "beauty_salon") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_spa)
                        } else if (history.placeTypes == "art_gallery") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_convenience_store)
                        } else if (history.placeTypes == "amusement_park") {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_packing)
                        } else {
                            itemView.imgv_placetype.setImageResource(R.drawable.ic24_place)
                        }

                        itemView.tv_name.setOnClickListener {
                            /*view: View, motionEvent: MotionEvent ->*/
                            //when (motionEvent.action and MotionEvent.ACTION_MASK) {
                            //   MotionEvent.ACTION_HOVER_EXIT -> {
                            onItemClickListener(itemView.context, itemView.tv_name, adapterPosition)
                            //  }
                            //}
                            // true
                        }
                        length_history--
                    }
                /*itemView.tv_name.setOnClickListener {
                    onItemClick(history)
                }*/
            }

        }

    }


}