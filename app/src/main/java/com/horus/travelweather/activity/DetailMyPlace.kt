package com.horus.travelweather.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import com.google.android.gms.location.places.PlacePhotoMetadataResponse
import com.google.android.gms.location.places.PlacePhotoResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.tasks.OnCompleteListener
import com.horus.travelweather.R
import com.horus.travelweather.adapter.SlidingImageAdapter
import com.horus.travelweather.model.PlaceDbO
import kotlinx.android.synthetic.main.activity_detail_my_place.*

class DetailMyPlace : AppCompatActivity() {


    private var arraySliding : ArrayList<Bitmap> = arrayListOf()
    private lateinit var adapterSliding : SlidingImageAdapter
//    private lateinit var adapterSliding : ArrayAdapter<String>
    private val TAG = DetailMyPlace::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_my_place)
        val place = intent.getSerializableExtra("MyPlace") as PlaceDbO
        Log.e(TAG,"ABC : "+place.name)
        getPhoto(place.placeId)

        sliding_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                pageIndicatorView.setSelection(position);
            }

        })

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
            Log.e(TAG,"ABC : "+photoMetadataBuffer.count)
            // Get the first photo in the list.
            for(i in 0..photoMetadataBuffer.count-1)
            {
                val photoMetadata = photoMetadataBuffer.get(i)
                // Get a full-size bitmap for the photo.
                val photoResponse = mGeoDataClient.getPhoto(photoMetadata)
                photoResponse.addOnCompleteListener(OnCompleteListener<PlacePhotoResponse> { task ->
                    val photo = task.result
                    val bitmap = photo.bitmap
                    Log.e(TAG,""+bitmap)
                    arraySliding.add(bitmap)
                    adapterSliding.notifyDataSetChanged()
                })
            }
            adapterSliding = SlidingImageAdapter(this,arraySliding)
            sliding_view_pager.adapter = adapterSliding
            pageIndicatorView.count = arraySliding.size
            pageIndicatorView.setViewPager(sliding_view_pager)
//            titles.setViewPager(sliding_view_pager)
        })
    }


}