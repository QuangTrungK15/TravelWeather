package com.example.dell.travelweather

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import com.google.firebase.database.*
import com.rengwuxian.materialedittext.MaterialEditText
import android.widget.Toast
import com.example.dell.travelweather.model.UserDbO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SignUpActivity : AppCompatActivity() {


    private val TAG : String = MainActivity::class.toString()


    lateinit var editEmail: MaterialEditText
    lateinit var editPass: MaterialEditText
    lateinit var editName: MaterialEditText
    lateinit var editPhone: MaterialEditText
    lateinit var btnSignUp: Button

    lateinit var mAuth : FirebaseAuth

    lateinit var table_user : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        table_user = database.getReference("user")

        mAuth  = FirebaseAuth.getInstance()


        editEmail = findViewById(R.id.editEmail)
        editName = findViewById(R.id.editName)
        editPass = findViewById(R.id.editPass)
        editPhone = findViewById(R.id.editPhone)

        btnSignUp = findViewById(R.id.btnSignUp)



        btnSignUp.setOnClickListener {


            createAccount(editEmail.text.toString(),editPass.text.toString())

         /*   table_user.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.child(editEmail.text.toString()).exists()) {

                        Toast.makeText(this@SignUpActivity, "Email already register ", Toast.LENGTH_SHORT).show()


                    } else {

                        //get value to elements
                        val user = UserDbO(editName.text.toString(), editPass.text.toString())

                        //Sign up user account
                        table_user.child(editPhone.text.toString()).setValue(user)
                        Toast.makeText(this@SignUpActivity, "Sign up successfull ! ", Toast.LENGTH_SHORT).show()
                        finish()

                    }


                }


            })*/








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
                Toast.makeText(this@SignUpActivity, "Sign up successfull ! ", Toast.LENGTH_SHORT).show()
                finish()
            }
            else
            {
                //Log.e("AAA", "signUp: Fail!", it.getException())
                Toast.makeText(this@SignUpActivity, "The email address is already in use", Toast.LENGTH_SHORT).show()

            }

        }




    }


    private fun writeNewUser(userId : String, username : String, email : String, phone : String) {
        val user  = UserDbO(username, email,phone)

        table_user.child(userId).setValue(user)
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





}
