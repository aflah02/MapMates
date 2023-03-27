package com.example.mapmates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent : Intent = if(userLoggedIn()){
            Intent(applicationContext,MainActivity::class.java)
        } else{
            Intent(applicationContext,LoginActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
    }

    private fun userLoggedIn() : Boolean {
        //Check if a user is already logged in, if not redirect to login screen.
//        return true
        return false
    }
}