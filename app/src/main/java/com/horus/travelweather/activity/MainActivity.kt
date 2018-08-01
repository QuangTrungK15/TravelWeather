package com.horus.travelweather.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.google.firebase.database.DatabaseError
import android.widget.Toast
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.horus.travelweather.common.TWConstant
import com.horus.travelweather.model.UserDbO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnCompleteListener
import android.app.ProgressDialog
import com.horus.travelweather.R


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.toString()

    lateinit var btnLogin: Button
    lateinit var btnSignUp: Button
    lateinit var editEmail: MaterialEditText
    lateinit var editPassword: MaterialEditText


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

            /*   // Attach a listener to read the data at our posts reference
            table_user.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if(p0.child(editPhone.text.toString()).exists())
                    {


                        val user = p0.child(editPhone.text.toString()).getValue(UserDbO::class.java)

                        //Log.e("AAA" , ""+ editPhone.text.toString())

                        user!!.phone = editPhone.text.toString()

                        if (user.password == editPassword.text.toString()) {
                            val homeIntent = Intent(this@MainActivity, HomeActivity::class.java)
                            TWConstant.currentUser = user
                            startActivity(homeIntent)
                            finish()

                        } else {
                            Toast.makeText(this@MainActivity, "Sign In failed !!!", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else
                        Toast.makeText(this@MainActivity, "UserDbO not exits in database !!!", Toast.LENGTH_SHORT).show()

                }
            })

*/

            signIN(editEmail.text.toString(), editPassword.text.toString())
        }

    }


    private fun signIN(email: String, password: String) {

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
                                //Log.e(TAG,"11111111"+ TWConstant.currentUser.name)
                                val homeIntent = Intent(this@MainActivity, HomeActivity::class.java)
                                startActivity(homeIntent)
                                finish()
                            }
                        })
                    } else {
                        progress.dismiss()
                        Log.e(TAG, "signIn: Fail!", task.getException())
                        Toast.makeText(this@MainActivity, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    }
                })
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


}
