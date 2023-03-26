package com.example.mapmates.ui.home

import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mapmates.R
import com.example.mapmates.databinding.FragmentHomeBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        Configuration.getInstance().userAgentValue = "github-firefishy-map/0.1"
        var map = binding.map
        val controller = map.controller
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        map.setMultiTouchControls(true)
        controller.setZoom(9.5)

        // Showing marker on current location
        val locationManager = requireActivity().getSystemService(LocationManager::class.java)
        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}