package com.example.mapmates.ui.profile

import com.squareup.picasso.Picasso

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mapmates.R
import com.example.mapmates.ui.people.friends.FriendData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch

class ProfileFragment : Fragment() {
    private lateinit var profilePicture: ImageView
    private lateinit var editBioButton: ImageButton
    private lateinit var saveBioButton: ImageButton
    private lateinit var uploadButton: ImageButton
    private lateinit var name: TextView
    private lateinit var userName: TextView
    private lateinit var userBio: EditText



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        profilePicture = view.findViewById(R.id.profile_picture)
//        TODO: Set name, Username and Bio
        val user = "Aflah"
        val uInfo = getUserInfo(user)?.let { parseJson(it) }
        name = view.findViewById(R.id.nameView)
        userName = view.findViewById(R.id.userNameView)
        userBio = view.findViewById(R.id.bioTextView)
        name.text = uInfo?.name
        userName.text = uInfo?.username
        userBio.setText(uInfo?.bio)
        uploadButton = view.findViewById(R.id.addPicture)
        editBioButton = view.findViewById(R.id.editBioButton)
        saveBioButton = view.findViewById(R.id.saveBioButton)
        val UserName = "Aflah"
//        TODO: get person picture put is in this link
        Picasso.get().load("https://mapsapp-1-m9050519.deta.app/users/$UserName/profile_picture").into(profilePicture)

        editBioButton.setOnClickListener {
            userBio.isEnabled = true
            editBioButton.visibility = View.GONE
            userBio.inputType = InputType.TYPE_CLASS_TEXT
            saveBioButton.visibility = View.VISIBLE
        }
        saveBioButton.setOnClickListener {
//            TODO: post the value of edit text button
            val newBio = userBio.text.toString()
            userBio.setText(newBio)
            setBioCall(UserName, newBio)
            userBio.inputType = InputType.TYPE_NULL
            userBio.isEnabled = false
            saveBioButton.visibility = View.GONE
            editBioButton.visibility = View.VISIBLE

        }

        return view
    }

    private fun setBioCall(userName: String, bio: String): String {
        val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/bio"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("bio", bio)
        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .put(requestBody)
            .build()
        val client = OkHttpClient()

        val latch = CountDownLatch(1)

        var APIresponse = ""

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("ErrorError",  e.toString())
                Log.e("CreateFragment", "Failed to update bio")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully updated bio")
                APIresponse = response.body!!.string()
                latch.countDown()
            }
        })

        latch.await()
        Log.i("Response", APIresponse)
        return APIresponse

    }
    private fun parseJson(jsonString: String): UserInfo {

        val jsonObject = JSONObject(jsonString)
        val userName = jsonObject.getString("username")
        val fullname = jsonObject.getString("full_name")
        val bio = jsonObject.getString("bio")

        return UserInfo(fullname, userName, bio)
    }


    private fun getUserInfo(userName:String): String? {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/Aflah")
            .build()
        val latch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("getUserInfo Failed").e(e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                Timber.tag("getUserInfo Response").i(responseString.toString())
                latch.countDown()
            }
        }
        )
        latch.await()
        return responseString
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}