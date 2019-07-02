
package com.horus.travelweather.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.horus.travelweather.R

class   AboutUsActivity : AppCompatActivity() {

    private lateinit var  progress : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aboutus)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progress = ProgressDialog(this)
        progress.setMessage("Loading....")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if(id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
