package com.example.mapmates.utils

import android.annotation.SuppressLint
import com.example.mapmates.R
import com.example.mapmates.ui.home.GroupModel
import com.example.mapmates.ui.home.MarkerModel
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Double

class JsonParserHelper {
    fun parseGroupsDataJson(jsonString: String): ArrayList<GroupModel> {

        val jsonObjectArray = JSONArray(jsonString)
        val groupList = ArrayList<GroupModel>()

        for (i in 0 until jsonObjectArray.length()) {
            val jsonObject = jsonObjectArray.getJSONObject(i)
            val groupName = jsonObject.getString("group_name")
            val usersArray = jsonObject.getJSONArray("users")
            val groupCount = usersArray.length().toString()
            val groupId = jsonObject.getString("_id")
            groupList.add(GroupModel(groupId, groupName, groupCount, R.drawable.ic_profile))
        }

        return groupList
    }
    @SuppressLint("UseValueOf")
    fun parseMarkersDataJson(jsonString: String, group_id: String): ArrayList<MarkerModel> {
        val markerList = ArrayList<MarkerModel>()
        val jsonObject = JSONObject(jsonString)
        if (jsonObject.has("markers")) {
            val markers = jsonObject.getJSONArray("markers")
            for (i in 0 until markers.length()) {
                val markerData = markers.getJSONObject(i)
                val username = markerData.getString("username")
                val marker = markerData.getJSONObject("marker")

                val name = marker.getString("name")
                val desc = marker.getString("description")
                val markerId = marker.getString("_id")
                // Convert to double
                val latitude = marker.getString("latitude").toDoubleOrNull() ?: 0.0
                val longitude = marker.getString("longitude").toDoubleOrNull() ?: 0.0

                val images = ArrayList<String>()
                val imageUploaders = ArrayList<String>()
                val notes = ArrayList<String>()
                val noteUploaders = ArrayList<String>()
                if (marker.has("image")) {
                    val imageArray = marker.getJSONArray("image")
                    val imageUploaderArray = marker.getJSONArray("image_uploaders")
                    for (j in 0 until imageArray.length()) {
                        val imageLink = imageArray.getString(j)
                        val imageUploader = imageUploaderArray.getString(j)
                        images.add(imageLink)
                        imageUploaders.add(imageUploader)
                    }
                }
                if (marker.has("images")) {
                    val imageArray = marker.getJSONArray("images")
                    val imageUploaderArray = marker.getJSONArray("image_uploaders")
                    for (j in 0 until imageArray.length()) {
                        val imageLink = imageArray.getString(j)
                        val imageUploader = imageUploaderArray.getString(j)
                        images.add(imageLink)
                        imageUploaders.add(imageUploader)
                    }
                }
                if (marker.has("notes")) {
                    val noteArray = marker.getJSONArray("notes")
                    val noteUploaderArray = marker.getJSONArray("notes_uploaders")
                    for (j in 0 until noteArray.length()) {
                        val noteText = noteArray.getString(j)
                        val noteUploader = noteUploaderArray.getString(j)
                        notes.add(noteText)
                        noteUploaders.add(noteUploader)
                    }
                }
                markerList.add(MarkerModel(markerId, username, name, desc, group_id, images, imageUploaders, notes, noteUploaders, latitude, longitude))
            }
        }
        return markerList
    }
}