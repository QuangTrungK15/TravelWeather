package com.horus.travelweather.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.R
import com.horus.travelweather.R.id.view_pager
import com.horus.travelweather.adapter.ViewPagerAdapter
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_fragment.*

class HomeFragment : Fragment() {

    private val TAG = HomeFragment::class.java.simpleName
    private val compositeDisposable = CompositeDisposable()
    private val menu : MutableList<PlaceEntity> = mutableListOf()
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth
    //lateinit var progress_loading: ProgressBar

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG,"Destroy Destroy Destroy Destroy Destroy Destroy : ")
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_home_fragment, container, false)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)

        //progress_loading = view.findViewById(R.id.progress_loading)

        var progress = ProgressDialog(context)
        val progress_loading = view.findViewById<ProgressBar>(R.id.progress_loading) as ProgressBar
        progress_loading.isIndeterminate = true
        progress_loading.max = 100

        progress_loading.visibility = View.VISIBLE
       // progress.setIndeterminateDrawable(R.drawable.my_progress_indeterminate)
        //progress.setMessage("Loading....")
        //progress.setProgressDrawable(resources.getDrawable(R.drawable.bottleloading))
        //progress.max = 100
        //progress.show()
        val rxPermissions = RxPermissions(this)
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted ->
                    if (granted) {
                        //to read firebase date, we need ValueEventListener
                        place_list.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Log.e(TAG,"Error in HomeFragment: "+p0.message)
                            }
                            override fun onDataChange(dataSnapshot : DataSnapshot) {
                                try {
                                    val placeList = ArrayList<PlaceEntity>()
                                    // Result will be holded Here
                                    for (dsp in dataSnapshot.children) {
                                        //add result into array list
                                        val item : PlaceEntity? = dsp.getValue(PlaceEntity::class.java)
                                        Log.e(TAG,"PlaceEntity : "+item)
                                        if (item != null) {
                                            placeList.add(item)
                                        }
                                    }
                                    Log.e(TAG,"Size : "+placeList.size)
                                    insertAllPlace().execute(placeList)
                                    //progress.cancel()
                                    progress_loading.visibility = View.GONE
                                    //Bind fragments on viewpager
                                } catch (e: NullPointerException) {
                                    Log.e(TAG, "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee : "+e.message)
                                }


                            }
                        })
                    } else {
                        // Oups permission denied
                        Log.e(TAG, "Access fail")
                    }
                }
        return view
    }


    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    private fun getDataFromLocal()
    {
        // RxJava là thư viện mã nguồn mở implement ReactiveX trên Java. Có 2 lớp chính là Observable và Subscriber:
        // Observable là một lớp đưa ra dòng dữ liệu hoặc sự kiện (event). Flow của Observable là đưa ra một
        // hoặc nhiều các items, sau đó gọi kết thúc thành công hoặc lỗi.
        // Subscriber lắng nghe flow, thực thi các hành động trên dòng dữ liệu hoặc sự kiện được đưa ra bởi Observable

        //get all places from database room
        val getAllPlace = TravelWeatherDB.getInstance(context!!).placeDataDao()

        // Probably, you already know that all UI code is done on Android Main thread.
        // RxJava is java library and it does not know about Android Main thread. That is the reason why we use RxAndroid.
        // RxAndroid gives us the possibility to choose Android Main thread as the thread where our code will be executed.
        // Obviously, our Observer should operate on Android Main thread.
        try {
            compositeDisposable.add(getAllPlace.getAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ //excute event
                        Log.e(TAG,"Get data from local : "+it)
                        val adapter = ViewPagerAdapter(childFragmentManager,it)
                        view_pager.adapter = adapter
                    }, {
                        Log.e(TAG, "" + it.message)
                    }))
        } catch (e: NullPointerException) {
            Log.e(TAG, "getdatafromlocal lich : "+e.message)
        }

    }

    inner class insertAllPlace() : AsyncTask<ArrayList<PlaceEntity>, Void, Void>() {
        override fun doInBackground(vararg params : ArrayList<PlaceEntity>): Void? {
            TravelWeatherDB.getInstance(context!!).placeDataDao().insertAllPlace(params[0])
            getDataFromLocal()
            return null
        }
    }
}