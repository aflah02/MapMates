package com.example.mapmates

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.math.sign

class SignupActivity : AppCompatActivity() {
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val loginRedirect: TextView = findViewById(R.id.login_redirect)
        val signupButton : Button = findViewById(R.id.sign_up)
        val userNameView : TextView = findViewById(R.id.username_field)
        val passwordView : TextView = findViewById(R.id.password_field)
        val fullNameView : TextView = findViewById(R.id.fullname_field)

        loginRedirect.setOnClickListener{
            val intent : Intent = Intent(applicationContext,LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        signupButton.setOnClickListener {
            val userId : String? = signUpUser(userNameView.text.toString(),passwordView.text.toString(), fullNameView.text.toString())
            if(userId == null){
                Toast.makeText(applicationContext,"Cannot Signup!", Toast.LENGTH_SHORT).show()
            }
            else{
                //Store Login Information
                val sharedPref = getSharedPreferences("Login", MODE_PRIVATE)
                val ed = sharedPref.edit()
                ed.putString("Username",userNameView.text.toString())
                ed.putString("UserId",userId)
                ed.apply()

                //Successful Sign-up
                val intent : Intent = Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("UserId",userId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            }
        }
    }

    @RequiresApi(33)
    private fun signUpUser(username : String, password: String, full_name: String): String? {
        var userId : String? = null
        //Make the API Call to log in here, fetch the id on successful login else null
        if(username.isBlank() || password.isBlank())return userId
        val url = "https://mapsapp-1-m9050519.deta.app/register"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("username",username)
        requestJSON.put("password",password)
        requestJSON.put("full_name", "$full_name ")
        requestJSON.put("email","-")
        requestJSON.put("bio","-")

        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
        val latch = CountDownLatch(1)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Signup2",e.message.toString())
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
                Log.i("Signup2",jsonResponse.toString(4))
                latch.countDown()
            }
        }
        )

        latch.await()
        findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.GONE
        Toast.makeText(applicationContext, "User id: $userId", Toast.LENGTH_SHORT).show()
        return userId
    }

}