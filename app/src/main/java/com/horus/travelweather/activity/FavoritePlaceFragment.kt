package com.horus.travelweather.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.PlacePhotoMetadataResponse
import com.google.android.gms.location.places.PlacePhotoResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R
import com.horus.travelweather.adapter.FavouritePlaceAdapter
import com.horus.travelweather.adapter.HistoryAdapter
import com.horus.travelweather.common.TWConstant.Companion.REMOVE_PLACE
import com.horus.travelweather.model.HistoryDbO
import com.horus.travelweather.model.PlaceDbO
import com.horus.travelweather.model.TempFavPlaceDbO
import kotlinx.android.synthetic.main.activity_directions.view.*
import kotlinx.android.synthetic.main.activity_favourite_my_place.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class FavoritePlaceFragment : Fragment() {


    private val TAG = FavoritePlaceFragment::class.java.simpleName
    var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private val placeDb = PlaceDbO()
    lateinit var database: FirebaseDatabase
    lateinit var favourite_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    lateinit var adapter: FirebaseRecyclerAdapter<PlaceDbO, FavouritePlaceAdapter.PlaceViewHolder>
    var myuser: FirebaseUser? = null

    private val historyDb = HistoryDbO()
    lateinit var history_list: DatabaseReference
    lateinit var adapter2: FirebaseRecyclerAdapter<HistoryDbO, HistoryAdapter.HistoryViewHolder>

    //ask to add temp favplace to favouriteplace
    private val tempfavplaceDb = TempFavPlaceDbO() //for AI
    lateinit var tempfavplace_list: DatabaseReference //for AI

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_favourite_my_place, container, false)
        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        myuser = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        favourite_list = database.getReference("favouriteplace")
        history_list = database.getReference("history")
        tempfavplace_list = database.getReference("tempfavplace").child(mAuth.currentUser!!.uid)

        add_tempfavplace()
        view.btn_add_my_place.setOnClickListener {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .setCountry("VN")
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(this.activity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        }

        val options = FirebaseRecyclerOptions.Builder<PlaceDbO>()
                .setQuery(favourite_list.child(myuser!!.uid), PlaceDbO::class.java)
                .setLifecycleOwner(this)
                .build()
        adapter = FavouritePlaceAdapter(options, { context, textView, i ->
            showPopup(context, textView, i)
        },{
            openDetailPlace(it)
        })
        view.rv_my_places.layoutManager = LinearLayoutManager(this.activity)
        view.rv_my_places.adapter = adapter
        view.rv_my_places.setNestedScrollingEnabled(false)
        return view
    }

    lateinit var favplace_list: DatabaseReference


    fun add_tempfavplace(){
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        tempfavplace_list = database.getReference("tempfavplace").child(mAuth.currentUser!!.uid)

        //Get favplace name list to ask add tempfavplace
        favplace_list = database.getReference("favouriteplace").child(mAuth.currentUser!!.uid)
        val favplacename_list = java.util.ArrayList<String>()
        var index_temp = 0

        favplace_list.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Result will be holded Here
                for (dsp in dataSnapshot.children) {
                    //add result into array list
                    val item: PlaceDbO? = dsp.getValue(PlaceDbO::class.java)
                    if (item != null) {
                        favplacename_list.add(item.name)
                        Log.d("favplace name : ", favplacename_list.get(index_temp))
                        index_temp++
                    }
                }
            }
        })
        //end

        //check tempfavplace data to add/update/ask tempfavplace
        tempfavplace_list.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG, "Error : " + p0.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    // code if data exists
                    for (dsp in dataSnapshot.children) {
                        val item: TempFavPlaceDbO? = dsp.getValue(TempFavPlaceDbO::class.java)
                        if (item != null) {

                            //Add new place if temp place (visit:1 or searchh: 5)
                            val date = getCurrentDateTime()
                            val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)

                            if (favplacename_list.contains(item.name) == true) {

                            } else if ((item.numofsearch > 2) && item.numofask < 2
                                    && currenttime != item.askdate && currentday_oldday_space(item.askdate) > 2
                            ) {
                                val alertDialogBuilder = AlertDialog.Builder(context)
                                alertDialogBuilder.setTitle("Thêm địa điểm yêu thích")
                                alertDialogBuilder
                                        .setMessage("Bạn có muốn thêm " + item.name + " vào danh sách yêu thích để tiện theo dõi" +
                                                " địa điểm hay không?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes") { dialog, id ->
                                            // Add new place if temp place qualified
                                            //for(pl in placeList){
                                            //    if(pl.name == item.name){
                                            val favplaceDB = PlaceDbO()
                                            favplaceDB.name = item.name
                                            favplaceDB.address = item.address
                                            favplaceDB.placeId = item.id
                                            favplaceDB.uri = item.uri
                                            favplace_list.child(item.id).setValue(favplaceDB)

                                            //  }
                                            //}
                                            Toast.makeText(context, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
                                            //val intent = Intent(context, BottomNavigation::class.java) //this activity will be this fragment's father
                                           // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            //startActivity(intent)
                                        }
                                        .setNegativeButton("No") { dialog, id ->
                                            // if this button is clicked, just close
                                            // the dialog box and do nothing
                                            tempfavplaceDb.id = item.id
                                            //tempfavplaceDb.latitude = item.latitude
                                            // tempfavplaceDb.longitude = item.longitude
                                            tempfavplaceDb.name = item.name
                                            tempfavplaceDb.uri = item.uri
                                            tempfavplaceDb.address = item.address
                                            tempfavplaceDb.numofsearch = item.numofsearch
                                            tempfavplaceDb.numofvisit = item.numofvisit
                                            tempfavplaceDb.numofask = item.numofask+1
                                            tempfavplaceDb.askdate = currenttime

                                            tempfavplace_list.child(item.id).setValue(tempfavplaceDb)

                                            dialog.cancel()
                                        }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }
                        }//
                    }

                } else {

                }
                // Result will be holded Here

                //insertAllPlace().execute(placeList)
            }
        })
    }

    fun currentday_oldday_space(startDate:String) : Long{
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = Date()
        var date1: Date? = null
        var date2: Date? = null

        var getDaysDiff:Long = 0
        try
        {
            //startDate = "01-01-2016"
            val endDate = simpleDateFormat.format(currentDate)
            date1 = simpleDateFormat.parse(startDate)
            date2 = simpleDateFormat.parse(endDate)
            val getDiff = date2.getTime() - date1.getTime()
            getDaysDiff = getDiff / (24 * 60 * 60 * 1000)
            println("Differance between date " + startDate + " and " + endDate + " is " + getDaysDiff + " days.")
        }
        catch (e:Exception) {
            e.printStackTrace()
        }

        return getDaysDiff
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (inflater != null) {
            inflater.inflate(R.menu.option_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.removeall -> {
                deleteAllFavPlace()
                return true
            }
        }
        return false
    }

    companion object {
        fun newInstance(): FavoritePlaceFragment = FavoritePlaceFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun openDetailPlace(it: PlaceDbO) {
        val intent = Intent(context, DetailMyPlace::class.java)
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
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
    //Carry on with AUTOCOMPLETE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this.context, data)


                val geocoder = Geocoder(context!!, Locale.getDefault())
                try
                {
                    val addresses = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)

                    if (addresses != null)
                    {
                        val returnedAddress = addresses.get(0)
                        val strReturnedAddress = StringBuilder("Address:\n")
                        for (i in 0 until returnedAddress.getMaxAddressLineIndex())
                        {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                        }
                        placeDb.address = addresses.get(0).getAddressLine(0)
                       // Log.e("start location: ",addresses.get(0).getAddressLine(0))
                    }
                    else
                    {
                        Log.d("a","No Address returned! : ")

                    }
                }
                catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                    Log.d("a","Canont get Address!")
                }

                placeDb.placeId = place.id
                placeDb.name = place.name.toString()
                getPhoto(place.id)

                //history object
                historyDb.address = place.address.toString()
                historyDb.name = place.name.toString()
                historyDb.placeTypes = place.placeTypes.toString()
                historyDb.historyId = place.id
                val date = getCurrentDateTime()
                //val c = GregorianCalendar(1995, 12, 23)
                val currenttime = String.format("%1\$td/%1\$tm/%1\$tY", date)
                historyDb.date = currenttime
                uploadDatabase2() //add to firebase
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this.context, data)
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
        val mGeoDataClient = Places.getGeoDataClient(this.context!!)
        val photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId)
        photoMetadataResponse.addOnCompleteListener(OnCompleteListener<PlacePhotoMetadataResponse>{ task ->
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

    private fun deleteAllFavPlace() {
        favourite_list.child(myuser!!.uid).removeValue()
        adapter.notifyDataSetChanged()
    }

    private fun deleteFavouritePlace(key: String) {
        favourite_list.child(myuser!!.uid).child(key).removeValue()
        adapter.notifyDataSetChanged()
    }
    private fun uploadDatabase() {
        favourite_list.child(myuser!!.uid).push().setValue(placeDb)
    }

    private fun uploadDatabase2() {
        history_list.child(myuser!!.uid).push().setValue(historyDb)
    }

}