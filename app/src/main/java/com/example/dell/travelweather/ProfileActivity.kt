package com.example.dell.travelweather

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.example.dell.travelweather.Common.Common
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {


    lateinit var txtUserName : TextView
    lateinit var txtPhoneNumber : TextView
    lateinit var txtEmailUser : TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        txtUserName = findViewById(R.id.txt_user_profile_name)
        txtPhoneNumber = findViewById(R.id.txt_phone_number)
        txtEmailUser = findViewById(R.id.txt_email_user)




        txtUserName.text =  Common.currentUser.name
        txtPhoneNumber.text = Common.currentUser.phone
        txtEmailUser.text = Common.currentUser.email








    }
}