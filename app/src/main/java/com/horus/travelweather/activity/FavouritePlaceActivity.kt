package com.horus.travelweather.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.PlacePhotoMetadataResponse
import com.google.android.gms.location.places.PlacePhotoResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.horus.travelweather.R
import com.horus.travelweather.adapter.FavouritePlaceAdapter
import com.horus.travelweather.common.TWConstant.Companion.REMOVE_PLACE
import com.horus.travelweather.model.PlaceDbO
import kotlinx.android.synthetic.main.activity_favourite_my_place.*
import java.io.ByteArrayOutputStream
import java.util.*


class FavouritePlaceActivity : AppCompatActivity() {


    private val TAG = FavouritePlaceActivity::class.java.simpleName
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private val placeDb = PlaceDbO()
    lateinit var database: FirebaseDatabase
    lateinit var favourite_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var adapter: FirebaseRecyclerAdapter<PlaceDbO, FavouritePlaceAdapter.PlaceViewHolder>
    var u: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite_my_place)
        mAuth = FirebaseAuth.getInstance()
        u = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        favourite_list = database.getReference("favouriteplace")

        //Use Autocomplete to search place to add favorite places
        btn_add_my_place.setOnClickListener {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .setCountry("VN")
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(this)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }

        //Use FirebaseRecyclerOptions to put favoriteplace list in rv_my_places
        val options = FirebaseRecyclerOptions.Builder<PlaceDbO>()
                .setQuery(favourite_list.child(u!!.uid), PlaceDbO::class.java)
                .setLifecycleOwner(this)
                .build()
        adapter = FavouritePlaceAdapter(options, { context, textView, i ->
            showPopup(context, textView, i)
        },{
            openDetailPlace(it)
        })
        rv_my_places.layoutManager = LinearLayoutManager(this)
        rv_my_places.adapter = adapter
    }

    //CLick any img
    private fun openDetailPlace(it: PlaceDbO) {
        val intent = Intent(this@FavouritePlaceActivity, DetailMyPlace::class.java)
        intent.putExtra("MyPlace",it)
        startActivity(intent)
    }

    //Click popup menu of any img
    private fun showPopup(context: Context, textView: TextView, position: Int) {
        var popup: PopupMenu? = null
        popup = PopupMenu(context, textView)
        //Add only option (remove) of per img
        popup.getMenu().add(0, position, 0, REMOVE_PLACE);
        popup.setOnMenuItemClickListener({ item ->
            deleteFavouritePlace(adapter.getRef(position).key!!) //get position id of rv_my_places
            true
        })
        popup.show()
    }

    //Carry on with AUTOCOMPLETE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                placeDb.placeId = place.id
                placeDb.name = place.name.toString()
                getPhoto(place.id)
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.e(TAG, status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }
    //Use PLACE PHOTOS of GG MAP API (AS`)
    // Request photos and metadata for the specified place.
    // mGeoDataClient - is to get detail of that place and then to move the marker (đánh dấu)
    // of the map to its co-ordinates (các tọa độ).
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
            val photoMetadata = photoMetadataBuffer.get(0)
            // Get a full-size bitmap for the photo.
            val photoResponse = mGeoDataClient.getPhoto(photoMetadata)

            //Listener Event compeleted itselt, update to firebase's storage
            photoResponse.addOnCompleteListener(OnCompleteListener<PlacePhotoResponse> { task ->
                val photo = task.result
                val bitmap = photo.bitmap
                upLoadBitmapToStorage(bitmap)
            })
        })
    }

    //Add that place's photos to storage & that place to firebase
    private fun upLoadBitmapToStorage(bitmap: Bitmap) {
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReference("images")
        val imageName = UUID.randomUUID().toString()
        val imageFolder = storageReference.child(imageName)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageFolder.putBytes(data)

        //after add photos to storage, update that place's uri to placeDb -> add to firebase by uploadDatabase()
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
        }).addOnSuccessListener(OnSuccessListener<Any> { taskSnapshot ->
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            imageFolder.downloadUrl.addOnSuccessListener {
                Log.e(TAG, "Image : " + it.toString())
                placeDb.uri = it.toString()
                uploadDatabase()
            }
        })

    }
    private fun deleteFavouritePlace(key: String) {
        favourite_list.child(u!!.uid).child(key).removeValue()
        adapter.notifyDataSetChanged()
    }
    private fun uploadDatabase() {
        favourite_list.child(u!!.uid).push().setValue(placeDb)
    }

}