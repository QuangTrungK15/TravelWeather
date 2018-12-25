package com.horus.travelweather.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.BottomNavigation
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.ProfileEntity
import com.horus.travelweather.database.TravelWeatherDB
import com.horus.travelweather.model.UserDbO
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.toString()
    private val compositeDisposable = CompositeDisposable()

    lateinit var btnLogin: Button
    lateinit var btnSignUp: TextView
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var mAuth: FirebaseAuth

    lateinit var table_user: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        table_user = database.getReference("users")
        mAuth = FirebaseAuth.getInstance()
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        editEmail = findViewById(R.id.txtEmail)
        editPassword = findViewById(R.id.txtPassword)
        btnSignUp.setOnClickListener {
            val signUpIntent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
        btnLogin.setOnClickListener {
            signIn(editEmail.text.toString(), editPassword.text.toString())
        }

        //get all profile from database room
        val getProfile = TravelWeatherDB.getInstance(this@MainActivity).profileDataDao()
        compositeDisposable.add(getProfile.getAllProfileUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    Toast.makeText(this@MainActivity, ""+it.size, Toast.LENGTH_SHORT).show();
                }, {
                    Log.e(TAG, "" + it.message)
                }))

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun signIn(email: String, password: String) {

        if (!validateForm(email, password))
            return
        val progress = ProgressDialog(this)
        progress.setMessage("Loading....")
        progress.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    if (task.isSuccessful) {
                        // update UI with the signed-in user's information
                        val u = mAuth.getCurrentUser()
                        table_user.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                try {
                                    val user = p0.child(u!!.uid).getValue(UserDbO::class.java)
                                    val profileUser = ProfileEntity(u.uid,user!!.name,user.email, user.phone)
                                    TWConstant.currentUser = user
                                    // insert user info into database room.
                                    insertProfileUser().execute(profileUser)
                                    Log.e(TAG, ""+ user.email)
                                    progress.dismiss()
                                    deleteAllPLace().execute()
                                    intoMainActivity()
                                }
                                catch (e : Exception)
                                {
                                    Toast.makeText(this@MainActivity, ""+e.message, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                    } else {
                        progress.dismiss()
                        Log.e(TAG, "signIn: Fail!", task.exception)
                        Toast.makeText(this@MainActivity, getString(R.string.authentication_fail), Toast.LENGTH_SHORT).show()
                    }
                }
    }

    //Move to another activity, close this activity
    /*override fun onPause() {
        super.onPause()
        finish()
    }*/

    private fun intoMainActivity()
    {
//        val homeIntent = Intent(this@MainActivity, HomeActivity::class.java)
//        startActivity(homeIntent)
        val homeIntent = Intent(this@MainActivity, BottomNavigation::class.java)
        startActivity(homeIntent)
    }

    private fun validateForm(email: String, password: String): Boolean {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@MainActivity, "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@MainActivity, "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length < 6) {
            Toast.makeText(this@MainActivity, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true
    }

    //Clear it before this activity refers to HomeActivity (in this activity, firebase data will put all place data in dbRoom)
    inner class deleteAllPLace(): AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            TravelWeatherDB.getInstance(this@MainActivity).placeDataDao().deleteAll()
            return null
        }
    }

    inner class insertProfileUser(): AsyncTask<ProfileEntity, Void, Void>() {
        override fun doInBackground(vararg params: ProfileEntity): Void? {
                TravelWeatherDB.getInstance(this@MainActivity).profileDataDao().insert(params[0])
            return null
        }
    }



}
