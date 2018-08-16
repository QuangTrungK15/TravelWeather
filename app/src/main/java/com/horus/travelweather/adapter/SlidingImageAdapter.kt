package com.horus.travelweather.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.horus.travelweather.R
import com.horus.travelweather.activity.DetailMyPlace

class SlidingImageAdapter(private val context: Context,private val listImage : List<Bitmap>) : PagerAdapter() {

    private val TAG = DetailMyPlace::class.java.simpleName
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return listImage.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(context).inflate(R.layout.sliding_images_layout, container, false)
        val imageView = imageLayout.findViewById(R.id.image) as ImageView
        imageView.setImageBitmap(listImage[position])
        container.addView(imageLayout, 0)
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
//        super.destroyItem(container, position, `object`)
    }
}