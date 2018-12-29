package com.horus.travelweather.common

import com.horus.travelweather.model.UserDbO

class TWConstant {

    companion object {
        var currentUser : UserDbO = UserDbO("","","","")
        const val BASE_API_LAYER = "http://api.openweathermap.org/data/2.5/"
        const val ACCESS_API_KEY = "ba29c1fc66a2ee3e436c1636937fafad"
        const val BASE_URL_UPLOAD  = "http://openweathermap.org/img/w/"
        const val REMOVE_PLACE = "Xóa"
        const val YOURLOCATION1 = "Vị trí hiện tại tại điểm xuất phát"
        const val YOURLOCATION2 = "Vị trí hiện tại tại điểm đến"
        const val BASE_URI_PHOTO = "https://i.ibb.co/V9VLdKr/Rectangle-Copy-11-1.png"
    }
}