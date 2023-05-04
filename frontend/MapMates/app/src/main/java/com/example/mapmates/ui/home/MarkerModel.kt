package com.example.mapmates.ui.home

data class MarkerModel(val markerId: String, val username: String, val name: String, val desc: String, val groupId: String,
                       var images: ArrayList<String>, var imageUploaders: ArrayList<String>, var notes: ArrayList<String>, var noteUploaders: ArrayList<String>,
                       val latitude: Double, val longitude: Double) {
}
