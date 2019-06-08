package com.horus.travelweather.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.horus.travelweather.BottomNavigation
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

        val actionBar1 = supportActionBar
        if (actionBar1 != null) {
            //actionBar1.setDisplayHomeAsUpEnabled(true)
            actionBar1.title = "Detail Place"
        }

        sliding_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                pageIndicatorView.selection = position
            }

        })

        val fab_directions = this.findViewById<View>(R.id.fab_directions2) as FloatingActionButton
        fab_directions.setOnClickListener {
            //val directionsIntent = Intent(this@DetailMyPlace, DirectionsFragment::class.java)
            //directionsIntent.putExtra("MyLatLng",latLng_toDirection.toString())
            setContentView(R.layout.activity_bottom_navigation)

            val aaa = BottomNavigation()
            //aaa.navigation
            val navigation =  this.findViewById<View>(R.id.navigation) as BottomNavigationView
            navigation.selectedItemId = R.id.navigation_direction
            val directionFragment = DirectionsFragment.newInstance(latLng_toDirection.toString())
            openFragment(directionFragment)
            val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        getSupportActionBar()!!.setTitle("Home")
                        val homeFragment = HomeFragment.newInstance()
                        openFragment(homeFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_favorite -> {
                        getSupportActionBar()!!.setTitle("Favorite Places")
                        val favouriteFragment = FavoritePlaceFragment.newInstance()
                        openFragment(favouriteFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_direction -> {
                        getSupportActionBar()!!.setTitle("Direction")
                        val directionFragment = DirectionsFragment.newInstance(latLng_toDirection.toString())
                        openFragment(directionFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_history -> {
                        getSupportActionBar()!!.setTitle("History")
                        val historyFragment = HistoryFragment.newInstance()
                        openFragment(historyFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.navigation_profile -> {
                        getSupportActionBar()!!.setTitle("Profile")
                        val profileFragment = NewProfileFragment.newInstance()
                        openFragment(profileFragment)
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }

            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


            /*val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.detailmyplace, directionFragment)
            fab_directions2.visibility = View.GONE
            app_bar_layout.visibility = View.GONE
            transaction.attach(directionFragment)
            transaction.addToBackStack(null)
            transaction.commit()
            directionbackDetail = true*/
        }

    }

   /* override fun onBackPressed() {
        super.onBackPressed()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_home
        val homeFragment = HomeFragment.newInstance()
        openFragment(homeFragment)
    }*/

    fun getMyData(): String {
        return latLng_toDirection.toString()
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    var latLng_toDirection: LatLng = LatLng(10.762622, 106.660172)
    // Request photos and metadata for the specified place.
    private fun getPhoto(placeId: String) {
        //val placeId = "ChIJa147K9HX3IAR-lwiGIQv9i4"

        //val ratingBar = this.findViewById<View>(R.id.rating_bar) as RatingBar

        val mGeoDataClient = Places.getGeoDataClient(this)

        val photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId)
        photoMetadataResponse.addOnCompleteListener{ task ->
            // Get the list of photos.
            val photos = task.result
            // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
            val photoMetadataBuffer = photos.photoMetadata
            Log.e(TAG,"ABC : "+photoMetadataBuffer.count)
            // Get the first photo in the list.
            for(i in 0 until photoMetadataBuffer.count)
            {
                val photoMetadata = photoMetadataBuffer.get(i)
                // Get a full-size bitmap for the photo.
                val photoResponse = mGeoDataClient.getPhoto(photoMetadata)
                photoResponse.addOnCompleteListener{ task ->
                    val photo = task.result
                    val bitmap = photo.bitmap
                    Log.e(TAG,""+bitmap)
                    arraySliding.add(bitmap)
                    adapterSliding.notifyDataSetChanged()
                }
            }
            adapterSliding = SlidingImageAdapter(this,arraySliding)
            sliding_view_pager.adapter = adapterSliding
            pageIndicatorView.count = arraySliding.size
            pageIndicatorView.setViewPager(sliding_view_pager)
        }

        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val txt_place_name = this.findViewById<View>(R.id.txt_place_name) as TextView
                val ratingBar = this.findViewById<View>(R.id.rating_bar) as RatingBar
                val ratingNumber = this.findViewById<View>(R.id.rating_number) as TextView
                val txt_address = this.findViewById<View>(R.id.txt_address) as TextView
                val txt_phonenumber = this.findViewById<View>(R.id.txt_phonenumber) as TextView
                val txt_weburi = this.findViewById<View>(R.id.txt_weburi) as TextView


                val places = task.result
                val myPlace = places.get(0)

                txt_place_name.text=myPlace.name
                ratingBar.rating=myPlace.rating
                ratingNumber.text=myPlace.rating.toString()

                if(myPlace.address == "" || myPlace.address == null){
                    txt_address.text = "Chưa cập nhật"
                }
                else txt_address.text = myPlace.address

                if(myPlace.phoneNumber == "" || myPlace.phoneNumber == null){
                    txt_phonenumber.text = "Chưa cập nhật"
                }
                else txt_phonenumber.text=myPlace.phoneNumber

                if(myPlace.websiteUri.toString() == "" || myPlace.websiteUri == null){
                    txt_weburi.text = "Chưa cập nhật"
                }
                else txt_weburi.text= myPlace.websiteUri.toString()

                latLng_toDirection = myPlace.latLng // to send to DirectionsFragment
                Log.i(TAG, "Place latlng found: " + latLng_toDirection)


                Log.i(TAG, "Place address found: " + myPlace.address)
                //Log.i(TAG, "Place attributions found: " + myPlace.attributions)
                //Log.i(TAG, "Place latLng found: " + myPlace.latLng)
                //Log.i(TAG, "Place locale found: " + myPlace.locale)
                Log.i(TAG, "Place name found: " + myPlace.name)
                Log.i(TAG, "Place rating found: " + myPlace.rating)
                Log.i(TAG, "Place phoneNumber found: " + myPlace.phoneNumber)
                Log.i(TAG, "Place placeTypes found: " + myPlace.placeTypes.get(0))
                //Log.i(TAG, "Place priceLevel found: " + myPlace.priceLevel)
                Log.i(TAG, "Place viewport found: " + myPlace.viewport.toString())
                Log.i(TAG, "Place websiteUri found: " + myPlace.websiteUri)

                places.release()
            } else {
                Log.e(TAG, "Place not found.")
            }
        }
    }


}