package com.example.mapmates.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.print.PrintAttributes.Resolution
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.example.mapmates.databinding.FragmentHomeBinding
import com.example.mapmates.utils.JsonParserHelper
import com.example.mapmates.utils.LocationPermissionHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonParser
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch

class HomeFragment : Fragment(), OnGroupItemClickListener {
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupsList: ArrayList<GroupModel>
    private lateinit var markersList: ArrayList<MarkerModel>
    private lateinit var markerIdList: ArrayList<Long>
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupSheetDialog: BottomSheetDialog
    private lateinit var markerSheetDialog: BottomSheetDialog
    private var mapLoaded: Boolean = false
    private var currentMarker: Int = -1

    private lateinit var locationPermissionHelper : LocationPermissionHelper

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapView = binding.mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        groupsList = ArrayList<GroupModel>()
        markersList = ArrayList<MarkerModel>()
        markerIdList = ArrayList<Long>()

        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

        createBottomGroupDialog()
        getGroupDetails("Aflah")
        createBottomMarkerDialog()

        pointAnnotationManager.addClickListener {clickedAnnotaton ->
            Toast.makeText(requireContext(), "Clicked on ${clickedAnnotaton.id}", Toast.LENGTH_SHORT).show()
            markerSheetDialog.show()
            updateMarkerPage(clickedAnnotaton.id)
            true
        }

        // Setting up change group fab
        val groupsFab : ExtendedFloatingActionButton = binding.groupsFab
        groupsFab.setOnClickListener {
            groupSheetDialog.show()
        }

        // Setting up location fab
        val locationFab : FloatingActionButton = binding.locationFab
        locationFab.setOnClickListener{
            // TODO: get current location and set camera to it
//            val pt
//            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(pt).build())
//            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(pt)
        }

