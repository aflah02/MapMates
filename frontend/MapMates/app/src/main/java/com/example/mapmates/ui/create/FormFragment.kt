//package com.example.mapmates.ui.create
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.example.mapmates.R
//
//class FormFragment : Fragment() {
//
//    private val PERMISSION_CODE = 1000
//    private val IMAGE_PICK_CODE = 1001
//    private lateinit var latitudeEditText: EditText
//    private lateinit var longitudeEditText: EditText
//    private lateinit var nameEditText: EditText
//    private lateinit var addImageButton: Button
//    private lateinit var submitButton: Button
//    private var selectedImageUris: MutableList<Uri> = mutableListOf()
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.newmarkerformlayout, container, false)
//
//        latitudeEditText = view.findViewById(R.id.latitudeEditText)
//        longitudeEditText = view.findViewById(R.id.longitudeEditText)
//        nameEditText = view.findViewById(R.id.nameEditText)
//        addImageButton = view.findViewById(R.id.addImageButton)
//        submitButton = view.findViewById(R.id.submitButton)
//
//        addImageButton.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                pickImagesFromGallery()
//            } else {
//                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
//            }
//        }
//
//        submitButton.setOnClickListener {
//            val latitude = latitudeEditText.text.toString()
//            val longitude = longitudeEditText.text.toString()
//            val name = nameEditText.text.toString()
//
//            // Do something with the latitude, longitude, name, and selectedImageUris
//        }
//
//        return view
//    }
//
//    private fun pickImagesFromGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//        startActivityForResult(intent, IMAGE_PICK_CODE)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == PERMISSION_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                pickImagesFromGallery()
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
//            val clipData = data?.clipData
//            if (clipData != null) {
//                for (i in 0 until clipData.itemCount) {
//                    selectedImageUris.add(clipData.getItemAt(i).uri)
//                }
//            } else {
//                selectedImageUris.add(data?.data!!)
//            }
//        }
//    }
//}
