package com.horus.travelweather.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_profile.view.*

class NewProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_new_profile, container, false)
        view.txt_username.text =  TWConstant.currentUser.name
        Picasso.with(this.context).load(TWConstant.currentUser.urlPhoto).into(view.img_url_photo)
        view.edit_profile.setOnClickListener {
            val intent = Intent(this.context, ProfileActivity::class.java) //this activity will be this fragment's father
            startActivity(intent)
        }
        view.btn_logout.setOnClickListener {
            getActivity()!!.finish();
        }
        return view
    }

    companion object {
        fun newInstance(): NewProfileFragment = NewProfileFragment()
    }

}