package com.example.mapmates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch

class LoginActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton : Button = findViewById(R.id.log_in)
        val signupRedirect : TextView = findViewById(R.id.signup_redirect)
        val userNameView : TextView = findViewById(R.id.username_field)
        val passwordView : TextView = findViewById(R.id.password_field)

        signupRedirect.setOnClickListener{
            val intent : Intent = Intent(applicationContext,SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        loginButton.setOnClickListener {
            val userId : String? = logInUser(userNameView.text.toString().trim(),passwordView.text.toString().trim())
            if(userId == null){
                Toast.makeText(applicationContext,"Invalid Credentials!",Toast.LENGTH_SHORT).show()
            }
            else{
                //Store Login Information
                val sharedPref = getSharedPreferences("Login", MODE_PRIVATE)
                val ed = sharedPref.edit()
                ed.putString("Username",userNameView.text.toString().trim())
                ed.putString("UserId",userId)
                ed.apply()

                //Successful Log-in
                val intent : Intent = Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("UserId",userId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }
    }
    @RequiresApi(33)
    private fun logInUser(username : String, password: String): String? {
        var userId : String? = null
        //Make the API Call to log in here, fetch the id on successful login else null
        if(username.isBlank() || password.isBlank())return userId
        val url = "https://mapsapp-1-m9050519.deta.app/login"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("username",username)
        requestJSON.put("password",password)

        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        // find view by id
        findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE

        val latch = CountDownLatch(1)

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login API",e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData)
                if(!response.isSuccessful){
                    latch.countDown()
                    return
                }
                userId = jsonResponse.get("id") as String
                Log.i("Login",jsonResponse.toString(4))
                latch.countDown()
            }
        }
        )

        latch.await()
        findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
        return userId
    }

}
