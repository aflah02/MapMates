package com.example.mapmates.ui.create

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.ContentResolver
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.widget.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.*
import java.io.IOException
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.EntryActivity
import com.example.mapmates.R
import com.example.mapmates.ui.home.OnGroupItemClickListener
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.InputStream
import java.net.URLEncoder
import java.util.concurrent.CountDownLatch

//Write a fragment class with constructor parameters for name, latitude, and longitude

class CreateFragment() : Fragment() , OnGroupItemClickListener{

    private lateinit var finalNotesTextList: ArrayList<String>
    private var selectedImageUris: ArrayList<Uri>?= null
    private lateinit var username: String
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var imageList : ArrayList<String>
    private lateinit var imageToShow: ArrayList<Bitmap>

    // Current position/index of selected image
    private var position = 0

    // request code to pick image(s)
    private val PICK_IMAGES_CODE = 0


    @SuppressLint("Range")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.newmarkerformlayout, container, false)

        val name = arguments?.getString("name")
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")
        val markerId = arguments?.getString("markerId")
        val groupId = arguments?.getString("groupId")
        val markerUploader = arguments?.getString("markerHoster")



        // get username from shared preferences
        val sharedPrefs = requireActivity().getSharedPreferences("Login", MODE_PRIVATE)
        username = sharedPrefs.getString("Username",null).toString()
        if(username.isBlank()){
            //Run EntryActivity
            val intent = Intent(requireActivity(), EntryActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
//        Toast.makeText(requireActivity(), "Name: $groupId, Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_LONG).show()

        finalNotesTextList = ArrayList()
        val addNotesButton: FloatingActionButton = view.findViewById(R.id.addNotesButton)
        val notesContainer: LinearLayout = view.findViewById(R.id.notesContainer)
        val notesText: EditText = view.findViewById(R.id.note_adder)
        val markerNameTextView: TextView = view.findViewById(R.id.markerNameTextView)
        markerNameTextView.text = name.toString().trim()

        addNotesButton.setOnClickListener {
            val newNote = TextView(requireActivity())
            // set text size to 20sp, layout_margin to 10dp, and center align
            newNote.textSize = 20f
            newNote.textAlignment = View.TEXT_ALIGNMENT_CENTER
            // set margin to 10dp
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 10)
            newNote.text = notesText.text.toString().trim()
            finalNotesTextList.add(notesText.text.toString().trim())
            notesContainer.addView(newNote)
            notesText.text.clear()
            addNotesButton.isEnabled = false
        }
        addNotesButton.isEnabled = false

        val uploadFab : ExtendedFloatingActionButton = view.findViewById(R.id.uploadFab)
        uploadFab.setOnClickListener {
            // Upload all images first
            for (i in 0 until imageList.size){
                uploadImage(imageList[i], i)
            }

            if(markerId == null){
                uploadMarker(name!!, latitude!!, longitude!!, groupId!!)
            } else {
                updateMarker(markerId, markerUploader!!)
            }
//            // remove this fragment from the back stack
            requireActivity().supportFragmentManager.popBackStack()
        }

        // TextWatcher for edit text
        notesText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // if text is empty, hide the button
                addNotesButton.isEnabled = !s.toString().trim().isEmpty()
            }
        })

        imageList = ArrayList<String>()
        imageToShow = ArrayList<Bitmap>()
        val adapter = ImageUploadAdapter(imageToShow, listener = this)
        imageRecyclerView = view.findViewById(R.id.imageUploadRecycler)
        imageRecyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        imageRecyclerView.adapter = adapter

        selectedImageUris = ArrayList()

        return view
    }
    private fun updateMarker(markerID: String, markerUploader: String){
        val userName = markerUploader
        val url = "https://mapsapp-1-m9050519.deta.app/users/$userName/add_images_and_notes_to_marker"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJSON = JSONObject()
        var imageIDSAsStr = ""
        for (imageID in imageList){
            imageIDSAsStr = imageIDSAsStr.plus(imageID)
            imageIDSAsStr = imageIDSAsStr.plus("<DELIMITER069>")
        }

        var notes_list_as_str = ""
        for (note in finalNotesTextList){
            notes_list_as_str = notes_list_as_str.plus(note)
            notes_list_as_str = notes_list_as_str.plus("<DELIMITER069>")
        }
        requestJSON.put("marker_id", markerID)
        requestJSON.put("imageIDs", imageIDSAsStr)
        requestJSON.put("notes", notes_list_as_str)
        requestJSON.put("uploaderName", username)

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
    private fun uploadMarker(name: String, latitude: Double, longitude: Double, groupId: String){
        // convert to string
        val latitudeAsString = latitude.toString()
        val longitudeAsString = longitude.toString()
        val description = ""
        val friendCanSee = (groupId == "friends")
        // Convert to string
        val friendCanSeeAsString = friendCanSee.toString()
        val groups_which_can_see = ArrayList<String>()
        if(!friendCanSee){
            groups_which_can_see.add(groupId)
        }
        // Convert to JSONArray
        var imageIDSAsStr = ""
        for (imageID in imageList){
            imageIDSAsStr = imageIDSAsStr.plus(imageID)
            imageIDSAsStr = imageIDSAsStr.plus("<DELIMITER069>")
        }
        var groupsAsString = ""
        for (group in groups_which_can_see){
            groupsAsString = groupsAsString.plus(group)
            groupsAsString = groupsAsString.plus("<DELIMITER069>")
        }
        val userName = username
        val image_uploaders = ArrayList<String>()
        for (i in 0 until imageList.size){
            image_uploaders.add(userName)
        }
        var image_uploaders_as_str = ""
        for (image_uploader in image_uploaders){
            image_uploaders_as_str = image_uploaders_as_str.plus(image_uploader)
            image_uploaders_as_str = image_uploaders_as_str.plus("<DELIMITER069>")
        }

        var note_uploaders_as_str = ""
        for (ii in 0 until finalNotesTextList.size){
            note_uploaders_as_str = note_uploaders_as_str.plus(username)
            note_uploaders_as_str = note_uploaders_as_str.plus("<DELIMITER069>")
        }

        var notes_list_as_str = ""
        for (note in finalNotesTextList){
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
    private fun uploadImage(encodedImage: String, idx: Int): String {
        var imageID : String? = null
        val url = "https://mapsapp-1-m9050519.deta.app/users/$username/upload_marker_image_url_encoded_base64"
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
                imageList[idx] = imageID!!
            }
        }
        )

        latch.await()
        return "boo"
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
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
                    //Convert image uris to list of bitmap
                    imageToShow = ArrayList<Bitmap>()
                    imageList = ArrayList<String>()
                    for (i in 0 until selectedImageUris!!.size){
                        imageToShow.add(MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUris!![i]))
                        imageList.add(getImageAsURLEncodedBinaryString(requireContext().contentResolver, selectedImageUris!![i])!!)
                    }

                    val adapter = ImageUploadAdapter(imageToShow, listener = this)
                    imageRecyclerView.adapter = adapter
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
                    imageToShow = ArrayList<Bitmap>()
                    imageList = ArrayList<String>()
                    for (i in 0 until selectedImageUris!!.size){
                        imageToShow.add(MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUris!![i]))
                        imageList.add(getImageAsURLEncodedBinaryString(requireContext().contentResolver, selectedImageUris!![i])!!)
                    }
                    val adapter = ImageUploadAdapter(imageToShow, listener = this)
                    imageRecyclerView.adapter = adapter
                    position = 0
                }
            }
        }
    }

    override fun onGroupItemClick(position: Int) {
//        Toast.makeText(requireContext(), "Group item clicked", Toast.LENGTH_SHORT).show()
        pickImagesIntent()
    }

    // unnecessary but since it implements the interface
    override fun onImageNoteClick(position: Int, s: String) {
        return
    }


}
