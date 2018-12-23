package com.horus.travelweather.activity

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.horus.travelweather.R
import com.horus.travelweather.database.TravelWeatherDB
import com.horus.travelweather.database.ProfileEntity
import com.horus.travelweather.model.UserDbO
import com.rengwuxian.materialedittext.MaterialEditText


class SignUpActivity : AppCompatActivity() {


    private val TAG : String = MainActivity::class.toString()


    lateinit var editEmail: EditText
    lateinit var editPass: EditText
    lateinit var editName: EditText
    lateinit var editPhone: EditText
    lateinit var btnSignUp: Button

    lateinit var mAuth : FirebaseAuth

    lateinit var table_user : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        table_user = database.getReference("users")

        mAuth  = FirebaseAuth.getInstance()


        editEmail = findViewById(R.id.editEmail)
        editName = findViewById(R.id.editName)
        editPass = findViewById(R.id.editPass)
        editPhone = findViewById(R.id.editPhone)

        btnSignUp = findViewById(R.id.btnSignUp)



        btnSignUp.setOnClickListener {
            createAccount(editEmail.text.toString(),editPass.text.toString())
        }
    }


    private fun createAccount(email : String , password : String)
    {
        if (!validateForm(email, password)) {
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {


            if(it.isSuccessful)
            {
                Log.e(TAG, "createAccount: Success!");
                val user : FirebaseUser = mAuth.currentUser!!
                writeNewUser(user.uid,editName.text.toString(), user.email!!,editPhone.text.toString())
                Toast.makeText(this@SignUpActivity, getString(R.string.sign_up_successfull), Toast.LENGTH_SHORT).show()
                finish()
            }
            else
            {
                //Log.e("AAA", "signUp: Fail!", it.getException())
                Toast.makeText(this@SignUpActivity, getString(R.string.already_in_use), Toast.LENGTH_SHORT).show()

            }

        }




    }


    private fun writeNewUser(userId : String, username : String, email : String, phone : String) {
        val user  = UserDbO(username, email,phone)
        val userEntity = ProfileEntity(userId,username,email,phone)
        table_user.child(userId).setValue(user)
        insertProfile().execute(userEntity)
    }


    private fun validateForm(email:String , password : String) : Boolean
    {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@SignUpActivity, "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@SignUpActivity, "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length < 6) {
            Toast.makeText(this@SignUpActivity, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true
    }


    inner class insertProfile(): AsyncTask<ProfileEntity, Void, Void>() {
        override fun doInBackground(vararg params: ProfileEntity): Void? {
            TravelWeatherDB.getInstance(this@SignUpActivity).profileDataDao().insert(params[0])
            return null
        }
    }


}
