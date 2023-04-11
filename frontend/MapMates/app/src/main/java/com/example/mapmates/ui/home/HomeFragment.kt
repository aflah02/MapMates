package com.example.mapmates.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mapmates.databinding.FragmentHomeBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import timber.log.Timber
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var latitude = 28.6139
    private var longitude = 77.2090
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLoction: Location? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates = false
    var startPoint: GeoPoint = GeoPoint(28.6139, 77.2090)
    lateinit var mapController: IMapController
    var marker: Marker? = null
    var path1: Polyline? = null
    private lateinit var map : org.osmdroid.views.MapView

    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            Timber.d("Permissions granted $allAreGranted")
            if (allAreGranted) {
                initCheckLocationSettings()
                //initMap() if settings are ok
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        locationRequest = LocationRequest.create()
            .apply {
                interval = 1000
                fastestInterval = 500
                smallestDisplacement = 10f
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                maxWaitTime = 1000
            }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }
        val br: BroadcastReceiver = LocationProviderChangedReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        requireContext().registerReceiver(br, filter)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mRotationGestureOverlay = RotationGestureOverlay(context, map)
        mRotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(mRotationGestureOverlay)
        map.controller.setZoom(9.4)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
        mapController = binding.map.controller
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE))
        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )

        activityResultLauncher.launch(appPerms)

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root




    }

    private fun initLoaction() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        readLastKnownLocation()
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                binding.root.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        requestingLocationUpdates = true
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdates = false
    }


    companion object {
        val REQUEST_CHECK_SETTINGS = 20202
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsg(status: MyEventLocationSettingsChange) {
        if (status.on) {
            initMap()
        } else {
            Timber.i("Stop something")
        }
    }

    private fun initLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { updateLocation(it) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }

    private fun initCheckLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient =
            LocationServices.getSettingsClient(requireActivity().applicationContext)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Timber.d("Settings Location IS OK")
            MyEventLocationSettingsChange.globalState = true //default
            initMap()
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Timber.d("Settings Location addOnFailureListener call settings")
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.d("Settings Location sendEx??")
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Settings onActivityResult for $requestCode result $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                initMap()
            }
        }
    }

    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(binding.map)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            binding.map.overlays.add(marker)
        }
        return marker!!
    }
    fun updateLocation(newLocation: Location) {
        lastLoction = newLocation
        var currentPoint: GeoPoint = GeoPoint(newLocation.latitude, newLocation.longitude);
        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        Log.i("Location", "updateLocation: $startPoint")
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        binding.map.invalidate()

    }

    fun initMap() {
        initLoaction()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        map.isHorizontalMapRepetitionEnabled = false
        map.isVerticalMapRepetitionEnabled = false
        mapController.setZoom(13.8)
        mapController.setCenter(startPoint);
        binding.map.invalidate()
    }


    private fun getPath(): Polyline { //Singelton
        if (path1 == null) {
            path1 = Polyline()
            path1!!.outlinePaint.color = Color.RED
            path1!!.outlinePaint.strokeWidth = 10f
            path1!!.addPoint(startPoint.clone())
            binding.map.overlayManager.add(path1)
        }
        return path1!!
    }

    fun onClickDraw1(view: View?) {
        startPoint.latitude = startPoint.latitude + (Random().nextDouble() - 0.5) * 0.001
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        binding.map.invalidate()
    }

    fun onClickDraw2(view: View?) {
        startPoint.latitude = startPoint.latitude + (Random().nextDouble() - 0.5) * 0.001
        mapController.setCenter(startPoint)
        val circle = Polygon(binding.map)
        circle.points = Polygon.pointsAsCircle(startPoint, 40.0 + Random().nextInt(100))
        circle.fillPaint.color = 0x32323232 //transparent
        circle.outlinePaint.color = Color.GREEN
        circle.outlinePaint.strokeWidth = 2f
        circle.title = "Area X"
        binding.map.overlays.add(circle) //Duplicate every time new
        binding.map.invalidate()
    }

    fun onClickDraw4(view: View?) {
        //Polyline path = new Polyline();
        startPoint.latitude = startPoint.latitude + (Random().nextDouble() - 0.5) * 0.001
        startPoint.longitude = startPoint.longitude + (Random().nextDouble() - 0.5) * 0.001
        getPath().addPoint(startPoint.clone())
        binding.map.invalidate()
    }
}