package com.example.mapmates.ui.explore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mapmates.R
import com.example.mapmates.databinding.FragmentExploreBinding
import com.example.mapmates.utils.LocationPermissionHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import java.lang.ref.WeakReference
import java.util.*


class ExploreFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var exploreViewModel: ExploreViewModel
    private var _binding: FragmentExploreBinding? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val binding get() = _binding!!
    private var userLoc : Point = Point.fromLngLat(0.0, 0.0)


    private lateinit var locationPermissionHelper : LocationPermissionHelper


    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        userLoc = it
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
        Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show()
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
        exploreViewModel =
            ViewModelProvider(this).get(ExploreViewModel::class.java)

        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapView = binding.mapView2
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

        // Setting up location fab
        val locationFab : FloatingActionButton = binding.locationFab
        locationFab.setOnClickListener{
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(userLoc).build())
            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(userLoc)
        }
        return root
    }

    private fun onMapReady() {
        mapView.scalebar.updateSettings {
            isMetricUnits = true
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
            spawnEvents()
        }
    }
    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
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

    private fun addAnnotationToMap(latitude: Double, longitude: Double) : Long {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        var annotateId = 0L
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.green_marker
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
    private fun spawnEvents(){
        Toast.makeText(requireContext(), "Spawning events", Toast.LENGTH_SHORT).show()
        //Randomly pick 10 coordinates within 50km radius of Point userLoc
        val events = mutableListOf<Point>()
        for(i in 0..9){
            val r = Random()
            val u = r.nextDouble()
            val v = r.nextDouble()
            val w = 5 * Math.sqrt(u)
            val t = 2 * Math.PI * v
            val x = w * Math.cos(t)
            val y = w * Math.sin(t)
            val xp = x / Math.cos(userLoc.latitude())
            val lat = xp + userLoc.latitude()
            val lng = y + userLoc.longitude()
            events.add(Point.fromLngLat(lng, lat))
            addAnnotationToMap(lat, lng)
        }

    }

}