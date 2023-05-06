package com.example.mapmates.ui.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.*
import java.io.IOException
import android.widget.Button
import android.widget.EditText
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.example.mapmates.R
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch

class CreateFragment : Fragment() {

    private val PERMISSION_CODE = 1000
    private val IMAGE_PICK_CODE = 1001
    private lateinit var descriptionEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var addImageButton: Button
    private lateinit var submitButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var imageSwitchDisplay: ImageSwitcher
    private var selectedImageUris: ArrayList<Uri>?= null

    // Current position/index of selected image
    private var position = 0

    // request code to pick image(s)
    private val PICK_IMAGES_CODE = 0


    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.newmarkerformlayout, container, false)
        selectedImageUris = ArrayList()
        nameEditText = view.findViewById(R.id.nameEditText)
        notesEditText = view.findViewById(R.id.notesEditText)
        addImageButton = view.findViewById(R.id.addImageButton)
        submitButton = view.findViewById(R.id.submitButton)
        nextButton = view.findViewById(R.id.nextButton)
        previousButton = view.findViewById(R.id.previousButton)
        imageSwitchDisplay = view.findViewById(R.id.imageSwitcher)
        var applicationContext = requireActivity().applicationContext
//        imageSwitchDisplay.setFactory { ImageView(applicationContext) }
        imageSwitchDisplay.setFactory {
            val myView = ImageView(applicationContext)
            myView.scaleType = ImageView.ScaleType.FIT_CENTER
            myView
        }
        addImageButton.setOnClickListener {
            pickImagesIntent()
        }

        submitButton.setOnClickListener {
            val imageURIs = selectedImageUris

            // Upload the images
            val context = requireActivity().applicationContext
            val imageIDs = ArrayList<String>()
            for (uri in imageURIs!!){
                val encodedImage = getImageAsURLEncodedBinaryString(context.contentResolver, uri)
                val imageID = uploadImage(encodedImage!!)
                imageIDs.add(imageID!!)
            }
            Log.i("Image IDs", imageIDs.toString())
//            uploadMarker(imageIDs)
            updateMarker(imageIDs)
            Log.i("Upload", "Marker uploaded")

        }


        nextButton.setOnClickListener {
            if (position < selectedImageUris!!.size - 1){
                position++
                imageSwitchDisplay.setImageURI(selectedImageUris!![position])
            }
            else{
                Toast.makeText(requireActivity(), "No more images", Toast.LENGTH_SHORT).show()
            }
        }

        previousButton.setOnClickListener {
            if (position > 0){
                position--
                imageSwitchDisplay.setImageURI(selectedImageUris!![position])
            }
            else{
                Toast.makeText(requireActivity(), "No more images", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
    private fun updateMarker(imageIDs: ArrayList<String>){
        val userName = "Aflah"
        val markerID = "0"
        val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/add_images_to_marker"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        var imageIDSAsStr = ""
        for (imageID in imageIDs){
            imageIDSAsStr = imageIDSAsStr.plus(imageID)
            imageIDSAsStr = imageIDSAsStr.plus("<DELIMITER069>")
        }
        requestJSON.put("marker_id", markerID)
        requestJSON.put("imageIDs", imageIDSAsStr)
        Log.i("CreateFragment", requestJSON.toString())

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
                Log.i("ErrorError",  e.toString())
                Log.e("CreateFragment", "Failed to upload marker")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("CreateFragment", "Successfully updated marker")
                latch.countDown()
            }
        })

        latch.await()

    }
    private fun uploadMarker(imageIDs: ArrayList<String>){
        val latitude = 0.0
        // convert to string
        val latitudeAsString = latitude.toString()
        val longitude = 0.0
        val longitudeAsString = longitude.toString()
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val notes = notesEditText.text.toString()
        val friendCanSee = false
        // Convert to string
        val friendCanSeeAsString = friendCanSee.toString()
        val groups_which_can_see = ArrayList<String>()
        groups_which_can_see.add("2")
        // Convert to JSONArray
        val imageIDsJSON = JSONArray(imageIDs)
        var imageIDSAsStr = ""
        for (imageID in imageIDs){
            imageIDSAsStr = imageIDSAsStr.plus(imageID)
            imageIDSAsStr = imageIDSAsStr.plus("<DELIMITER069>")
        }
        val groups_which_can_see_JSONArray = JSONArray(groups_which_can_see)
        var groupsAsString = ""
        for (group in groups_which_can_see){
            groupsAsString = groupsAsString.plus(group)
            groupsAsString = groupsAsString.plus("<DELIMITER069>")
        }
        val userName = "Aflah"
        val image_uploaders = ArrayList<String>()
        for (i in 0 until imageIDs.size){
            image_uploaders.add(userName)
        }
        val image_uploaders_JSONArray = JSONArray(image_uploaders)
        var image_uploaders_as_str = ""
        for (image_uploader in image_uploaders){
            image_uploaders_as_str = image_uploaders_as_str.plus(image_uploader)
            image_uploaders_as_str = image_uploaders_as_str.plus("<DELIMITER069>")
        }
        val note_uploaders = ArrayList<String>()
        note_uploaders.add(userName)

        val note_uploaders_JSONArray = JSONArray(note_uploaders)
        var note_uploaders_as_str = ""
        for (note_uploader in note_uploaders){
            note_uploaders_as_str = note_uploaders_as_str.plus(note_uploader)
            note_uploaders_as_str = note_uploaders_as_str.plus("<DELIMITER069>")
        }
        val notes_list = ArrayList<String>()
        notes_list.add(notes)
        val notes_list_JSONArray = JSONArray(notes_list)
        var notes_list_as_str = ""
        for (note in notes_list){
            notes_list_as_str = notes_list_as_str.plus(note)
            notes_list_as_str = notes_list_as_str.plus("<DELIMITER069>")
        }
        val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/add_marker"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        requestJSON.put("latitude", latitudeAsString)
        requestJSON.put("longitude", longitudeAsString)
        requestJSON.put("name", name)
        requestJSON.put("description", description)
        requestJSON.put("friends_can_see", friendCanSeeAsString)
        requestJSON.put("image", imageIDSAsStr)
        requestJSON.put("notes", notes_list_as_str)
        requestJSON.put("group_which_can_see", groupsAsString)
        requestJSON.put("image_uploaders", image_uploaders_as_str)
        requestJSON.put("notes_uploaders", note_uploaders_as_str)
        Log.i("image_id tostring", imageIDSAsStr)
        Log.i("notes tostring", notes_list_as_str)
        Log.i("groups tostring", groupsAsString)
        Log.i("image uploaders tostrin", image_uploaders_as_str)
        Log.i("note uploaders tostring", note_uploaders_as_str)

        Log.i("CreateFragment", requestJSON.toString())

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
                Log.e("CreateFragment", "Failed to upload marker")
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.i("Response", response.toString())
                Log.i("CreateFragment", "Successfully uploaded marker")
                latch.countDown()
            }
        })

        latch.await()

    }
    private fun uploadImage(encodedImage: String): String? {
        var imageID : String? = null
        val url = "https://mapsapp-1-m9050519.deta.app/users/Aflah/upload_marker_image_url_encoded_base64"
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


    private fun pickImagesIntent(){
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_CODE){
            if (resultCode == Activity.RESULT_OK){
                if (data!!.clipData != null){
                    Log.d("CreateFragment", "clipData: ${data.clipData!!.itemCount}")
                    // picked multiple images
                    // get number of picked images
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count){
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        selectedImageUris!!.add(imageUri)
                    }
                    // Remove repeat images
                    selectedImageUris = ArrayList(selectedImageUris!!.toSet())
                    // set the first image to imageSwitcher
                    imageSwitchDisplay.setImageURI(selectedImageUris!![0])
                    position = 0
                }
                else{
                    // picked single image
                    // set image uri to imageSwitcher
                    Log.d("CreateFragment", "data: ${data.data}")
                    val imageUri = data.data
                    selectedImageUris!!.add(imageUri!!)
                    // Remove repeat images
                    selectedImageUris = ArrayList(selectedImageUris!!.toSet())
                    imageSwitchDisplay.setImageURI(selectedImageUris!![0])
                    position = 0
                }
            }
        }
    }
}