        return root
    }

    private fun createBottomGroupDialog() {
        groupSheetDialog = BottomSheetDialog(requireContext())
        groupSheetDialog.setContentView(R.layout.group_sheet_dialog)
        groupSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // initialize adapter
        groupsRecyclerView = groupSheetDialog.findViewById(R.id.recycler_view)!!
        groupsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        groupsRecyclerView.setHasFixedSize(true)
        val adapter = GroupsAdapter(groupsList, this)
        groupsRecyclerView.adapter = adapter

        val closeButton : ImageButton = groupSheetDialog.findViewById(R.id.closeDialog)!!

        closeButton.setOnClickListener {
            groupSheetDialog.dismiss()
        }
    }

    private fun createBottomMarkerDialog() {
        markerSheetDialog = BottomSheetDialog(requireContext())
        markerSheetDialog.setContentView(R.layout.marker_sheet_dialog)
        val closeButton : ImageButton = markerSheetDialog.findViewById(R.id.closeDialog)!!
        val viewFlipper: ViewFlipper = markerSheetDialog.findViewById(R.id.viewFlipper)!!
        val markerName: TextView = markerSheetDialog.findViewById(R.id.markerHeading)!!
        val prevImage: FloatingActionButton = markerSheetDialog.findViewById(R.id.floatingActionButtonPrev)!!
        val nextImage: FloatingActionButton = markerSheetDialog.findViewById(R.id.floatingActionButtonNext)!!
        val imageBy: TextView = markerSheetDialog.findViewById(R.id.imageBy)!!

        prevImage.setOnClickListener {
            // get id
            viewFlipper.showPrevious()
            // check if currentMarker within range
            if(currentMarker in 0 until markersList.size)
                if(viewFlipper.displayedChild in 0 until markersList[currentMarker].imageUploaders.size)
                    imageBy.text = markersList[currentMarker].imageUploaders[viewFlipper.displayedChild]
        }
        nextImage.setOnClickListener {
            viewFlipper.showNext()
            if(currentMarker in 0 until markersList.size)
                if(viewFlipper.displayedChild in 0 until markersList[currentMarker].imageUploaders.size)
                    imageBy.text = markersList[currentMarker].imageUploaders[viewFlipper.displayedChild]
        }

        closeButton.setOnClickListener {
            markerSheetDialog.dismiss()
        }
    }

    override fun onGroupItemClick(position: Int) {
        binding.groupsFab.text = groupsList[position].groupName
        // iterate over viewholder in groupRecylcerView and set background to white
        for (i in 0 until groupsRecyclerView.childCount) {
            val holder = groupsRecyclerView.getChildViewHolder(groupsRecyclerView.getChildAt(i))
            if(i == position) {
                // select
                holder.itemView.findViewById<TextView>(R.id.groupName).setTextColor(Color.DKGRAY)
                // get groupName
                groupsList[i].isShowing = true
            }
            else {
                // deselect
                holder.itemView.findViewById<TextView>(R.id.groupName).setTextColor(Color.GRAY)
                groupsList[i].isShowing = false
            }
        }
        // reset markers
        pointAnnotationManager.deleteAll()
        markersList.clear()
        markerIdList.clear()
        getAndUpdateMarkerDetails(groupsList[position].groupId)
        groupSheetDialog.dismiss()
    }

    private fun getGroupDetails(username: String) {
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/users/$username/all_group_details")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("groups").e(e.message.toString())
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    return
                }
                groupsList = JsonParserHelper().parseGroupsDataJson(responseString!!)
                groupsList.add(0, GroupModel("friends", "Friends", "420", R.drawable.ic_profile))
                // Run on UI thread
                requireActivity().runOnUiThread {
                    // Add friendsList
                    val adapter = GroupsAdapter(groupsList, this@HomeFragment)
                    groupsRecyclerView.adapter = adapter
                }

                Timber.tag("Groups").i(responseString.toString())
            }
        }
        )
    }

    private fun updateMarkerPage(matchId: Long) {
        var idx = -1
        for (i in 0 until markerIdList.size){
            if(markerIdList[i] == matchId){
                idx = i
            }
        }
        currentMarker = idx
        if(idx == -1) return
        val markerName: TextView = markerSheetDialog.findViewById(R.id.markerHeading)!!
        val viewFlipper: ViewFlipper = markerSheetDialog.findViewById(R.id.viewFlipper)!!
        viewFlipper.removeAllViews()
        markerName.text = markersList[idx].name
        GlobalScope.launch{
            val imageBitmaps = mutableListOf<Bitmap>()

            for (imageAt in markersList[idx].images) {
                val imageUrl = "https://mapsapp-1-m9050519.deta.app/users/$imageAt/marker_image"
                val bitmap = Picasso.get().load(imageUrl).get()
                imageBitmaps.add(bitmap)
            }

            // Update the ViewFlipper in the UI thread
            requireActivity().runOnUiThread {
                if(currentMarker == idx){
                    for (bitmap in imageBitmaps) {
                        val imageView = ImageView(context)
                        imageView.setImageBitmap(bitmap)
                        val layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        imageView.layoutParams = layoutParams
                        viewFlipper.addView(imageView)
                    }
                }
            }
        }
    }
    private fun getAndUpdateMarkerDetails(groupId: String) {
        if(groupId == "friends") {
            return
        }
        var responseString : String? = null
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://mapsapp-1-m9050519.deta.app/groups/$groupId/markers")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.tag("markers").e(e.message.toString())
                Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                responseString = response.body?.string()
                if (!response.isSuccessful) {
                    return
                }
                markersList = JsonParserHelper().parseMarkersDataJson(responseString!!, groupId)
                // Run on UI thread
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "https://mapsapp-1-m9050519.deta.app/groups/$groupId/markers", Toast.LENGTH_LONG).show()
                    Toast.makeText(requireContext(), markersList.toString(), Toast.LENGTH_SHORT).show()
                    for (marker in markersList) {
                        markerIdList.add(addAnnotationToMap(marker.latitude, marker.longitude))
                    }
                }

                Timber.tag("Markers").i(responseString.toString())
            }
        })
    }
    private fun onMapReady() {
        mapView.scalebar.updateSettings {
            enabled = false
        }
        mapView.compass.updateSettings {
            enabled = false
        }
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
            mapLoaded = true
            addAnnotationToMap(28.512, 78.234)
        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_launcher_foreground,
                    )
                },
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // Adding marker logics
    private fun addAnnotationToMap(latitude: Double, longitude: Double) : Long {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        var annotateId = 0L
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.ic_profile
        )?.let {
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(Point.fromLngLat( longitude, latitude))
                // Specify the bitmap you assigned to the point annotation
                // The bitmap will be added to map style automatically.
                .withIconImage(it)
            // Add the resulting pointAnnotation to the map.
            val pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
            annotateId = pointAnnotation.id
        }
        return annotateId
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))
    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}
