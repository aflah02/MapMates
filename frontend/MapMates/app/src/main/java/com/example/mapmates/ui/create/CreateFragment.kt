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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import java.io.IOException
import android.widget.Button
import android.widget.EditText
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.example.mapmates.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.io.File

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
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
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
            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val notes = notesEditText.text.toString()
            val imageURIs = selectedImageUris

            // Upload the images
            uploadImages(imageURIs!!)

            Log.d("CreateFragment", "Uploaded")
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


//    fun uploadImages(uris: ArrayList<Uri>) {
//        runBlocking {
//            // Create a coroutine scope to run the uploads in parallel
//            val scope = launch(Dispatchers.IO) {
//                // Create an OkHttpClient instance
//                val client = OkHttpClient()
//
//                // Iterate over each URI and upload the corresponding image
//                for (uri in uris) {
//                    val file = uri.path?.let { File(it) }
//
//                    // Create a multipart request with the image file
//                    val requestBody =
//                        file?.let { RequestBody.create("image/*".toMediaTypeOrNull(), it) }?.let {
//                            MultipartBody.Builder()
//                                .setType(MultipartBody.FORM)
//                                .addFormDataPart(
//                                    "image",
//                                    file.name,
//                                    it
//                                )
//                                .build()
//                        }
//
//                    // Create the request object with the URL and request body
//                    val request = requestBody?.let {
//                        Request.Builder()
//                            .url("https://mapsapp-1-m9050519.deta.app/users/Aflah/upload_marker_image")
//                            .post(it)
//                            .build()
//                    }
//
//                    // Send the request asynchronously and handle the response
//                    if (request != null) {
//                        client.newCall(request).enqueue(object : Callback {
//                            override fun onFailure(call: Call, e: IOException) {
//                                // Handle errors
//                            }
//
//                            override fun onResponse(call: Call, response: Response) {
//                                // Handle successful response
//                            }
//                        })
//                    }
//                }
//            }
//            // Wait for all the uploads to complete
//            scope.join()
//            // Log the response
//            Timber.d("Uploads completed")
//        }
//    }


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
