package com.example.mapmates.ui.home

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mapmates.R
import com.example.mapmates.databinding.FragmentHomeBinding
import com.example.mapmates.utils.LocationPermissionHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import java.lang.ref.WeakReference
class HomeFragment : Fragment(), OnItemClickListener {
    private lateinit var mapView: MapView
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var groupsList: ArrayList<String>
    private lateinit var groupsRecyclerView: RecyclerView

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
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapView = binding.mapView
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
        // initialize groupsList with few strings
        groupsList = ArrayList<String>()
        groupsList.add("Group 1")
        groupsList.add("Group 2")
        groupsList.add("Group 3")
        groupsList.add("Group 4")


        val groupsFab : ExtendedFloatingActionButton = binding.groupsFab
        groupsFab.setOnClickListener {
            showBottomGroupDialog()
        }
        val locationFab : FloatingActionButton = binding.locationFab
        locationFab.setOnClickListener{
            // TODO: get current location and set camera to it
//            val pt
//            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(pt).build())
//            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(pt)
        }

        return root
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

    private fun showBottomGroupDialog() {
        val groupSheetDialog = BottomSheetDialog(requireContext())
        groupSheetDialog.setContentView(R.layout.group_sheet_dialog)

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
        groupSheetDialog.show()
    }

    override fun onItemClick(position: Int) {
        binding.groupsFab.text = groupsList[position]
    }
}
