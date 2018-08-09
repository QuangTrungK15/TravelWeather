package com.horus.travelweather.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.places.PlacePhotoMetadataResponse
import com.google.android.gms.location.places.PlacePhotoResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.tasks.OnCompleteListener
import com.horus.travelweather.R
import com.horus.travelweather.model.PlaceDbO

class DetailMyPlace : AppCompatActivity() {

    private val TAG = DetailMyPlace::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_my_place)
        val place = intent.getSerializableExtra("MyPlace") as PlaceDbO
        getPhoto(place.placeId)
    }

    // Request photos and metadata for the specified place.
    private fun getPhoto(placeId: String) {
        //val placeId = "ChIJa147K9HX3IAR-lwiGIQv9i4"
        val mGeoDataClient = Places.getGeoDataClient(this)
        val photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId)
        photoMetadataResponse.addOnCompleteListener(OnCompleteListener<PlacePhotoMetadataResponse> { task ->
            // Get the list of photos.
            val photos = task.result
            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            val photoMetadataBuffer = photos.photoMetadata
            // Get the first photo in the list.
//            for(i in 0..photoMetadataBuffer.count )
//            {
//                val photoMetadata = photoMetadataBuffer.get(i)
//                // Get a full-size bitmap for the photo.
//                val photoResponse = mGeoDataClient.getPhoto(photoMetadata)
//                photoResponse.addOnCompleteListener(OnCompleteListener<PlacePhotoResponse> { task ->
//                    val photo = task.result
//                    val bitmap = photo.bitmap
//                    Log.e(TAG,""+bitmap)
//                })
//            }

        })
    }


}