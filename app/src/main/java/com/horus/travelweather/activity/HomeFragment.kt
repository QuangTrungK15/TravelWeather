package com.horus.travelweather.activity

import android.Manifest
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.R
import com.horus.travelweather.adapter.ViewPagerAdapter
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.PlaceEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home_fragment.*


class HomeFragment : Fragment() {

    private val TAG = HomeFragment::class.java.simpleName
    private val compositeDisposable = CompositeDisposable()
    private val menu : MutableList<PlaceEntity> = mutableListOf()
    lateinit var database: FirebaseDatabase
    lateinit var place_list: DatabaseReference
    lateinit var mAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_home_fragment, container, false)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        place_list = database.getReference("places").child(mAuth.currentUser!!.uid)
        val rxPermissions = RxPermissions(this)
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe { granted ->
                    if (granted) {
                        //to read firebase date, we need ValueEventListener
                        place_list.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Log.e(TAG,"Error : "+p0.message)
                            }
                            override fun onDataChange(dataSnapshot : DataSnapshot) {
                                val placeList = ArrayList<PlaceEntity>()
                                // Result will be holded Here
                                for (dsp in dataSnapshot.children) {
                                    //add result into array list
                                    val item : PlaceEntity? = dsp.getValue(PlaceEntity::class.java)
                                    if (item != null) {
                                        placeList.add(item)
                                    }
                                }
                                Log.e(TAG,"Size : "+placeList.size)
                                insertAllPlace().execute(placeList)
                                //Bind fragments on viewpager
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
        compositeDisposable.add(getAllPlace.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ //excute event
                    val adapter = ViewPagerAdapter(childFragmentManager,it)
                    view_pager.adapter = adapter
                }, {
                    Log.e(TAG, "" + it.message)
                }))
    }

    inner class insertAllPlace() : AsyncTask<ArrayList<PlaceEntity>, Void, Void>() {
        override fun doInBackground(vararg params : ArrayList<PlaceEntity>): Void? {
            TravelWeatherDB.getInstance(context!!).placeDataDao().insertAllPlace(params[0])
            getDataFromLocal()
            return null
        }
    }
}