package com.example.mapmates.ui.profile

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.squareup.picasso.Picasso

import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mapmates.R
import com.example.mapmates.ui.people.friends.FriendData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
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

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra("User", UserName)
            startActivityForResult(intent, 123)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userName  = data?.getStringExtra("User")
        if (requestCode == 123 && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            val encodedImage = imageUri?.let { getImageAsURLEncodedBinaryString(requireContext().contentResolver, it) }
            val imageID = encodedImage?.let { uploadImage(it) }
            Picasso.get().load("https://mapsapp-1-m9050519.deta.app/users/$userName/profile_picture").into(profilePicture)
        }
    }

    private fun uploadImage(encodedImage: String): String? {
        var imageID : String? = null
        val url = "https://mapsapp-1-m9050519.deta.app/users/Aflah/profile_picture"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("image", encodedImage)

        val requestBody = requestJSON.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .addHeader("accept","application/json")
            .addHeader("Content-Type","application/json")
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        val latch = CountDownLatch(1)

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login API",e.message.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonResponse = responseData?.let { JSONObject(it) }
                if (!response.isSuccessful) {
                    latch.countDown()
                    return
                }
                if (jsonResponse != null) {
                    imageID = jsonResponse.get("image_id") as String
                }
                if (jsonResponse != null) {
                    Timber.tag("Login").i(jsonResponse.toString(4))
                }
                latch.countDown()
            }
        }
        )

        latch.await()
        return imageID
    }

    fun getImageAsURLEncodedBinaryString(contentResolver: ContentResolver, uri: Uri): String? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            // URL encode the base64 string
            val urlEncoded = URLEncoder.encode(base64, "UTF-8")
            return urlEncoded

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
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