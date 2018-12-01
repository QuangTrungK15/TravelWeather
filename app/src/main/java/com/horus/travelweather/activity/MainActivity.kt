package com.horus.travelweather.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.horus.travelweather.R
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.database.PlaceDatabase
import com.horus.travelweather.model.UserDbO


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.toString()

    lateinit var btnLogin: Button
    lateinit var btnSignUp: Button
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var mAuth: FirebaseAuth

    lateinit var table_user: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
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
    }

    private fun signIn(email: String, password: String) {

        if (!validateForm(email, password))
            return

        val progress = ProgressDialog(this)
        progress.setMessage("Loading....")
        progress.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // update UI with the signed-in user's information
                        val u = mAuth.getCurrentUser()
                        table_user.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                val user = p0.child(u!!.uid).getValue(UserDbO::class.java)
                                TWConstant.currentUser = user!!
                                Log.e(TAG,"Phone : "+ user.phone)
                                progress.dismiss()
                                deleteAllPLace().execute()
                                val homeIntent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(homeIntent)
                            }
                        })
                    } else {
                        progress.dismiss()
                        Log.e(TAG, "signIn: Fail!", task.exception)
                        Toast.makeText(this@MainActivity, getString(R.string.authentication_fail), Toast.LENGTH_SHORT).show()
                    }
                })
    }

    //Move to another activity, close this activity
    /*override fun onPause() {
        super.onPause()
        finish()
    }*/

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
            PlaceDatabase.getInstance(this@MainActivity).placeDataDao().deleteAll()
            return null
        }
    }

}
