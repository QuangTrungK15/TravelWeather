package com.horus.travelweather.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R
import com.horus.travelweather.activity.AddLocationActivity
import kotlinx.android.synthetic.main.fragment_add_location.view.*

class AddLocationFragment: Fragment() {

    //Creating view, return view is a view (xml) as fragment
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view  = inflater.inflate(R.layout.fragment_add_location, container, false)

        view.btn_add_location.setOnClickListener {
            val intent = Intent(this.context, AddLocationActivity::class.java)
            //after successlly addlocation -> refer to HomeAcrivity (code: 1234)
            startActivityForResult(intent, 1234)
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234){ //way to start Kotlin activity
            if (resultCode == Activity.RESULT_OK){
                val intent = Intent(context, BottomNavigation::class.java) //this activity will be this fragment's father
                //update fragments of HomeActivity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //Tao bỏ FLAG_ACTIVITY_CLEAR_TASK để tránh lỗi thêm vị trí xong về lại homeactivity đc 1 lúc bị out ra nha
                startActivity(intent)
            }
        }
    }
}