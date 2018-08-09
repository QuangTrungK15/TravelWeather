package com.horus.travelweather.adapter

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.horus.travelweather.R

class SlidingImageAdapter(private val context: Context, private val imageModelArrayList: ArrayList<Bitmap>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
       return imageModelArrayList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout = LayoutInflater.from(context).inflate(R.layout.sliding_images_layout, container, false)!!

        val imageView = imageLayout.findViewById(R.id.image) as ImageView

        imageView.setImageBitmap(imageModelArrayList[position])

        container.addView(imageLayout, 0)

        return imageLayout
    }
}