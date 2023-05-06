package com.example.mapmates.ui.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.mapmates.R
import com.example.mapmates.databinding.FragmentExploreBinding
import com.example.mapmates.databinding.FragmentLocationSelectorBinding
import com.example.mapmates.utils.LocationPermissionHelper
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
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import java.lang.ref.WeakReference

/**
 * A simple [Fragment] subclass.
 * Use the [LocationSelectorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationSelectorFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var _binding: FragmentLocationSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationPermissionHelper : LocationPermissionHelper


    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onCameraChangeListener = OnCameraChangeListener {
        val cameraState = mapView.getMapboxMap().cameraState
        // iterate over pointAnnotations and update their position
        pointAnnotationManager.annotations.forEach {
            it.point = Point.fromLngLat(cameraState.center.longitude(), cameraState.center.latitude())
            pointAnnotationManager.update(it)
        }
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationSelectorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapView = binding.mapView3
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        mapView.getMapboxMap().addOnCameraChangeListener(onCameraChangeListener)
        val nextFab = binding.nextFab
        val nameField = binding.nameField
        nextFab.isEnabled = false
        nextFab.setOnClickListener {
            // replace current fragment with new fragment
            val bundle = Bundle()
            bundle.putString("name", nameField.text.toString())
            bundle.putDouble("latitude", mapView.getMapboxMap().cameraState.center.latitude())
            bundle.putDouble("longitude", mapView.getMapboxMap().cameraState.center.longitude())

            val newFragment = CreateFragment()
            newFragment.arguments = bundle
            val transaction = parentFragmentManager.beginTransaction()
//            transaction.remove(this)
            transaction.replace(R.id.nav_host_fragment_activity_main, newFragment)
            transaction.addToBackStack(null)
            transaction.setReorderingAllowed(true)
            transaction.commit()
        }
        // create onTextChangeListener for nameField with TextWatcher
        val nameFieldTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nextFab.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                // do nothing
            }
        }
        nameField.addTextChangedListener(nameFieldTextWatcher)
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

            addAnnotationToMap(mapView.getMapboxMap().cameraState.center.latitude(), mapView.getMapboxMap().cameraState.center.longitude())
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
            R.drawable.red_marker
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