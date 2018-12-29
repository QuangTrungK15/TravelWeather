package com.horus.travelweather.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.toString()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var btnLogin: Button
    lateinit var btnSignUp: TextView
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var mAuth: FirebaseAuth

    lateinit var table_user: DatabaseReference

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        // [END config_signin]
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        table_user = database.getReference("users")
        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        editEmail = findViewById(R.id.txtEmail)
        editPassword = findViewById(R.id.txtPassword)
        btnSignUp.setOnClickListener {
            val signUpIntent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }
        btnLogin.setOnClickListener {
            signInWithEmail(editEmail.text.toString(), editPassword.text.toString())
        }

        sign_in_with_gg.setOnClickListener {
            signInWithGG()
        }

        //get all profile from database room
        val getProfile = TravelWeatherDB.getInstance(this@MainActivity).profileDataDao()
        compositeDisposable.add(getProfile.getAllProfileUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                    Log.e(TAG, "" + it.message)
                }))

    }

    private fun signInWithGG() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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

    private fun signInWithEmail(email: String, password: String) {
        if (!validateForm(email, password))
            return
        val progress = ProgressDialog(this)
        progress.setMessage("Loading....")
        progress.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // update UI with the signed-in user's information
                        val u = mAuth.getCurrentUser()
                        table_user.addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                try {
                                    val user = p0.child(u!!.uid).getValue(UserDbO::class.java)
                                    val profileUser = ProfileEntity(u.uid, user!!.name, user.email,user.phone)
                                    TWConstant.currentUser = user
                                    // insert user info into database room.
                                    insertProfileUser().execute(profileUser)
                                    progress.dismiss()
                                    deleteAllPLace().execute()
                                    intoMainActivity()
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Error : " + e.message, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                    } else {
                        progress.dismiss()
                        Log.e(TAG, "signInWithEmail: Fail!", task.exception)
                        Toast.makeText(this@MainActivity, getString(R.string.authentication_fail), Toast.LENGTH_SHORT).show()
                    }
                }
    }


    // [START onactivityresult]
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
//                Toast.makeText(this@MainActivity, "Google sign in successfull", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this@MainActivity, "Error  :  " + e.message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
//                updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        // [START_EXCLUDE silent]
//        showProgressDialog()
        val progress = ProgressDialog(this)
        progress.setMessage("Loading....")
        progress.show();
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, save data with the signed-in user's information
                        val u = mAuth.currentUser
                        if (u != null) {
                            table_user.orderByKey().equalTo(u.uid).addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    Toast.makeText(this@MainActivity, "Error Equal", Toast.LENGTH_SHORT).show()
                                }
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.getChildren().iterator().hasNext()) {
                                        try {
                                            val user = dataSnapshot.child(u!!.uid).getValue(UserDbO::class.java)
                                            TWConstant.currentUser = user!!
                                            progress.dismiss()
                                            deleteAllPLace().execute()
                                            intoMainActivity()
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error : " + e.message, Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        try {
                                            val userDbO = UserDbO(u.displayName!!,u.email!!, "01234", u.photoUrl.toString())
                                            if (table_user.child(u.uid).setValue(userDbO).isComplete) {
                                                val user = dataSnapshot.child(u!!.uid).getValue(UserDbO::class.java)
                                                TWConstant.currentUser = user!!
                                                progress.dismiss()
                                                deleteAllPLace().execute()
                                                intoMainActivity()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Error : " + e.message, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                            })
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithCredential:failure", task.exception)
                    }

                    // [START_EXCLUDE]
                    progress.dismiss()
                    // [END_EXCLUDE]
                }
    }

    // [END auth_with_google]
    private fun intoMainActivity() {
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
    inner class deleteAllPLace() : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            TravelWeatherDB.getInstance(this@MainActivity).placeDataDao().deleteAll()
            return null
        }
    }

    inner class insertProfileUser() : AsyncTask<ProfileEntity, Void, Void>() {
        override fun doInBackground(vararg params: ProfileEntity): Void? {
            TravelWeatherDB.getInstance(this@MainActivity).profileDataDao().insert(params[0])
            return null
        }
    }


}
