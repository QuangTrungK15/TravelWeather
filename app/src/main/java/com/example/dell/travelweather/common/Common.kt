package com.example.dell.travelweather.common

import com.example.dell.travelweather.model.UserDbO

class Common {

    companion object {
        var currentUser : UserDbO = UserDbO("","","")
        const val BASE_API_LAYER = "http://api.openweathermap.org/data/2.5/"
        const val ACCESS_API_KEY = "ba29c1fc66a2ee3e436c1636937fafad"
        const val BASE_URL_UPLOAD  = "http://openweathermap.org/img/w/"
        //val BASE_URL: String = "https://api.learn2crack.com/"
    }

}